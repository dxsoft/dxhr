package com.dxsoft.rsgzgl.payroll;

public record AdditionalPayrollCalculation(
        String rankAllowanceStandardYearMonth,
        String rankName,
        Integer rankAllowance,
        String floatingStep,
        Integer floatingSalary,
        Integer bonusBalance,
        String postAllowanceStandardYearMonth,
        String postAllowanceCategory,
        Integer postAllowance,
        Integer retainedSpecialPostAllowance,
        Integer storedRankAllowance,
        Integer storedFloatingSalary,
        Integer storedBonusBalance,
        Integer storedPostAllowance,
        Integer storedRetainedSpecialPostAllowance
) {
}
