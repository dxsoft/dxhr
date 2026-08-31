package com.dxsoft.rsgzgl.license;

import com.dxsoft.rsgzgl.maintenance.OperationLogService;
import com.dxsoft.rsgzgl.security.AccessControlService;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final AccessControlService accessControlService;
    private final OperationLogService operationLogService;
    private final String hmacSecret;
    private final boolean issueEnabled;

    LicenseService(
            LicenseRepository licenseRepository,
            AccessControlService accessControlService,
            OperationLogService operationLogService,
            @Value("${rsgzgl.license.hmac-secret:dxsoft-rsgzgl-license-dev-secret}") String hmacSecret,
            @Value("${rsgzgl.license.issue-enabled:true}") boolean issueEnabled) {
        this.licenseRepository = licenseRepository;
        this.accessControlService = accessControlService;
        this.operationLogService = operationLogService;
        this.hmacSecret = hmacSecret == null || hmacSecret.isBlank()
                ? "dxsoft-rsgzgl-license-dev-secret"
                : hmacSecret.trim();
        this.issueEnabled = issueEnabled;
    }

    @PostConstruct
    void init() {
        licenseRepository.ensureTable();
    }

    public LicenseStatus status() {
        LicenseRepository.LicenseStatusRow row = licenseRepository.findLatest();
        if (row == null) {
            return new LicenseStatus(
                    false, null, null, null, null, null, 0, null,
                    "尚未导入单位授权包。请导入厂商签发的 .rsauth / .json 授权文件。",
                    null, null);
        }
        Boolean ukeyEnabled = null;
        Boolean ukeyRequired = null;
        if (row.payloadJson() != null && !row.payloadJson().isBlank()) {
            try {
                LicensePackageDocument doc = LicenseCrypto.parseJson(row.payloadJson());
                ukeyEnabled = doc.ukeyEnabled();
                ukeyRequired = doc.ukeyRequired();
            } catch (RuntimeException ignored) {
                // legacy / corrupt payload — ignore policy fields
            }
        }
        if (isExpired(row.expiresAt())) {
            return new LicenseStatus(
                    false,
                    row.subjectCode(),
                    row.subjectName(),
                    row.issuedAt(),
                    row.expiresAt(),
                    row.issuer(),
                    row.organizationCount(),
                    row.fingerprint(),
                    "单位授权已过期，请重新导入授权包。",
                    ukeyEnabled,
                    ukeyRequired);
        }
        return new LicenseStatus(
                true,
                row.subjectCode(),
                row.subjectName(),
                row.issuedAt(),
                row.expiresAt(),
                row.issuer(),
                row.organizationCount(),
                row.fingerprint(),
                "已授权：" + row.subjectName() + "（签约主体 " + row.subjectCode() + "）",
                ukeyEnabled,
                ukeyRequired);
    }

    public boolean isAuthorized() {
        return status().authorized();
    }

    public String requireSubjectCodeOrNull() {
        LicenseStatus current = status();
        return current.authorized() ? current.subjectCode() : null;
    }

    @Transactional
    public LicenseImportResult importPackage(MultipartFile file) {
        requireImportPermission();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择授权文件。");
        }
        String json;
        try {
            json = readPackageJson(file);
        } catch (IOException ex) {
            throw new IllegalArgumentException("无法读取授权文件: " + ex.getMessage());
        }
        LicensePackageDocument doc = LicenseCrypto.parseJson(json);
        validateDocument(doc, true);
        String canonical = LicenseCrypto.canonicalPayload(doc);
        String expected = LicenseCrypto.hmacSha256Hex(hmacSecret, canonical);
        if (!LicenseCrypto.constantTimeEquals(expected, doc.signature())) {
            throw new IllegalArgumentException("授权签名校验失败，文件可能被篡改或密钥不匹配。");
        }
        String fingerprint = LicenseCrypto.fingerprint(canonical);
        String softsn = ("LIC-" + fingerprint).substring(0, Math.min(25, ("LIC-" + fingerprint).length()));

        List<LicenseOrganization> orgs = normalizeOrganizations(doc);
        for (LicenseOrganization org : orgs) {
            if (licenseRepository.organizationExists(org.organizationCode())) {
                licenseRepository.updateOrganization(org);
            } else {
                licenseRepository.insertOrganization(org);
            }
        }
        licenseRepository.upsertCyxx(doc.subject(), doc.localPolicy(), softsn, fingerprint);
        String policyNote = doc.localPolicy() != null
                ? "，已写入本地工资政策"
                : (licenseRepository.hasExistingLocalPolicyFields()
                        ? "，保留已有本地政策"
                        : "");
        String storedJson = LicenseCrypto.toJson(doc);
        licenseRepository.saveImported(doc, fingerprint, storedJson);
        List<String> codes = orgs.stream().map(LicenseOrganization::organizationCode).toList();
        operationLogService.record(
                "LICENSE_IMPORT",
                "app_license",
                doc.subject().organizationCode(),
                "导入单位授权：" + doc.subject().organizationName()
                        + "，初始单位种子 " + orgs.size() + " 个，指纹 " + fingerprint
                        + policyNote
                        + "（不删除本地已有多余单位）");
        return new LicenseImportResult(
                doc.subject().organizationCode(),
                doc.subject().organizationName(),
                orgs.size(),
                codes,
                fingerprint,
                "单位授权导入成功：已写入/更新初始单位种子"
                        + (doc.localPolicy() != null ? "及本地工资政策参数" : policyNote)
                        + "，本地已有单位不会被删除；审批单位可继续增删改单位。");
    }

    public boolean isIssueEnabled() {
        return issueEnabled;
    }

    /**
     * 导出本地 dwbm 单位目录，供 rsgzgl-ops 导入到独立 H2（不共享人事库）。
     */
    public byte[] exportOrganizationsForOps() {
        requireImportPermission();
        List<LicenseOrganization> orgs = licenseRepository.findAllOrganizationsForIssue();
        if (orgs.isEmpty()) {
            throw new IllegalArgumentException("本地单位库为空，无法导出。");
        }
        String city = empty(licenseRepository.findCyxxCity());
        String supervisor = empty(licenseRepository.findCyxxSupervisor());
        LicenseLocalPolicy localPolicy = licenseRepository.findLocalPolicyForIssue().orElse(null);
        String json = LicenseCrypto.toOrgsExportJson(
                LicenseOrgsExportFormat.SEED_FORMAT,
                Instant.now().toString(),
                city,
                supervisor,
                orgs,
                localPolicy);
        operationLogService.record(
                "LICENSE_ORGS_EXPORT",
                "dwbm",
                "all",
                "导出签发种子供 ops：单位数 " + orgs.size()
                        + (localPolicy != null ? "，含本地工资政策" : "，未找到 cyxx 政策"));
        return json.getBytes(StandardCharsets.UTF_8);
    }

    public byte[] issuePackage(LicenseIssueRequest request) {
        if (!issueEnabled) {
            throw new IllegalStateException("本环境已关闭授权签发，请使用运维控制台（rsgzgl-ops）签发。");
        }
        requireImportPermission();
        if (request == null || blank(request.organizationCode())) {
            throw new IllegalArgumentException("签发授权包需要签约主体编码。");
        }
        if (blank(request.organizationName())) {
            throw new IllegalArgumentException("签发授权包需要签约主体名称。");
        }
        String subjectCode = request.organizationCode().trim();
        boolean includeAll = Boolean.TRUE.equals(request.includeAllOrganizations());
        boolean includeSubordinates = request.includeSubordinates() == null || request.includeSubordinates();
        List<LicenseOrganization> orgs;
        if (request.organizations() != null && !request.organizations().isEmpty()) {
            orgs = request.organizations();
        } else if (includeAll) {
            orgs = licenseRepository.findAllOrganizationsForIssue();
            if (orgs.isEmpty()) {
                throw new IllegalArgumentException("本地单位库为空，无法签发全部单位授权包。");
            }
        } else {
            orgs = licenseRepository.findOrganizationsForIssue(subjectCode, includeSubordinates);
            if (orgs.isEmpty()) {
                throw new IllegalArgumentException("本地单位库中未找到单位：" + subjectCode
                        + "。若签约主体不是用人单位，请勾选「包含本地全部单位」。");
            }
        }
        String subjectName = request.organizationName().trim();
        LicenseOrganization matched = orgs.stream()
                .filter(o -> subjectCode.equalsIgnoreCase(nullToEmpty(o.organizationCode())))
                .findFirst()
                .orElse(null);
        String subjectLevel = blank(request.organizationLevel())
                ? (matched == null ? "" : empty(matched.organizationLevel()))
                : request.organizationLevel().trim();
        // 所在城市固定取自 cyxx.szds；对全部单位授权时必填
        String city = empty(licenseRepository.findCyxxCity());
        if (includeAll && blank(city)) {
            throw new IllegalArgumentException(
                    "对全部单位签发授权时，所在城市（cyxx.szds）不能为空，请先在「本地政策/系统配置」中填写所在城市后再签发。");
        }
        LicenseSubject subject = new LicenseSubject(
                subjectCode,
                subjectName,
                subjectLevel,
                city,
                empty(request.supervisor()));
        LicenseLocalPolicy localPolicy = licenseRepository.findLocalPolicyForIssue().orElse(null);
        Boolean ukeyEnabled = request.ukeyEnabled() == null || request.ukeyEnabled();
        Boolean ukeyRequired = Boolean.TRUE.equals(request.ukeyRequired()) && ukeyEnabled;
        LicensePackageDocument unsigned = new LicensePackageDocument(
                LicensePackageDocument.FORMAT,
                Instant.now().toString(),
                blank(request.expiresAt()) ? null : request.expiresAt().trim(),
                blank(request.issuer()) ? "鼎星软件" : request.issuer().trim(),
                subject,
                orgs,
                localPolicy,
                ukeyEnabled,
                ukeyRequired,
                "");
        validateDocument(unsigned, false);
        String signature = LicenseCrypto.hmacSha256Hex(hmacSecret, LicenseCrypto.canonicalPayload(unsigned));
        LicensePackageDocument signed = unsigned.withSignature(signature);
        String scopeNote = includeAll
                ? "（本地全部单位作初始种子）"
                : (includeSubordinates ? "（含前缀下属）" : "（按选定范围）");
        operationLogService.record(
                "LICENSE_ISSUE",
                "app_license",
                subject.organizationCode(),
                "签发单位授权包：" + subject.organizationName()
                        + "，单位数 " + orgs.size()
                        + (localPolicy != null ? "，含本地工资政策" : "")
                        + scopeNote
                        + "，UKey启用=" + ukeyEnabled
                        + "，要求双认证=" + ukeyRequired);
        return LicenseCrypto.toJson(signed).getBytes(StandardCharsets.UTF_8);
    }

    public LicenseIssuePreview previewIssue(
            String organizationCode,
            Boolean includeSubordinates,
            Boolean includeAllOrganizations) {
        if (!issueEnabled) {
            throw new IllegalStateException("本环境已关闭授权签发，请使用运维控制台（rsgzgl-ops）签发。");
        }
        requireImportPermission();
        boolean includeAll = Boolean.TRUE.equals(includeAllOrganizations);
        boolean include = includeSubordinates == null || includeSubordinates;
        List<LicenseOrganization> orgs;
        if (includeAll) {
            orgs = licenseRepository.findAllOrganizationsForIssue();
            if (orgs.isEmpty()) {
                throw new IllegalArgumentException("本地单位库为空。");
            }
        } else {
            if (blank(organizationCode)) {
                throw new IllegalArgumentException("请先填写或选择签约主体编码；或勾选「包含本地全部单位」。");
            }
            orgs = licenseRepository.findOrganizationsForIssue(organizationCode.trim(), include);
            if (orgs.isEmpty()) {
                throw new IllegalArgumentException("本地单位库中未找到单位：" + organizationCode.trim()
                        + "。若主体不是用人单位，请勾选「包含本地全部单位」。");
            }
        }
        String code = blank(organizationCode) ? "" : organizationCode.trim();
        LicenseOrganization root = orgs.stream()
                .filter(o -> code.equalsIgnoreCase(nullToEmpty(o.organizationCode())))
                .findFirst()
                .orElse(null);
        List<String> codes = orgs.stream().map(LicenseOrganization::organizationCode).toList();
        return new LicenseIssuePreview(
                code.isBlank() ? (root == null ? "" : root.organizationCode()) : code,
                root == null ? "" : root.name(),
                root == null ? "" : root.organizationLevel(),
                licenseRepository.findCyxxCity(),
                orgs.size(),
                codes,
                include,
                includeAll);
    }

    public void assertCanModifyOrganization(String organizationCode, String newCode, String newName) {
        LicenseStatus current = status();
        if (!current.authorized() || current.subjectCode() == null) {
            return;
        }
        String subject = current.subjectCode().trim();
        boolean touchesSubject = subject.equalsIgnoreCase(nullToEmpty(organizationCode))
                || subject.equalsIgnoreCase(nullToEmpty(newCode));
        if (!touchesSubject) {
            return;
        }
        // 签约主体仅保护编码；名称可由单位维护权限修改。主体也可为纯签约身份而不在 dwbm。
        if (!accessControlService.hasPermission("LICENSE_IMPORT")) {
            if (newCode != null && !subject.equalsIgnoreCase(newCode.trim())) {
                throw new IllegalStateException("签约主体编码不可修改，请重新导入单位授权包。");
            }
        }
    }

    private void validateDocument(LicensePackageDocument doc, boolean requireSignature) {
        if (doc == null || !LicensePackageDocument.FORMAT.equals(doc.format())) {
            throw new IllegalArgumentException("不支持的授权格式，需要 " + LicensePackageDocument.FORMAT);
        }
        if (doc.subject() == null
                || blank(doc.subject().organizationCode())
                || blank(doc.subject().organizationName())) {
            throw new IllegalArgumentException("授权包缺少签约主体编码或名称。");
        }
        if (requireSignature && blank(doc.signature())) {
            throw new IllegalArgumentException("授权包缺少签名。");
        }
        if (isExpired(doc.expiresAt())) {
            throw new IllegalArgumentException("授权包已过期。");
        }
    }

    private List<LicenseOrganization> normalizeOrganizations(LicensePackageDocument doc) {
        // 仅导入包内单位清单作为种子；签约主体可只写 cyxx，不必强制插入 dwbm。
        List<LicenseOrganization> source = doc.organizations() == null ? List.of() : doc.organizations();
        List<LicenseOrganization> result = new ArrayList<>();
        for (LicenseOrganization org : source) {
            if (blank(org.organizationCode()) || blank(org.name())) {
                continue;
            }
            result.add(org);
        }
        return result;
    }

    private String readPackageJson(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        byte[] bytes = file.getBytes();
        if (name.endsWith(".zip") || name.endsWith(".rsauth")) {
            try (ZipInputStream zis = new ZipInputStream(new java.io.ByteArrayInputStream(bytes))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.isDirectory()) {
                        continue;
                    }
                    String entryName = entry.getName().toLowerCase();
                    if (entryName.endsWith("license.json") || entryName.endsWith(".json")) {
                        return new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                    }
                }
            }
            throw new IllegalArgumentException("授权压缩包中未找到 license.json。");
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private boolean isExpired(String expiresAt) {
        if (blank(expiresAt)) {
            return false;
        }
        try {
            if (expiresAt.contains("T")) {
                return Instant.parse(expiresAt).isBefore(Instant.now());
            }
            return LocalDate.parse(expiresAt.substring(0, Math.min(10, expiresAt.length())))
                    .isBefore(LocalDate.now());
        } catch (DateTimeParseException | StringIndexOutOfBoundsException ex) {
            return false;
        }
    }

    private void requireImportPermission() {
        if (!accessControlService.hasPermission("LICENSE_IMPORT")
                && !accessControlService.hasPermission("SYSTEM_CONFIG")) {
            throw new IllegalStateException("当前用户没有单位授权导入权限。");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String empty(String value) {
        return value == null ? "" : value.trim();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
