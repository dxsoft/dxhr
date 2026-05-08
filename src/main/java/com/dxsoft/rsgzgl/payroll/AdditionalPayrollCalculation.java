package com.dxsoft.rsgzgl.payroll;

public record AdditionalPayrollCalculation(
        String rankAllowanceStandardYearMonth,
        String rankName,
        Integer rankAllowance,
        String floatingStep,
        Integer floatingSalary,
        Integer storedRankAllowance,
        Integer storedFloatingSalary
) {
}
