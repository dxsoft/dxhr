package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;

public record YearAllowanceStandard(
        String standardYearMonth,
        BigDecimal categoryOneAmount,
        BigDecimal categoryTwoAmount,
        BigDecimal categoryThreeAmount,
        BigDecimal categoryFourAmount
) {
}
