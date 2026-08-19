package com.dxsoft.rsgzgl.payroll;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PositionChangePromotionCandidateRowTest {

    @Test
    void pendingAppointmentChangeDetectsPositionCodeChange() {
        PositionChangePromotionCandidateRow row = new PositionChangePromotionCandidateRow(
                100, 1, "0504", "机关中级工", "0503", "机关高级工",
                null, null, "正常档次", "2022.09");

        assertThat(row.pendingAppointmentChange()).isTrue();
    }

    @Test
    void pendingAppointmentChangeIgnoresRankOnlyChange() {
        PositionChangePromotionCandidateRow row = new PositionChangePromotionCandidateRow(
                100, 1, "0190", "正科级领导职务", "0190", "正科级领导职务",
                "2307", "2306", "正常档次", "2024.07");

        assertThat(row.pendingAppointmentChange()).isFalse();
    }

    @Test
    void pendingAppointmentChangeNormalizesLegacyPositionCodes() {
        PositionChangePromotionCandidateRow row = new PositionChangePromotionCandidateRow(
                100, 1, "0116", "副科级非领导职务", "01A1", "副科级非领导职务",
                null, null, "正常档次", "2024.07");

        assertThat(row.pendingAppointmentChange()).isFalse();
    }

    @Test
    void processedRowIsNeverPendingAppointmentChange() {
        PositionChangePromotionCandidateRow row = new PositionChangePromotionCandidateRow(
                100, 2, "0504", "机关中级工", "0503", "机关高级工",
                null, null, "同序列职务变化", "2022.09");

        assertThat(row.pendingAppointmentChange()).isFalse();
    }
}
