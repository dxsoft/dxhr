package com.dxsoft.rsgzgl.payroll;

record RankAllowanceStandardHistoryMutation(
        String calculationYear,
        String calculationMonth,
        String changeType,
        String policeStandardYearMonth,
        String prosecutionStandardYearMonth,
        String judicialStandardYearMonth,
        Integer rankAllowance,
        Integer totalAmount) {
}
