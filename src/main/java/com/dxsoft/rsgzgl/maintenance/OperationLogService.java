package com.dxsoft.rsgzgl.maintenance;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.SecurityAuditLog;
import com.dxsoft.rsgzgl.security.SecurityAuditService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OperationLogService {

    private final SecurityAuditService securityAuditService;
    private final AccessControlService accessControlService;

    OperationLogService(SecurityAuditService securityAuditService, AccessControlService accessControlService) {
        this.securityAuditService = securityAuditService;
        this.accessControlService = accessControlService;
    }

    public void record(String action, String resource, String targetId, String summary) {
        securityAuditService.record(action, resource, targetId, summary);
    }

    public     PageResponse<SecurityAuditLog> search(String keyword, PageRequest pageRequest) {
        return search(keyword, null, null, null, pageRequest);
    }

    PageResponse<SecurityAuditLog> search(
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            PageRequest pageRequest) {
        return search(keyword, fromDate, toDate, null, pageRequest);
    }

    PageResponse<SecurityAuditLog> search(
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            String actionPrefix,
            PageRequest pageRequest) {
        requireOperationLogPermission();
        return securityAuditService.search(keyword, fromDate, toDate, actionPrefix, pageRequest);
    }

    public List<SecurityAuditLog> recent(int limit) {
        requireOperationLogPermission();
        return securityAuditService.recent(limit);
    }

    private void requireOperationLogPermission() {
        if (!accessControlService.hasPermission("OPERATION_LOG_READ")) {
            throw new IllegalStateException("当前用户没有上机日志查询权限。");
        }
    }
}
