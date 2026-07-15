package com.dxsoft.rsgzgl.maintenance;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.security.SecurityAuditLog;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operation-logs")
public class OperationLogController {

    private final OperationLogService operationLogService;

    OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping
    List<SecurityAuditLog> recent(@RequestParam(defaultValue = "100") int limit) {
        return operationLogService.recent(limit);
    }

    @GetMapping("/page")
    PageResponse<SecurityAuditLog> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return operationLogService.search(keyword, PageRequest.of(page, size));
    }
}
