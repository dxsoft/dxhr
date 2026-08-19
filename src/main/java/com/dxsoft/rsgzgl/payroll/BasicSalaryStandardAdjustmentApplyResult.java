package com.dxsoft.rsgzgl.payroll;

public record BasicSalaryStandardAdjustmentApplyResult(
        String payrollHistoryId,
        String previousPayrollHistoryId,
        String changeType,
        String message,
        BasicSalaryStandardAdjustmentMidChainExport midChainExport
) {
    public static BasicSalaryStandardAdjustmentApplyResult from(
            PromotionActionResult result,
            BasicSalaryStandardAdjustmentMidChainExport midChainExport) {
        return new BasicSalaryStandardAdjustmentApplyResult(
                result.payrollHistoryId(),
                result.previousPayrollHistoryId(),
                result.changeType(),
                result.message(),
                midChainExport);
    }

    public static BasicSalaryStandardAdjustmentApplyResult from(PromotionActionResult result) {
        return from(result, null);
    }
}
