package com.dxsoft.rsgzgl.payroll;

public record AdditionalPayrollCalculation(
        String rankAllowanceStandardYearMonth,
        String rankName,
        Integer rankAllowance,
        String floatingStep,
        Integer floatingSalary,
        Integer bonusBalance,
        Integer storedRankAllowance,
        Integer storedFloatingSalary,
        Integer storedBonusBalance
) {
}
