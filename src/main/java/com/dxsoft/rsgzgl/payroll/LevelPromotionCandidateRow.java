package com.dxsoft.rsgzgl.payroll;

public record LevelPromotionCandidateRow(
        int uid,
        int priority,
        PayrollHistorySnapshot history
) {
    public LevelPromotionCandidateRow(int uid, int priority) {
        this(uid, priority, null);
    }

    boolean processed() {
        return priority == 2;
    }
}
