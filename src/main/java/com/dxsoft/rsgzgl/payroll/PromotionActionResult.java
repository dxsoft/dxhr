package com.dxsoft.rsgzgl.payroll;

public record PromotionActionResult(
        String payrollHistoryId,
        String previousPayrollHistoryId,
        String changeType,
        String message
) {
}
