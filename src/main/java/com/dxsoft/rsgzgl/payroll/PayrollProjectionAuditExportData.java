package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record PayrollProjectionAuditExportData(
        long totalPersonnelWithHistory,
        int comparedPersonnel,
        int latestDifferenceCount,
        int historyMismatchPersonCount,
        int totalHistoryRecordsCompared,
        int totalHistoryRecordMismatches,
        List<PayrollProjectionPersonAudit> personSummaries,
        List<PayrollProjectionAuditDetailRow> historyDetails
) {
}
