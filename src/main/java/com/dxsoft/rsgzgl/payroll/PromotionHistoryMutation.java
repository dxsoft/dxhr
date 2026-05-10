package com.dxsoft.rsgzgl.payroll;

record PromotionHistoryMutation(
        String calculationYear,
        String calculationMonth,
        String changeType,
        String nextStepAssessmentStartYear,
        String nextLevelAssessmentStartYear,
        String positionSalaryGrade,
        String gradeSalaryLevel,
        String gradeSalaryStep,
        Integer gradeSalary,
        Integer totalAmount
) {
}
