package com.dxsoft.rsgzgl.payroll;

public record RankAllowanceChangeApplyResult(
        String payrollHistoryId,
        String previousPayrollHistoryId,
        String changeType,
        String message,
        RankAllowanceChangeMidChainExport midChainExport
) {
    public static RankAllowanceChangeApplyResult from(PromotionActionResult result) {
        return from(result, null);
    }

    public static RankAllowanceChangeApplyResult from(
            PromotionActionResult result,
            RankAllowanceChangeMidChainExport midChainExport) {
        return new RankAllowanceChangeApplyResult(
                result.payrollHistoryId(),
                result.previousPayrollHistoryId(),
                result.changeType(),
                result.message(),
                midChainExport);
    }
}
