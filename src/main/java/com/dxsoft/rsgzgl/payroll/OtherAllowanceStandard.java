package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;

public record OtherAllowanceStandard(
        String standardType,
        String standardYearMonth,
        String code,
        String name,
        BigDecimal amount,
        BigDecimal averageAmount,
        BigDecimal multiplier
) {
    public OtherAllowanceStandard withName(String name) {
        return new OtherAllowanceStandard(
                standardType,
                standardYearMonth,
                code,
                name,
                amount,
                averageAmount,
                multiplier);
    }
}
