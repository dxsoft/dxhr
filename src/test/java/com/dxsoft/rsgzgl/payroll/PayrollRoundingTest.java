package com.dxsoft.rsgzgl.payroll;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PayrollRoundingTest {
    @Test
    void zroundUsesHalfUpWhenDecimalPlacesAreZero() {
        PayrollRoundingPolicy policy = PayrollRoundingPolicy.from("0", "0");
        assertThat(PayrollRounding.zroundToInt(new BigDecimal("12.4"), policy)).isEqualTo(12);
        assertThat(PayrollRounding.zroundToInt(new BigDecimal("12.5"), policy)).isEqualTo(13);
    }

    @Test
    void zroundUsesUpwardOffsetWhenRequested() {
        PayrollRoundingPolicy policy = PayrollRoundingPolicy.from("0", "1");
        assertThat(PayrollRounding.zroundToInt(new BigDecimal("12.1"), policy)).isEqualTo(13);
        assertThat(PayrollRounding.zroundToInt(new BigDecimal("12.0"), policy)).isEqualTo(12);
    }

    @Test
    void zroundUsesFloorWhenRequested() {
        PayrollRoundingPolicy policy = PayrollRoundingPolicy.from("0", "2");
        assertThat(PayrollRounding.zroundToInt(new BigDecimal("12.9"), policy)).isEqualTo(12);
    }

    @Test
    void zroundKeepsOneDecimalPlaceWhenConfigured() {
        PayrollRoundingPolicy policy = PayrollRoundingPolicy.from("1", "0");
        assertThat(PayrollRounding.zround(new BigDecimal("12.34"), policy)).isEqualByComparingTo("12.3");
        assertThat(PayrollRounding.zround(new BigDecimal("12.35"), policy)).isEqualByComparingTo("12.4");
    }

    @Test
    void zroundPercentUsesPolicyRounding() {
        PayrollRoundingPolicy policy = PayrollRoundingPolicy.from("0", "1");
        assertThat(PayrollRounding.zroundPercent(1705, 85, policy)).isEqualTo(1450);
    }
}
