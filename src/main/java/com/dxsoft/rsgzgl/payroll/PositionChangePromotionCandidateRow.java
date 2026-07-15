package com.dxsoft.rsgzgl.payroll;

public record PositionChangePromotionCandidateRow(
        int uid,
        int priority,
        String beforePositionCode,
        String beforePositionName,
        String afterPositionCode,
        String afterPositionName,
        String payrollChangeType
) {
    boolean processed() {
        return priority == 2;
    }
}
