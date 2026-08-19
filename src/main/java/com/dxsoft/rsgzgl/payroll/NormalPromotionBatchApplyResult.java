package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record NormalPromotionBatchApplyResult(
        int successCount,
        int failureCount,
        List<String> failures,
        List<String> successIds,
        List<SuccessItem> successes,
        String message
) {
    public record SuccessItem(
            String previousPayrollHistoryId,
            String payrollHistoryId,
            String changeType
    ) {
    }
}
