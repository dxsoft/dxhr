package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;
import java.util.List;

public record PayrollCalculationAudit(
        Integer uid,
        String organizationCode,
        String personCode,
        String name,
        String calculationPeriod,
        Integer storedTotal,
        BigDecimal recalculatedKnownTotal,
        BigDecimal totalDifference,
        Boolean matched,
        List<PayrollComponentDifference> componentDifferences
) {
}
