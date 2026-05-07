package com.dxsoft.rsgzgl.payroll;

public record BasicPayrollCalculation(
        String standardYearMonth,
        String positionCode,
        String mappedPositionCode,
        String positionSalaryGrade,
        String gradeSalaryLevel,
        String gradeSalaryStep,
        Integer positionSalary,
        Integer gradeSalary,
        Integer salaryLevelSalary,
        Integer technicalGradeSalary,
        String baseSalarySource,
        Integer selectedBaseSalary,
        Integer storedPositionSalary,
        Integer storedGradeSalary,
        Integer storedTechnicalGradeSalary,
        Integer storedTotal
) {
}
