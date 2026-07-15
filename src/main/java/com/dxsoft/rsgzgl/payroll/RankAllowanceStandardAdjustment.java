package com.dxsoft.rsgzgl.payroll;

public record RankAllowanceStandardAdjustment(
        String payrollHistoryId,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String calculationPeriod,
        String positionCode,
        String positionName,
        String rankName,
        String policeStandardYearMonth,
        String storedStandardYearMonth,
        String targetStandardYearMonth,
        Integer storedRankAllowance,
        Integer calculatedRankAllowance,
        Integer differenceAmount,
        Boolean eligible,
        Boolean applyEligible,
        Boolean rollbackEligible) {
}
