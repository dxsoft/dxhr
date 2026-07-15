package com.dxsoft.rsgzgl.payroll;

record RankAllowanceStandardContext(
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
        String prosecutionStandardYearMonth,
        String judicialStandardYearMonth,
        Integer storedRankAllowance,
        Integer storedTotal) {
}
