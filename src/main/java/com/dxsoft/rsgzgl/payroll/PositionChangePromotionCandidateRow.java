package com.dxsoft.rsgzgl.payroll;

import java.util.Objects;

public record PositionChangePromotionCandidateRow(
        int uid,
        int priority,
        String beforePositionCode,
        String beforePositionName,
        String afterPositionCode,
        String afterPositionName,
        String beforeRankCode,
        String afterRankCode,
        String payrollChangeType,
        String appointmentYearMonth
) {
    boolean processed() {
        return priority == 2;
    }

    /**
     * 待办：现任职务编码相对 tip 有变化（仅职级编码 zjbm 变化不算职务变化待办）。
     */
    boolean pendingAppointmentChange() {
        if (this.processed()) {
            return false;
        }
        String before = PayrollRepository.normalizeAppointmentPositionCode(this.beforePositionCode());
        String after = PayrollRepository.normalizeAppointmentPositionCode(this.afterPositionCode());
        return !Objects.equals(this.normalized(before), this.normalized(after));
    }

    private String normalized(String value) {
        return value == null ? "" : value.trim();
    }
}
