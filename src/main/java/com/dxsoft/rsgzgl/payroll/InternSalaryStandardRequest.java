package com.dxsoft.rsgzgl.payroll;

public record InternSalaryStandardRequest(
        String standardYearMonth,
        String educationCode,
        String educationName,
        String regularPositionCode,
        String regularPositionName,
        String regularGradeStep,
        String regularLevel,
        Integer firstYearAmount,
        Integer secondYearAmount
) {
}
