package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record BasicSalaryStandardAdjustmentBatchApplyRequest(
        List<BasicSalaryStandardAdjustmentApplyItem> items
) {
    public record BasicSalaryStandardAdjustmentApplyItem(
            String payrollHistoryId,
            String organizationCode,
            String personCode,
            Integer calculatedPositionSalary,
            Integer calculatedGradeSalary,
            Integer calculatedTechnicalGradeSalary,
            Integer calculatedPerformanceAllowance,
            Integer calculatedSubsidyAllowance,
            Integer calculatedFloatingSalary,
            Integer calculatedTotal
    ) {
    }
}
