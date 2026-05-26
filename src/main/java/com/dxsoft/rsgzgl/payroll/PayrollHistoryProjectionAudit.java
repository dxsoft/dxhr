package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;
import java.util.List;

public record PayrollHistoryProjectionAudit(
        String historyId,
        String calculationPeriod,
        String changeType,
        Boolean projectionEligible,
        String note,
        Boolean matched,
        Integer storedTotal,
        BigDecimal projectedTotal,
        BigDecimal totalDifference,
        List<String> structureMismatches,
        List<PayrollComponentDifference> componentDifferences
) {
}
