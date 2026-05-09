package com.dxsoft.rsgzgl.payroll;

public record LevelPromotionPreview(
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
        Integer currentGradeSalary,
        Integer promotedGradeSalary,
        Integer increaseAmount,
        Boolean eligible,
        String note
) {
}
