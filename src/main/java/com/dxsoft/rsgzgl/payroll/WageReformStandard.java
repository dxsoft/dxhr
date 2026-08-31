package com.dxsoft.rsgzgl.payroll;

public record WageReformStandard(
        String positionCode,
        int appointmentYearsLower,
        int appointmentYearsUpper,
        int reformYearsLower,
        int reformYearsUpper,
        String convertedLevel,
        String convertedStep,
        String positionName
) {
    public WageReformStandard(
            String positionCode,
            int appointmentYearsLower,
            int appointmentYearsUpper,
            int reformYearsLower,
            int reformYearsUpper,
            String convertedLevel,
            String convertedStep) {
        this(positionCode, appointmentYearsLower, appointmentYearsUpper, reformYearsLower, reformYearsUpper,
                convertedLevel, convertedStep, null);
    }

    public WageReformStandard withPositionName(String name) {
        return new WageReformStandard(
                positionCode,
                appointmentYearsLower,
                appointmentYearsUpper,
                reformYearsLower,
                reformYearsUpper,
                convertedLevel,
                convertedStep,
                name);
    }
}
