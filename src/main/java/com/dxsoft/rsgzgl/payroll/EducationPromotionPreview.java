package com.dxsoft.rsgzgl.payroll;

public record EducationPromotionPreview(
        String payrollHistoryId,
        String organizationCode,
        String personCode,
        String name,
        String calculationPeriod,
        String currentPositionCode,
        String currentPositionName,
        String educationCode,
        String educationName,
        String graduationDate,
        String standardPositionCode,
        String standardPositionName,
        String standardLevel,
        String standardStep,
        String promotedPositionCode,
        String promotedLevel,
        String promotedStep,
        String promotedGradeStepDifference,
        Integer currentPositionSalary,
        Integer promotedPositionSalary,
        Integer currentGradeSalary,
        Integer promotedGradeSalary,
        Integer positionSalaryIncrease,
        Integer gradeSalaryIncrease,
        Integer totalIncrease,
        String nextLevelAssessmentStartYear,
        String nextStepAssessmentStartYear,
        Boolean eligible,
        String note
) {
}
