package com.dxsoft.rsgzgl.organization;

import com.dxsoft.rsgzgl.common.NotFoundException;
import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.license.LicenseService;
import com.dxsoft.rsgzgl.maintenance.OperationLogService;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final AccessControlService accessControlService;
    private final OperationLogService operationLogService;
    private final LicenseService licenseService;

    OrganizationService(
            OrganizationRepository organizationRepository,
            AccessControlService accessControlService,
            OperationLogService operationLogService,
            LicenseService licenseService) {
        this.organizationRepository = organizationRepository;
        this.accessControlService = accessControlService;
        this.operationLogService = operationLogService;
        this.licenseService = licenseService;
    }

    public PageResponse<OrganizationSummary> list(String keyword, PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        return PageResponse.of(
                organizationRepository.findAll(keyword, scope, pageRequest),
                pageRequest,
                organizationRepository.count(keyword, scope));
    }

    public PageResponse<OrganizationMaintenanceRecord> maintenanceRecords(String keyword, PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        return PageResponse.of(
                organizationRepository.findMaintenanceRecords(keyword, scope, pageRequest),
                pageRequest,
                organizationRepository.countMaintenanceRecords(keyword, scope));
    }

    public List<OrganizationTreeNode> tree(String keyword) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        return organizationRepository.findTree(scope, keyword);
    }

    public OrganizationMaintenanceRecord maintenanceRecordById(int id) {
        OrganizationMaintenanceRecord existing = organizationRepository.findMaintenanceRecordById(id);
        if (existing == null) {
            throw new NotFoundException("Organization not found: " + id);
        }
        accessControlService.requireOrganization(existing.organizationCode());
        return existing;
    }

    public OrganizationMaintenanceRecord maintenanceRecordByCode(String organizationCode) {
        if (organizationCode == null || organizationCode.isBlank()) {
            throw new IllegalArgumentException("单位编码不能为空。");
        }
        String code = organizationCode.trim();
        accessControlService.requireOrganization(code);
        OrganizationMaintenanceRecord existing = organizationRepository.findMaintenanceRecordByCode(code);
        if (existing == null) {
            throw new NotFoundException("Organization not found: " + code);
        }
        return existing;
    }

    public OrganizationMaintenanceRecord updateMaintenanceRecord(int id, OrganizationMaintenanceRequest request) {
        requireWritePermission();
        OrganizationMaintenanceRecord existing = organizationRepository.findMaintenanceRecordById(id);
        if (existing == null) {
            throw new NotFoundException("Organization not found: " + id);
        }
        accessControlService.requireOrganization(existing.organizationCode());
        licenseService.assertCanModifyOrganization(
                existing.organizationCode(),
                existing.organizationCode(),
                request.name());
        organizationRepository.updateMaintenanceRecord(id, request);
        OrganizationMaintenanceRecord updated = organizationRepository.findMaintenanceRecordById(id);
        operationLogService.record(
                "UPDATE_ORGANIZATION",
                "dwbm",
                updated.organizationCode(),
                "更新单位 " + updated.organizationCode() + " " + updated.name());
        return updated;
    }

    public OrganizationMaintenanceRecord createOrganization(OrganizationCreateRequest request) {
        requireWritePermission();
        if (request.organizationCode() == null || request.organizationCode().isBlank()) {
            throw new IllegalArgumentException("单位编码不能为空。");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("单位名称不能为空。");
        }
        String organizationCode = request.organizationCode().trim();
        if (organizationRepository.existsByOrganizationCode(organizationCode)) {
            throw new IllegalArgumentException("单位编码已存在：" + organizationCode);
        }
        ensureCanCreateOrganization(organizationCode);
        int id = organizationRepository.insertOrganization(request);
        OrganizationMaintenanceRecord created = organizationRepository.findMaintenanceRecordById(id);
        if (created == null) {
            throw new IllegalStateException("单位创建失败。");
        }
        operationLogService.record(
                "CREATE_ORGANIZATION",
                "dwbm",
                created.organizationCode(),
                "新增单位 " + created.organizationCode() + " " + created.name());
        return created;
    }

    public void deleteOrganization(int id) {
        requireWritePermission();
        OrganizationMaintenanceRecord existing = organizationRepository.findMaintenanceRecordById(id);
        if (existing == null) {
            throw new NotFoundException("Organization not found: " + id);
        }
        accessControlService.requireOrganization(existing.organizationCode());
        String code = existing.organizationCode();
        var licenseStatus = licenseService.status();
        if (licenseStatus.authorized() && licenseStatus.subjectCode() != null
                && licenseStatus.subjectCode().equalsIgnoreCase(code)) {
            throw new IllegalStateException("不能删除签约主体编码对应的单位行；若主体仅为签约身份，请使用不在单位树中的主体编码。");
        }
        if (code.length() <= 3) {
            throw new IllegalStateException("不能删除顶级单位。");
        }
        if (organizationRepository.hasChildOrganizations(code)) {
            throw new IllegalStateException("该单位有下级单位，不能删除。");
        }
        if (organizationRepository.hasPersonnel(code)) {
            throw new IllegalStateException("该单位存在人员信息，请先删除人员信息。");
        }
        organizationRepository.deleteById(id);
        operationLogService.record(
                "DELETE_ORGANIZATION",
                "dwbm",
                code,
                "删除单位 " + code + " " + existing.name());
    }

    public OrganizationCodeSuggestion nextRootCode() {
        requireWritePermission();
        return new OrganizationCodeSuggestion(organizationRepository.suggestNextRootCode());
    }

    public OrganizationCodeSuggestion nextChildCode(String parentCode) {
        requireWritePermission();
        if (parentCode == null || parentCode.isBlank()) {
            throw new IllegalArgumentException("增加下辖单位前请先指明上级单位。");
        }
        String code = parentCode.trim();
        accessControlService.requireOrganization(code);
        if (organizationRepository.findMaintenanceRecordByCode(code) == null) {
            throw new NotFoundException("上级单位不存在：" + code);
        }
        return new OrganizationCodeSuggestion(organizationRepository.suggestNextChildCode(code));
    }

    public OrganizationFieldOptions fieldOptions() {
        List<OrganizationFieldOption> properties = organizationRepository.findPropertyOptions();
        if (properties.isEmpty()) {
            properties = stringOptions(organizationRepository.findDistinctValues("dwsx"));
        }
        return new OrganizationFieldOptions(
                properties,
                mergeStringOptions(organizationRepository.findDistinctValues("dwbz"), List.of("行政", "事业")),
                stringOptions(organizationRepository.findDistinctValues("dwjc")),
                stringOptions(organizationRepository.findDistinctValues("gzczbz")),
                stringOptions(organizationRepository.findDistinctValues("jtbz")),
                mergeLabeledOptions(
                        integerOptions(organizationRepository.findDistinctIntegers("dfbt"), value -> value == 0 ? "否" : "是"),
                        List.of(
                                new OrganizationFieldOption("0", "0 否"),
                                new OrganizationFieldOption("1", "1 是"))),
                mergeLabeledOptions(
                        integerOptions(organizationRepository.findDistinctIntegers("jxlb"), null),
                        List.of(
                                new OrganizationFieldOption("0", "0"),
                                new OrganizationFieldOption("1", "1"),
                                new OrganizationFieldOption("2", "2"),
                                new OrganizationFieldOption("5", "5"))),
                mergeLabeledOptions(
                        integerOptions(organizationRepository.findDistinctIntegers("njbt"), null),
                        List.of(
                                new OrganizationFieldOption("0", "0"),
                                new OrganizationFieldOption("1", "1"),
                                new OrganizationFieldOption("2", "2"),
                                new OrganizationFieldOption("3", "3"),
                                new OrganizationFieldOption("4", "4"))),
                stringOptions(organizationRepository.findDistinctValues("jfly")),
                mergeStringOptions(organizationRepository.findDistinctValues("kzfgjj"), List.of("是", "否")),
                mergeStringOptions(organizationRepository.findDistinctValues("kylbxf"), List.of("是", "否")));
    }

    private void requireWritePermission() {
        if (!accessControlService.hasPermission("ORG_WRITE")) {
            throw new IllegalStateException("当前用户没有单位维护权限。");
        }
    }

    /**
     * 新建单位时新编码尚不在角色单位列表中。全部单位范围可建任意编码；
     * 否则要求已有可访问的上级（编码前缀）存在于单位库，或角色范围覆盖该前缀分支。
     */
    private void ensureCanCreateOrganization(String organizationCode) {
        var user = accessControlService.currentUser();
        if (user.allOrganizations()) {
            return;
        }
        for (String allowed : user.organizationCodes()) {
            if (allowed == null || allowed.isBlank()) {
                continue;
            }
            String scope = allowed.trim();
            if (organizationCode.equals(scope) || organizationCode.startsWith(scope)) {
                return;
            }
        }
        for (int len = organizationCode.length() - 1; len >= 1; len--) {
            String parent = organizationCode.substring(0, len);
            if (accessControlService.canAccessOrganization(parent)
                    && organizationRepository.existsByOrganizationCode(parent)) {
                return;
            }
        }
        throw new IllegalStateException("当前账号无权在该单位分支下新增单位：" + organizationCode
                + "。审批账号请使用「全部单位」数据范围，或先获得上级单位权限。");
    }

    private static List<OrganizationFieldOption> stringOptions(List<String> values) {
        List<OrganizationFieldOption> options = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                options.add(new OrganizationFieldOption(value, value));
            }
        }
        return options;
    }

    private static List<OrganizationFieldOption> mergeStringOptions(List<String> values, List<String> defaults) {
        Set<String> seen = new LinkedHashSet<>();
        List<OrganizationFieldOption> options = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank() && seen.add(value)) {
                options.add(new OrganizationFieldOption(value, value));
            }
        }
        for (String value : defaults) {
            if (value != null && !value.isBlank() && seen.add(value)) {
                options.add(new OrganizationFieldOption(value, value));
            }
        }
        return options;
    }

    private static List<OrganizationFieldOption> integerOptions(
            List<Integer> values, java.util.function.Function<Integer, String> labeler) {
        List<OrganizationFieldOption> options = new ArrayList<>();
        for (Integer value : values) {
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value);
            String suffix = labeler == null ? "" : labeler.apply(value);
            String label = suffix == null || suffix.isBlank() ? text : text + " " + suffix;
            options.add(new OrganizationFieldOption(text, label));
        }
        return options;
    }

    private static List<OrganizationFieldOption> mergeLabeledOptions(
            List<OrganizationFieldOption> values, List<OrganizationFieldOption> defaults) {
        Set<String> seen = new LinkedHashSet<>();
        List<OrganizationFieldOption> options = new ArrayList<>();
        for (OrganizationFieldOption option : values) {
            if (option != null && option.value() != null && seen.add(option.value())) {
                options.add(option);
            }
        }
        for (OrganizationFieldOption option : defaults) {
            if (option != null && option.value() != null && seen.add(option.value())) {
                options.add(option);
            }
        }
        return options;
    }
}
