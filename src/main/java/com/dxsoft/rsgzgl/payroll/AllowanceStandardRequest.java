package com.dxsoft.rsgzgl.payroll;

public record AllowanceStandardRequest(
        String standardYearMonth,
        String item,
        String positionCode,
        String name,
        Integer workYearsLower,
        Integer workYearsUpper,
        Integer amount,
        Integer performanceCategory
) {
}
