package com.dxsoft.rsgzgl.payroll;

public record RegularizationHighGradePreview(
        String payrollHistoryId,
        String organizationCode,
        String personCode,
        String name,
        String calculationPeriod,
        String regularizationPeriod,
        String positionCode,
        String positionName,
        String educationCode,
        String educationName,
        Integer highGradeIncrement,
        String baseStep,
        String currentStep,
        String targetStep,
        Integer currentGradeSalary,
        Integer targetGradeSalary,
        Integer increaseAmount,
        String note,
        Boolean applyEligible,
        Boolean rollbackEligible) {
}
