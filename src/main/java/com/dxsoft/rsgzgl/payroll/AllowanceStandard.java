package com.dxsoft.rsgzgl.payroll;

public record AllowanceStandard(
        Integer id,
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
