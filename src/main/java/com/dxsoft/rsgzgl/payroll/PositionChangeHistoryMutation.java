package com.dxsoft.rsgzgl.payroll;

record PositionChangeHistoryMutation(
        String calculationYear,
        String calculationMonth,
        String changeType,
        String nextStepAssessmentStartYear,
        String nextLevelAssessmentStartYear,
        String positionCode,
        String positionName,
        Integer positionSalary,
        String positionSalaryGrade,
        String gradeSalaryLevel,
        String gradeSalaryStep,
        Integer gradeSalary,
        Integer technicalGradeSalary,
        Integer performanceAllowance,
        Integer subsidyAllowance,
        Integer retainedAllowance,
        Integer salaryIncrease,
        Integer pgbc,
        Integer totalAmount
) {
}
