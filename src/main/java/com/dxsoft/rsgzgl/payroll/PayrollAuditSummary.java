package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;
import java.util.List;

public record PayrollAuditSummary(
        Long totalPersonnelWithHistory,
        Integer comparedPersonnel,
        Integer differenceCount,
        BigDecimal maxAbsoluteDifference,
        List<PayrollCalculationAudit> differences
) {
}
