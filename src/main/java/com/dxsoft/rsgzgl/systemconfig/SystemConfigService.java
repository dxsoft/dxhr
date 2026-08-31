package com.dxsoft.rsgzgl.systemconfig;

import com.dxsoft.rsgzgl.common.NotFoundException;
import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.maintenance.OperationLogService;
import com.dxsoft.rsgzgl.security.AccessControlService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;
    private final AccessControlService accessControlService;
    private final OperationLogService operationLogService;

    SystemConfigService(
            SystemConfigRepository systemConfigRepository,
            AccessControlService accessControlService,
            OperationLogService operationLogService) {
        this.systemConfigRepository = systemConfigRepository;
        this.accessControlService = accessControlService;
        this.operationLogService = operationLogService;
    }

    private void requireSystemConfigWrite() {
        if (!accessControlService.hasPermission("SYSTEM_CONFIG")) {
            throw new IllegalStateException("当前用户没有系统配置维护权限。");
        }
    }

    public PageResponse<LocalPolicyConfig> localPolicies(String keyword, PageRequest pageRequest) {
        return PageResponse.of(
                systemConfigRepository.findLocalPolicies(keyword, pageRequest),
                pageRequest,
                systemConfigRepository.countLocalPolicies(keyword));
    }

    public List<SystemOptionConfig> systemOptions() {
        return systemConfigRepository.findSystemOptions();
    }

    public List<SystemOptionConfig> updateSystemOptions(SystemOptionUpdateRequest request) {
        requireSystemConfigWrite();
        systemConfigRepository.updateSystemOptions(request);
        operationLogService.record(
                "UPDATE_SYSTEM_OPTIONS",
                "xtcs",
                "xtcs",
                "更新系统选项");
        return systemConfigRepository.findSystemOptions();
    }

    public LocalPolicyConfig updateLocalPolicy(int id, LocalPolicyUpdateRequest request) {
        requireSystemConfigWrite();
        if (!systemConfigRepository.localPolicyExists(id)) {
            throw new NotFoundException("Local policy not found: " + id);
        }
        systemConfigRepository.updateLocalPolicy(id, request);
        LocalPolicyConfig updated = systemConfigRepository.findLocalPolicyById(id);
        operationLogService.record(
                "UPDATE_LOCAL_POLICY",
                "bdzcsz",
                String.valueOf(id),
                "更新本地政策 " + updated.organizationCode() + " " + updated.organizationName());
        return updated;
    }
}
