package com.dxsoft.rsgzgl.payroll;

public record RegularizationPreview(
        String payrollHistoryId,
        String organizationCode,
        String personCode,
        String name,
        String calculationPeriod,
        String probationPositionCode,
        String probationPositionName,
        String educationCode,
        String educationName,
        String graduationDate,
        String regularPositionCode,
        String regularPositionName,
        String regularLevel,
        String regularStep,
        Integer currentSalary,
        Integer regularPositionSalary,
        Integer regularBaseSalary,
        Integer totalRegularSalary,
        Integer increaseAmount,
        Boolean eligible,
        String note,
        Boolean applyEligible,
        Boolean rollbackEligible
) {
}
