package com.dxsoft.rsgzgl.statistics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RetirementMonthCalculatorTest {

    @Test
    void calculatesMaleRetirementBeforeDelayThreshold() {
        RetirementMonthCalculator.CalculationResult result = RetirementMonthCalculator.calculate(
                "1964.06", "男", "0190");
        assertThat(result.retirementYearMonth()).isEqualTo("2024.06");
        assertThat(result.delayMonths()).isZero();
        assertThat(result.category()).isEqualTo(RetirementMonthCalculator.Category.MALE);
    }

    @Test
    void calculatesFemaleWorkerRetirementWithDelay() {
        RetirementMonthCalculator.CalculationResult result = RetirementMonthCalculator.calculate(
                "1976.03", "女", "0501");
        assertThat(result.category()).isEqualTo(RetirementMonthCalculator.Category.FEMALE_WORKER);
        assertThat(result.delayMonths()).isGreaterThan(0);
        assertThat(RetirementMonthCalculator.compareYearMonth(result.retirementYearMonth(), "2026.03")).isGreaterThan(0);
    }

    @Test
    void classifiesFemaleCadreForNonWorkerPosition() {
        RetirementMonthCalculator.CalculationResult result = RetirementMonthCalculator.calculate(
                "1970.01", "女", "0190");
        assertThat(result.category()).isEqualTo(RetirementMonthCalculator.Category.FEMALE_CADRE);
        assertThat(result.retirementYearMonth()).isEqualTo("2025.02");
        assertThat(result.delayMonths()).isEqualTo(1);
    }
}
