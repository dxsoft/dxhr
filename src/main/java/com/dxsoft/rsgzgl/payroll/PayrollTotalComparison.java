package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;
import java.util.List;

public record PayrollTotalComparison(
        String teachingStartYearMonth,
        Integer teachingInterruptedYears,
        Integer teachingAllowance,
        Integer salaryIncrease,
        Integer storedTeachingAllowance,
        Integer storedSalaryIncrease,
        BigDecimal storedComponentTotal,
        BigDecimal recalculatedKnownTotal,
        Integer storedTotal,
        BigDecimal totalDifference,
        List<PayrollComponentDifference> componentDifferences
) {
}
