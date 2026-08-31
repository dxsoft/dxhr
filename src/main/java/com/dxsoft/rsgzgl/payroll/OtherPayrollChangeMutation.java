package com.dxsoft.rsgzgl.payroll;

public record OtherPayrollChangeMutation(
        String calculationYear,
        String calculationMonth,
        String changeType,
        String positionCode,
        String positionName,
        String gradeSalaryLevel,
        String positionSalaryGrade,
        String salaryStandardYearMonth,
        String allowanceStandardYearMonth,
        Integer positionSalary,
        Integer gradeSalary,
        Integer technicalGradeSalary,
        Integer performanceAllowance,
        Integer subsidyAllowance,
        Integer retainedAllowance,
        Integer teachingAllowance,
        Integer salaryIncrease,
        Integer totalAmount) {
}
