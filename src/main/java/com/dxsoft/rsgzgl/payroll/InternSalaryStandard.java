package com.dxsoft.rsgzgl.payroll;

public record InternSalaryStandard(
        String standardYearMonth,
        String educationCode,
        String educationName,
        String regularPositionCode,
        String regularPositionName,
        String regularGradeStep,
        String regularLevel,
        int firstYearAmount,
        int secondYearAmount
) {
}
