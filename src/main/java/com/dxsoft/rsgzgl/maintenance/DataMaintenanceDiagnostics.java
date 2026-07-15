package com.dxsoft.rsgzgl.maintenance;

import java.util.Map;

public record DataMaintenanceDiagnostics(
        long personnelCount,
        long payrollHistoryCount,
        long auditLogCount,
        long appRecordMarkerCount,
        long orphanAppRecordMarkerCount,
        Map<String, Long> tableCounts) {
}
