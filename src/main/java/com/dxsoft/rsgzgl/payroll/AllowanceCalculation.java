package com.dxsoft.rsgzgl.payroll;

import java.math.BigDecimal;

public record AllowanceCalculation(
        String allowanceStandardYearMonth,
        String positionCode,
        String performancePositionCode,
        String subsidyPositionCode,
        Integer performanceCategory,
        String performanceRatio,
        BigDecimal performanceAllowance,
        Integer subsidyAllowance,
        Integer retainedAllowance,
        BigDecimal yearAllowance,
        Integer storedPerformanceAllowance,
        Integer storedSubsidyAllowance,
        Integer storedRetainedAllowance,
        BigDecimal storedYearAllowance
) {
}
