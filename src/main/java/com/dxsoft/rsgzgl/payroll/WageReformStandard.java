package com.dxsoft.rsgzgl.payroll;

public record WageReformStandard(
        String positionCode,
        int appointmentYearsLower,
        int appointmentYearsUpper,
        int reformYearsLower,
        int reformYearsUpper,
        String convertedLevel,
        String convertedStep
) {
}
