package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;

public record PayrollComponentDifference(
        String fieldName,
        String caption,
        BigDecimal storedAmount,
        BigDecimal calculatedAmount,
        BigDecimal difference
) {
}
