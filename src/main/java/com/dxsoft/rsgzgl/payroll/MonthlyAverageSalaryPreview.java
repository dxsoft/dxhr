package com.dxsoft.rsgzgl.payroll;

public record MonthlyAverageSalaryPreview(
        String payrollHistoryId,
        String organizationCode,
        String personCode,
        String name,
        String calculationYear,
        Integer weightedSalaryTotal,
        Integer countedMonths,
        Integer calculatedAverageSalary,
        Integer storedAverageSalary,
        String yearEndPositionCode,
        String yearEndPositionName,
        String yearEndLevel,
        String yearEndStep,
        Integer yearEndPositionSalary,
        Integer yearEndGradeSalary,
        String note,
        Boolean applyEligible,
        Boolean rollbackEligible) {
}
