package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;

public record PayrollComponentValue(
        String fieldName,
        String caption,
        String inputMode,
        Boolean allowance,
        BigDecimal storedAmount
) {
}
