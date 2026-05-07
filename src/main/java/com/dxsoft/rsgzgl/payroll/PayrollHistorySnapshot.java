package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;

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
        Integer storedPositionSalary,
        Integer storedGradeSalary,
        Integer storedTechnicalGradeSalary,
        Integer storedPerformanceAllowance,
        Integer storedSubsidyAllowance,
        Integer storedRetainedAllowance,
        BigDecimal storedYearAllowance,
        Integer storedTotal
) {
}
