package com.dxsoft.rsgzgl.payroll;

public record SalaryLevelStandardRequest(
        String standardYearMonth,
        String jobCategoryCode,
        String salaryLevel,
        Integer amount,
        Integer baseAmount,
        Integer baseAmountExtra
) {
}
