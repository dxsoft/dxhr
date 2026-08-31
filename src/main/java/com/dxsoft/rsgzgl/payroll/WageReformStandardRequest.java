package com.dxsoft.rsgzgl.payroll;

public record WageReformStandardRequest(
        String positionCode,
        Integer appointmentYearsLower,
        Integer appointmentYearsUpper,
        Integer reformYearsLower,
        Integer reformYearsUpper,
        String convertedLevel,
        String convertedStep
) {
}
