package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;

public record PayrollCalculationAudit(
        Integer uid,
        String organizationCode,
        String personCode,
        String name,
        String calculationPeriod,
        Integer storedTotal,
        BigDecimal recalculatedKnownTotal,
        BigDecimal totalDifference,
        Boolean matched
) {
}
