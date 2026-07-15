package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;

public record YearAllowanceStandardRequest(
        String standardYearMonth,
        BigDecimal categoryOneAmount,
        BigDecimal categoryTwoAmount,
        BigDecimal categoryThreeAmount,
        BigDecimal categoryFourAmount
) {
}
