package com.dxsoft.rsgzgl.payroll;

public record RaiseGradeStandard(
        String positionCode,
        int appointmentYearsLower,
        int appointmentYearsUpper,
        int raiseYearsLower,
        int raiseYearsUpper,
        String gradeLevel,
        String gradeStep
) {
}
