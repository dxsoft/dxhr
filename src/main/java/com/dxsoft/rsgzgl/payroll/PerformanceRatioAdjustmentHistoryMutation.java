package com.dxsoft.rsgzgl.payroll;

record PerformanceRatioAdjustmentHistoryMutation(
        String calculationYear,
        String calculationMonth,
        String changeType,
        String performanceRatio,
        Integer performanceAllowance,
        Integer totalAmount) {
}
