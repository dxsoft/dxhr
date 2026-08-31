package com.dxsoft.rsgzgl.payroll;

public record PerformanceRatioAdjustmentPreview(
        String payrollHistoryId,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String calculationPeriod,
        String currentChangeType,
        String positionCode,
        String positionName,
        String storedPerformanceRatio,
        String targetPerformanceRatio,
        Integer storedPerformanceAllowance,
        Integer calculatedPerformanceAllowance,
        Integer currentTotal,
        Integer calculatedTotal,
        Integer differenceAmount,
        String standardNote,
        Boolean applyEligible,
        Boolean rollbackEligible) {
}
