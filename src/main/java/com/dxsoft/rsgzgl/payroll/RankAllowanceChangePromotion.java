package com.dxsoft.rsgzgl.payroll;

public record RankAllowanceChangePromotion(
        String payrollHistoryId,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String calculationPeriod,
        String positionCode,
        String positionName,
        String storedRankName,
        String targetRankName,
        String rankChangeYearMonth,
        Integer storedRankAllowance,
        Integer calculatedRankAllowance,
        Integer differenceAmount,
        Boolean eligible,
        Boolean applyEligible,
        Boolean rollbackEligible) {
}
