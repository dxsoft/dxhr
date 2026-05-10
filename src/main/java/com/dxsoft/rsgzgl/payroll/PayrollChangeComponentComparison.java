package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;

public record PayrollChangeComponentComparison(
        String fieldName,
        String caption,
        BigDecimal beforeAmount,
        BigDecimal afterAmount,
        BigDecimal difference
) {
}
