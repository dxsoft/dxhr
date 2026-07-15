package com.dxsoft.rsgzgl.payroll;

record InitialPayrollHistoryMutation(
        String calculationYear,
        String calculationMonth,
        String changeType,
        String positionCode,
        String positionName,
        String positionSalaryGrade,
        Integer positionSalary,
        String gradeSalaryLevel,
        String gradeSalaryStep,
        Integer gradeSalary,
        Integer technicalGradeSalary,
        Integer internSalary,
        Integer performanceAllowance,
        Integer subsidyAllowance,
        Integer retainedAllowance,
        Integer teachingAllowance,
        Integer salaryIncrease,
        Integer totalAmount,
        String standardYearMonth,
        String allowanceStandardYearMonth,
        String levelAssessmentStartYear,
        String stepAssessmentStartYear) {
}
