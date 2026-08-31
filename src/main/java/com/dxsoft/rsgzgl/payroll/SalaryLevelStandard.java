package com.dxsoft.rsgzgl.payroll;

public record SalaryLevelStandard(
        String standardYearMonth,
        String jobCategoryCode,
        String salaryLevel,
        Integer amount,
        Integer baseAmount,
        Integer baseAmountExtra
) {
}
