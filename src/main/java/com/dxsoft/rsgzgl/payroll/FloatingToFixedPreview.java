package com.dxsoft.rsgzgl.payroll;

public record FloatingToFixedPreview(
        String payrollHistoryId,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String calculationPeriod,
        String duePeriod,
        String floatingStartYearMonth,
        String positionCode,
        String positionName,
        String currentGradeStep,
        String nextGradeStep,
        String floatingSteps,
        Integer storedFloatingSalary,
        Integer currentGradeSalary,
        Integer nextGradeSalary,
        Integer nextPositionSalary,
        Integer currentTotal,
        Integer nextTotal,
        Integer differenceAmount,
        Boolean eligible,
        Boolean applyEligible,
        Boolean rollbackEligible) {
}
