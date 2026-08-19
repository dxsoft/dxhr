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
        Integer totalAmount,
        Integer positionSalary,
        Integer salaryIncrease
) {
    PromotionHistoryMutation(
            String calculationYear,
            String calculationMonth,
            String changeType,
            String nextStepAssessmentStartYear,
            String nextLevelAssessmentStartYear,
            String positionSalaryGrade,
            String gradeSalaryLevel,
            String gradeSalaryStep,
            Integer gradeSalary,
            Integer totalAmount) {
        this(calculationYear, calculationMonth, changeType, nextStepAssessmentStartYear, nextLevelAssessmentStartYear,
                positionSalaryGrade, gradeSalaryLevel, gradeSalaryStep, gradeSalary, totalAmount, null, null);
    }

    PromotionHistoryMutation(
            String calculationYear,
            String calculationMonth,
            String changeType,
            String nextStepAssessmentStartYear,
            String nextLevelAssessmentStartYear,
            String positionSalaryGrade,
            String gradeSalaryLevel,
            String gradeSalaryStep,
            Integer gradeSalary,
            Integer totalAmount,
            Integer positionSalary) {
        this(calculationYear, calculationMonth, changeType, nextStepAssessmentStartYear, nextLevelAssessmentStartYear,
                positionSalaryGrade, gradeSalaryLevel, gradeSalaryStep, gradeSalary, totalAmount, positionSalary, null);
    }
}
