package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record RankAllowanceChangeBatchApplyResult(
        int successCount,
        int failureCount,
        List<String> failures,
        List<String> successIds,
        List<SuccessItem> successes,
        String message,
        List<RankAllowanceChangeMidChainExport> midChainExports
) {
    public record SuccessItem(
            String previousPayrollHistoryId,
            String payrollHistoryId,
            String changeType
    ) {
    }
}
