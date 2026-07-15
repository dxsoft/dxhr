package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;

public record OtherAllowanceStandardRequest(
        String standardType,
        String standardYearMonth,
        String code,
        String name,
        BigDecimal amount,
        BigDecimal averageAmount,
        BigDecimal multiplier
) {
}
