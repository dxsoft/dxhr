package com.dxsoft.rsgzgl.ops.license;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class OpsLicenseService {

    private final LicenseOrgRepository repository;
    private final LicensePolicyRepository policyRepository;
    private final String hmacSecret;
    private final String defaultIssuer;

    OpsLicenseService(
            LicenseOrgRepository repository,
            LicensePolicyRepository policyRepository,
            @Value("${rsgzgl.license.hmac-secret:dxsoft-rsgzgl-license-dev-secret}") String hmacSecret,
            @Value("${rsgzgl.license.default-issuer:鼎星软件}") String defaultIssuer) {
        this.repository = repository;
        this.policyRepository = policyRepository;
        this.hmacSecret = hmacSecret == null || hmacSecret.isBlank()
                ? "dxsoft-rsgzgl-license-dev-secret"
                : hmacSecret.trim();
        this.defaultIssuer = defaultIssuer == null || defaultIssuer.isBlank() ? "鼎星软件" : defaultIssuer.trim();
    }

    public List<LicenseOrgRepository.LicenseOrgRow> listOrgs(String keyword) {
        List<LicenseOrgRepository.LicenseOrgRow> rows = repository.findAll(keyword);
        Map<String, String> issuedAt = repository.latestIssueAtBySubject();
        return rows.stream()
                .map(row -> {
                    String code = row.organizationCode() == null ? "" : row.organizationCode().trim();
                    String at = issuedAt.get(code);
                    boolean issued = at != null && !at.isBlank();
                    return row.withIssueStatus(issued, issued ? at : null);
                })
                .toList();
    }

    public LicenseOrgRepository.LicenseOrgRow saveOrg(LicenseOrgRequest request) {
        if (request == null || blank(request.organizationCode()) || blank(request.name())) {
            throw new IllegalArgumentException("单位编码与名称不能为空");
        }
        repository.upsert(request);
        return repository.findByCode(request.organizationCode().trim());
    }

    public void deleteOrg(String code) {
        if (blank(code)) {
            throw new IllegalArgumentException("单位编码不能为空");
        }
        repository.delete(code.trim());
    }

    public int importCsv(String csvText) {
        if (csvText == null || csvText.isBlank()) {
            throw new IllegalArgumentException("CSV 内容为空");
        }
        String[] lines = csvText.replace("\r\n", "\n").replace('\r', '\n').split("\n");
        int saved = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }
            if (i == 0 && (line.toLowerCase(Locale.ROOT).contains("organization") || line.contains("单位编码"))) {
                continue;
            }
            String[] cols = splitCsv(line);
            if (cols.length < 2 || blank(cols[0]) || blank(cols[1])) {
                continue;
            }
            repository.upsert(new LicenseOrgRequest(
                    cols[0].trim(),
                    cols[1].trim(),
                    col(cols, 2),
                    col(cols, 3),
                    col(cols, 4),
                    col(cols, 5),
                    col(cols, 6),
                    parseInt(col(cols, 7)),
                    parseInt(col(cols, 8)),
                    parseInt(col(cols, 9)),
                    col(cols, 10),
                    col(cols, 19),
                    parseInt(col(cols, 11)),
                    parseInt(col(cols, 12)),
                    col(cols, 20),
                    parseInt(col(cols, 13)),
                    col(cols, 14),
                    col(cols, 15),
                    col(cols, 16),
                    col(cols, 17),
                    col(cols, 18)));
            saved++;
        }
        return saved;
    }

    public static final String ORGS_IMPORT_FORMAT = "RSGZGL_LICENSE_ORGS_V1";
    public static final String SEED_IMPORT_FORMAT = "RSGZGL_LICENSE_SEED_V2";

    public LocalPolicyStatus localPolicyStatus() {
        return LocalPolicyStatus.from(policyRepository.find());
    }

    /**
     * 导入人事系统导出的签发种子（V2 含 localPolicy）或旧版单位目录包（V1）。
     */
    public SeedImportResult importOrgsJson(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("单位包内容为空");
        }
        String format = textField(json, "format");
        if (!ORGS_IMPORT_FORMAT.equals(format) && !SEED_IMPORT_FORMAT.equals(format)) {
            throw new IllegalArgumentException(
                    "不支持的单位包格式，需要 " + SEED_IMPORT_FORMAT + " 或 " + ORGS_IMPORT_FORMAT);
        }
        String defaultCity = textField(json, "city");
        String defaultSupervisor = textField(json, "supervisor");
        String orgsBlock = arrayBlock(json, "organizations");
        List<String> items = splitObjects(orgsBlock);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("单位包中没有 organizations");
        }
        int saved = 0;
        for (String item : items) {
            String code = textField(item, "organizationCode");
            String name = textField(item, "name");
            if (blank(code) || blank(name)) {
                continue;
            }
            String city = textField(item, "city");
            if (blank(city)) {
                city = defaultCity;
            }
            String supervisor = textField(item, "supervisor");
            if (blank(supervisor)) {
                supervisor = defaultSupervisor;
            }
            repository.upsert(new LicenseOrgRequest(
                    code.trim(),
                    name.trim(),
                    textField(item, "shortName"),
                    textField(item, "property"),
                    textField(item, "category"),
                    textField(item, "payrollCategory"),
                    textField(item, "allowanceStandard"),
                    intField(item, "personnelQuota"),
                    intField(item, "establishmentCount"),
                    intField(item, "actualCount"),
                    textField(item, "organizationLevel"),
                    textField(item, "systemCategory"),
                    intField(item, "performanceAllowanceEnabled"),
                    intField(item, "performanceCategory"),
                    textField(item, "performanceRatio"),
                    intField(item, "yearAllowanceCategory"),
                    textField(item, "financeSource"),
                    textField(item, "housingFundWithheld"),
                    textField(item, "pensionWithheld"),
                    city,
                    supervisor));
            saved++;
        }
        LicenseLocalPolicy localPolicy = LicenseCrypto.parseLocalPolicyFromSeed(json);
        boolean policySynced = false;
        if (localPolicy != null) {
            policyRepository.save(localPolicy);
            policySynced = true;
        }
        if (saved == 0) {
            throw new IllegalArgumentException("单位包中未解析到有效单位（需 organizationCode 与 name）。");
        }
        return new SeedImportResult(saved, format, policySynced);
    }

    public byte[] issue(OpsLicenseIssueRequest request) {
        if (request == null || blank(request.organizationCode()) || blank(request.organizationName())) {
            throw new IllegalArgumentException("签发需要签约主体编码与名称");
        }
        String subjectCode = request.organizationCode().trim();
        List<LicenseOrganization> orgs = resolveOrganizations(request);
        if (orgs.isEmpty()) {
            throw new IllegalArgumentException("没有可写入授权包的单位，请先维护单位目录或勾选单位");
        }
        LicenseOrgRepository.LicenseOrgRow subjectRow = repository.findByCode(subjectCode);
        String subjectLevel = blank(request.organizationLevel())
                ? (subjectRow == null ? "" : empty(subjectRow.organizationLevel()))
                : request.organizationLevel().trim();
        String city = blank(request.city())
                ? (subjectRow == null ? "" : empty(subjectRow.city()))
                : request.city().trim();
        LicenseSubject subject = new LicenseSubject(
                subjectCode,
                request.organizationName().trim(),
                subjectLevel,
                city,
                empty(request.supervisor()));
        LicenseLocalPolicy localPolicy = policyRepository.find();
        Boolean ukeyEnabled = request.ukeyEnabled() == null || request.ukeyEnabled();
        Boolean ukeyRequired = Boolean.TRUE.equals(request.ukeyRequired()) && ukeyEnabled;
        LicensePackageDocument unsigned = new LicensePackageDocument(
                LicensePackageDocument.FORMAT,
                Instant.now().toString(),
                blank(request.expiresAt()) ? null : request.expiresAt().trim(),
                blank(request.issuer()) ? defaultIssuer : request.issuer().trim(),
                subject,
                orgs,
                localPolicy,
                ukeyEnabled,
                ukeyRequired,
                "");
        String canonical = LicenseCrypto.canonicalPayload(unsigned);
        String signature = LicenseCrypto.hmacSha256Hex(hmacSecret, canonical);
        LicensePackageDocument signed = unsigned.withSignature(signature);
        String fingerprint = LicenseCrypto.fingerprint(canonical);
        repository.insertIssueLog(
                currentUsername(),
                subject.organizationCode(),
                subject.organizationName(),
                signed.expiresAt(),
                orgs.size(),
                fingerprint,
                "签发单位授权包，单位数 " + orgs.size()
                        + (localPolicy != null ? "，含本地工资政策" : "，未同步本地政策")
                        + "，UKey启用=" + ukeyEnabled
                        + "，要求双认证=" + ukeyRequired
                        + (Boolean.TRUE.equals(request.includeAllOrganizations())
                        ? "，全部单位"
                        : (includeSubordinatesNote(request) ? "，含下属" : ""))
                        + "，指纹 " + fingerprint);
        return LicenseCrypto.toJson(signed).getBytes(StandardCharsets.UTF_8);
    }

    private static boolean includeSubordinatesNote(OpsLicenseIssueRequest request) {
        return request.includeSubordinates() == null || request.includeSubordinates();
    }

    public List<LicenseOrgRepository.IssueLogRow> issueLogs(int limit) {
        return repository.recentLogs(limit);
    }

    private List<LicenseOrganization> resolveOrganizations(OpsLicenseIssueRequest request) {
        List<String> codes = request.organizationCodes();
        boolean includeAll = Boolean.TRUE.equals(request.includeAllOrganizations());
        boolean includeSubordinates = request.includeSubordinates() == null || request.includeSubordinates();
        List<LicenseOrgRepository.LicenseOrgRow> all = repository.findAll(null);
        if (includeAll) {
            return toLicenseOrgs(all);
        }

        LinkedHashSet<String> roots = new LinkedHashSet<>();
        if (codes != null) {
            for (String code : codes) {
                if (!blank(code)) {
                    roots.add(code.trim());
                }
            }
        }
        // 未勾选包含单位时：仅签约主体（可选含下属），不再默认整库
        if (roots.isEmpty() && !blank(request.organizationCode())) {
            roots.add(request.organizationCode().trim());
        }
        if (roots.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> unique = new LinkedHashSet<>(roots);
        if (includeSubordinates) {
            for (LicenseOrgRepository.LicenseOrgRow row : all) {
                String code = row.organizationCode() == null ? "" : row.organizationCode().trim();
                if (code.isEmpty()) {
                    continue;
                }
                for (String root : roots) {
                    if (code.equals(root)
                            || (code.startsWith(root) && code.length() > root.length())) {
                        unique.add(code);
                        break;
                    }
                }
            }
        }

        List<LicenseOrgRepository.LicenseOrgRow> rows = all.stream()
                .filter(row -> {
                    String code = row.organizationCode() == null ? "" : row.organizationCode().trim();
                    return unique.contains(code);
                })
                .toList();
        return toLicenseOrgs(rows);
    }

    private static List<LicenseOrganization> toLicenseOrgs(List<LicenseOrgRepository.LicenseOrgRow> rows) {
        List<LicenseOrganization> orgs = new ArrayList<>();
        for (LicenseOrgRepository.LicenseOrgRow row : rows) {
            if (!blank(row.organizationCode()) && !blank(row.name())) {
                orgs.add(row.toLicenseOrganization());
            }
        }
        return orgs;
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null || auth.getName() == null ? "ops" : auth.getName();
    }

    private static String[] splitCsv(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (ch == ',' && !inQuotes) {
                parts.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(ch);
        }
        parts.add(current.toString());
        return parts.toArray(String[]::new);
    }

    private static String col(String[] cols, int index) {
        return index < cols.length ? cols[index] : "";
    }

    private static Integer parseInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String empty(String value) {
        return value == null ? "" : value.trim();
    }

    private static String textField(String json, String name) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\"" + java.util.regex.Pattern.quote(name) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
                .matcher(json);
        if (!matcher.find()) {
            return "";
        }
        return matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r");
    }

    private static Integer intField(String json, String name) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\"" + java.util.regex.Pattern.quote(name) + "\"\\s*:\\s*(null|-?\\d+)")
                .matcher(json);
        if (!matcher.find() || "null".equals(matcher.group(1))) {
            return null;
        }
        return Integer.valueOf(matcher.group(1));
    }

    private static String arrayBlock(String json, String name) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\"" + java.util.regex.Pattern.quote(name) + "\"\\s*:\\s*\\[")
                .matcher(json);
        if (!matcher.find()) {
            return "[]";
        }
        return extractBalanced(json, matcher.end() - 1, '[', ']');
    }

    private static String extractBalanced(String text, int start, char open, char close) {
        int depth = 0;
        boolean inString = false;
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '"' && (i == 0 || text.charAt(i - 1) != '\\')) {
                inString = !inString;
            }
            if (inString) {
                continue;
            }
            if (ch == open) {
                depth++;
            } else if (ch == close) {
                depth--;
                if (depth == 0) {
                    return text.substring(start, i + 1);
                }
            }
        }
        return "";
    }

    private static List<String> splitObjects(String arrayJson) {
        List<String> items = new ArrayList<>();
        int depth = 0;
        boolean inString = false;
        int start = -1;
        for (int i = 0; i < arrayJson.length(); i++) {
            char ch = arrayJson.charAt(i);
            if (ch == '"' && (i == 0 || arrayJson.charAt(i - 1) != '\\')) {
                inString = !inString;
            }
            if (inString) {
                continue;
            }
            if (ch == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (ch == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    items.add(arrayJson.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return items;
    }
}
