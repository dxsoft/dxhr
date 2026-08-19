package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;
import java.util.List;

public record PayrollProjectionAuditSummary(
        Long totalPersonnelWithHistory,
        Integer comparedPersonnel,
        Integer latestDifferenceCount,
        Integer historyMismatchPersonCount,
        Integer totalHistoryRecordsCompared,
        Integer totalHistoryRecordMismatches,
        BigDecimal maxAbsoluteDifference,
        List<PayrollProjectionPersonAudit> differences,
        List<PayrollProjectionPersonAudit> audits
) {
}
