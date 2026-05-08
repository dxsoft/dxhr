package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;

public record ExcludedPayrollComponent(
        String fieldName,
        String caption,
        BigDecimal storedAmount,
        String reason
) {
}
