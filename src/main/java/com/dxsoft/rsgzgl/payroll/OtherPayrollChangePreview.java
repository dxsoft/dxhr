package com.dxsoft.rsgzgl.payroll;

public record OtherPayrollChangePreview(
        String payrollHistoryId,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String calculationPeriod,
        String changeType,
        String positionCode,
        String positionName,
        Integer positionSalary,
        Integer gradeSalary,
        Integer technicalGradeSalary,
        Integer performanceAllowance,
        Integer retainedAllowance,
        Integer currentTotal,
        Boolean applyEligible,
        Boolean rollbackEligible) {
}
