package com.dxsoft.rsgzgl.payroll;

public record PgbcComparison(
        Integer storedAmount,
        Integer comparisonAmount,
        String treatment,
        String note
) {
}
