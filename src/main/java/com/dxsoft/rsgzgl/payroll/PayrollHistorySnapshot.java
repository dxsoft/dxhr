package com.dxsoft.rsgzgl.payroll;

public record PayrollHistorySnapshot(
        String id,
        String organizationCode,
        String personCode,
        String name,
        String calculationYear,
        String calculationMonth,
        String calculationType,
        String positionCode,
        String positionName,
        String positionSalaryGrade,
        String gradeSalaryLevel,
        String gradeSalaryStep,
        String salaryStandardYearMonth,
        String allowanceStandardYearMonth,
        Integer storedTotal
) {
}
