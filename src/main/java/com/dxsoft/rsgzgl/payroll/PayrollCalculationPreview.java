package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;
import java.util.List;

public record PayrollCalculationPreview(
        Integer uid,
        String organizationCode,
        String personCode,
        String name,
        String calculationPeriod,
        List<PayrollPreviewComponent> calculatedComponents,
        List<ExcludedPayrollComponent> excludedComponents,
        PgbcComparison pgbcComparison,
        BigDecimal recalculatedKnownTotal,
        Integer storedTotal,
        BigDecimal totalDifference
) {
}
