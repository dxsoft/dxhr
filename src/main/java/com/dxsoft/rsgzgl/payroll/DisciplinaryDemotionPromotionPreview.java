package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record DisciplinaryDemotionPromotionPreview(
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
        String promotedLevel,
        String promotedStep,
        Integer currentPositionSalary,
        Integer newPositionSalary,
        Integer currentGradeSalary,
        Integer promotedGradeSalary,
        Integer totalIncrease,
        String changeType,
        Integer linkedAwardId,
        String positionChangeReason,
        Boolean rollbackEligible,
        Boolean applyEligible,
        Boolean eligible,
        String note,
        List<String> explanationLines
) {
}
