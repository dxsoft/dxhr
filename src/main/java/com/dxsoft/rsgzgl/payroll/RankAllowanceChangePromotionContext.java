package com.dxsoft.rsgzgl.payroll;

record RankAllowanceChangePromotionContext(
        String payrollHistoryId,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String calculationYear,
        String calculationMonth,
        String calculationType,
        String positionCode,
        String positionName,
        String rankName,
        Integer storedRankAllowance,
        String policeStandardYearMonth,
        String prosecutionStandardYearMonth,
        String judicialStandardYearMonth,
        String positionStartYearMonth) {
    String calculationPeriod() {
        return (calculationYear == null ? "" : calculationYear) + (calculationMonth == null ? "" : calculationMonth);
    }
}
