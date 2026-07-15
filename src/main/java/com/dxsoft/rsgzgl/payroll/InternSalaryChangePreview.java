package com.dxsoft.rsgzgl.payroll;

public record InternSalaryChangePreview(
        String payrollHistoryId,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String calculationPeriod,
        String workStartYearMonth,
        String regularizationYearMonth,
        String educationCode,
        String educationName,
        String positionCode,
        String positionName,
        String salaryStandardYearMonth,
        Integer storedInternSalary,
        Integer calculatedInternSalary,
        Integer differenceAmount,
        Integer currentTotal,
        Integer nextTotal,
        String standardNote,
        Boolean eligible,
        Boolean applyEligible,
        Boolean rollbackEligible) {
}
