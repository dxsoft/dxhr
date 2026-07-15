package com.dxsoft.rsgzgl.organization;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.maintenance.OperationLogService;
import com.dxsoft.rsgzgl.common.NotFoundException;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final AccessControlService accessControlService;
    private final OperationLogService operationLogService;

    OrganizationService(
            OrganizationRepository organizationRepository,
            AccessControlService accessControlService,
            OperationLogService operationLogService) {
        this.organizationRepository = organizationRepository;
        this.accessControlService = accessControlService;
        this.operationLogService = operationLogService;
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

    public OrganizationMaintenanceRecord updateMaintenanceRecord(int id, OrganizationMaintenanceRequest request) {
        if (!accessControlService.hasPermission("ORG_WRITE")) {
            throw new IllegalStateException("当前用户没有单位维护权限。");
        }
        OrganizationMaintenanceRecord existing = organizationRepository.findMaintenanceRecordById(id);
        if (existing == null) {
            throw new NotFoundException("Organization not found: " + id);
        }
        accessControlService.requireOrganization(existing.organizationCode());
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
        if (!accessControlService.hasPermission("ORG_WRITE")) {
            throw new IllegalStateException("当前用户没有单位维护权限。");
        }
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
        accessControlService.requireOrganization(organizationCode);
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
}
