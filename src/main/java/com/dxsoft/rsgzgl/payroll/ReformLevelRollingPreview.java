package com.dxsoft.rsgzgl.payroll;

public record ReformLevelRollingPreview(
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
        String rollingYear,
        Integer currentGradeSalary,
        Integer promotedGradeSalary,
        Integer increaseAmount,
        String note,
        Boolean applyEligible,
        Boolean rollbackEligible) {
}
