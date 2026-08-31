package com.dxsoft.rsgzgl.payroll;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RankAllowanceCancellationSupportTest {

    @Test
    void recognizesEmptyPoliceRankWithExplicitCategory() {
        assertThat(RankAllowanceCancellationSupport.isCancellation("jx", "", "jx")).isTrue();
        assertThat(RankAllowanceCancellationSupport.isCancellation("jx", "   ", "jx")).isTrue();
    }

    @Test
    void recognizesSentinelValuesWithExplicitCategory() {
        assertThat(RankAllowanceCancellationSupport.isCancellation("jx", "无", "jx")).isTrue();
        assertThat(RankAllowanceCancellationSupport.isCancellation("jx", "无警衔", "jx")).isTrue();
        assertThat(RankAllowanceCancellationSupport.isCancellation("jc", "无等级", "jc")).isTrue();
    }

    @Test
    void rejectsMissingCategoryLabel() {
        assertThat(RankAllowanceCancellationSupport.isCancellation("jx", "", "")).isFalse();
        assertThat(RankAllowanceCancellationSupport.isCancellation("jx", "无", "")).isFalse();
    }

    @Test
    void rejectsNormalRankNames() {
        assertThat(RankAllowanceCancellationSupport.isCancellation("jx", "三级警督", "jx")).isFalse();
        assertThat(RankAllowanceCancellationSupport.isCancellation("jc", "四级检察官", "jc")).isFalse();
    }
}
