package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record PayrollCalculationContext(
        Integer uid,
        PayrollHistorySnapshot latestHistory,
        BasicPayrollCalculation basicCalculation,
        List<PayrollComponentValue> storedComponents,
        List<PositionSalaryStandard> matchedPositionStandards,
        List<AllowanceStandard> matchedAllowanceStandards
) {
}
