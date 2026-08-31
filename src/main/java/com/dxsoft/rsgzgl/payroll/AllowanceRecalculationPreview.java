package com.dxsoft.rsgzgl.payroll;

public record AllowanceRecalculationPreview(
        String payrollHistoryId,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String calculationPeriod,
        String currentChangeType,
        String currentAllowanceStandardYearMonth,
        String resolvedAllowanceStandardYearMonth,
        String positionCode,
        String positionName,
        Integer storedPerformanceAllowance,
        Integer storedSubsidyAllowance,
        Integer storedRetainedAllowance,
        Integer storedSalaryIncrease,
        Integer calculatedPerformanceAllowance,
        Integer calculatedSubsidyAllowance,
        Integer calculatedRetainedAllowance,
        Integer calculatedSalaryIncrease,
        Integer currentTotal,
        Integer calculatedTotal,
        Integer differenceAmount,
        String standardNote,
        Boolean applyEligible,
        Boolean rollbackEligible) {
}
