package com.dxsoft.rsgzgl.payroll;

public record WageReformPosition(
        String positionCode,
        String positionName,
        String startYearMonth,
        Integer interruptedYears
) {
}
