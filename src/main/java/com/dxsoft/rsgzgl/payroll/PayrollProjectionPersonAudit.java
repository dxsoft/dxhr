package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;
import java.util.List;

public record PayrollProjectionPersonAudit(
        Integer uid,
        String organizationCode,
        String personCode,
        String name,
        String latestPeriod,
        Boolean latestProjectionEligible,
        String latestNote,
        Integer storedTotal,
        BigDecimal projectedTotal,
        BigDecimal latestTotalDifference,
        Boolean latestMatched,
        Integer historyRecordCount,
        Integer historyMismatchCount,
        List<PayrollHistoryProjectionAudit> historyMismatches
) {
}
