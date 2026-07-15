package com.dxsoft.rsgzgl.maintenance;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data-maintenance")
public class DataMaintenanceController {

    private final DataMaintenanceService dataMaintenanceService;

    DataMaintenanceController(DataMaintenanceService dataMaintenanceService) {
        this.dataMaintenanceService = dataMaintenanceService;
    }

    @GetMapping("/diagnostics")
    DataMaintenanceDiagnostics diagnostics() {
        return dataMaintenanceService.diagnostics();
    }

    @PostMapping("/purge-audit-logs")
    int purgeAuditLogs(@RequestParam(defaultValue = "90") int keepDays) {
        return dataMaintenanceService.purgeAuditLogs(keepDays);
    }

    @PostMapping("/purge-orphan-markers")
    int purgeOrphanAppRecordMarkers() {
        return dataMaintenanceService.purgeOrphanAppRecordMarkers();
    }
}
