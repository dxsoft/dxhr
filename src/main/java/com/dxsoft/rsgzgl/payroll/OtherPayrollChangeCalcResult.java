package com.dxsoft.rsgzgl.payroll;

public record OtherPayrollChangeCalcResult(
        String calculationPeriod,
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
        Integer totalAmount,
        String performanceAllowanceCaption,
        String subsidyAllowanceCaption,
        Boolean showSubsidyAllowance,
        String note) {
}
