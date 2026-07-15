package com.dxsoft.rsgzgl.payroll;

record RankAllowanceChangeHistoryMutation(
        String calculationYear,
        String calculationMonth,
        String changeType,
        String rankName,
        String policeStandardYearMonth,
        String prosecutionStandardYearMonth,
        String judicialStandardYearMonth,
        Integer rankAllowance,
        Integer totalAmount) {
}
