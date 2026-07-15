package com.dxsoft.rsgzgl.payroll;

public record LevelPromotionCandidateRow(
        int uid,
        int priority
) {
    boolean processed() {
        return priority == 2;
    }
}
