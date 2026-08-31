package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record BasicSalaryStandardAdjustmentBatchApplyResult(
        int successCount,
        int failureCount,
        List<String> failures,
        List<String> successIds,
        List<SuccessItem> successes,
        String message,
        List<BasicSalaryStandardAdjustmentMidChainExport> midChainExports
) {
    public record SuccessItem(
            String previousPayrollHistoryId,
            String payrollHistoryId,
            String changeType
    ) {
    }
}
