package com.dxsoft.rsgzgl.payroll;

public record LevelPromotionPreview(
        Integer uid,
        String payrollHistoryId,
        String organizationCode,
        String personCode,
        String name,
        String calculationPeriod,
        String changeType,
        String positionCode,
        String positionName,
        String salaryStandardYearMonth,
        String currentLevel,
        String currentStep,
        String promotedLevel,
        String promotedStep,
        String levelAssessmentStartYear,
        String stepAssessmentStartYear,
        String nextLevelAssessmentStartYear,
        String nextStepAssessmentStartYear,
        Integer qualifiedYearsForLevel,
        Integer qualifiedYearsForStep,
        Boolean levelPromotionDue,
        Boolean stepPromotionDue,
        Boolean gradeIncreaseExceedsStepDifference,
        Integer currentGradeSalary,
        Integer promotedGradeSalary,
        Integer increaseAmount,
        Boolean eligible,
        String note
) {
}
