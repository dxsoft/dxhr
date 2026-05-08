package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record PayrollCalculationContext(
        Integer uid,
        PayrollHistorySnapshot latestHistory,
        BasicPayrollCalculation basicCalculation,
        AllowanceCalculation allowanceCalculation,
        AdditionalPayrollCalculation additionalCalculation,
        PayrollTotalComparison totalComparison,
        List<PayrollComponentValue> storedComponents,
        List<PositionSalaryStandard> matchedPositionStandards,
        List<AllowanceStandard> matchedAllowanceStandards
) {
}
