package com.dxsoft.rsgzgl.payroll;

public record PositionChangePromotionPreview(
        String payrollHistoryId,
        String organizationCode,
        String personCode,
        String name,
        String currentPositionCode,
        String currentPositionName,
        String newPositionCode,
        String newPositionName,
        String positionStartYearMonth,
        String effectivePeriod,
        String salaryStandardYearMonth,
        String currentLevel,
        String currentStep,
        String newPositionMinimumLevel,
        String newPositionMaximumLevel,
        String promotedLevel,
        String promotedStep,
        Integer currentPositionSalary,
        Integer newPositionSalary,
        Integer currentGradeSalary,
        Integer promotedGradeSalary,
        Integer positionSalaryIncrease,
        Integer gradeSalaryIncrease,
        Integer totalIncrease,
        String nextLevelAssessmentStartYear,
        Boolean eligible,
        String note
) {
}
