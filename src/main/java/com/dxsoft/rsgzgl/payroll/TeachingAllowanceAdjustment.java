package com.dxsoft.rsgzgl.payroll;

public record TeachingAllowanceAdjustment(
        String payrollHistoryId,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String calculationPeriod,
        String positionCode,
        String positionName,
        String teachingStartYearMonth,
        Integer interruptedYears,
        Integer teachingYears,
        Integer storedAmount,
        Integer calculatedAmount,
        Integer differenceAmount,
        Boolean eligible,
        Boolean applyEligible,
        Boolean rollbackEligible
) {
}
