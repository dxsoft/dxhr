package com.dxsoft.rsgzgl.payroll;

public record PayrollHistoryMaintenanceRequest(
        String calculationYear,
        String calculationMonth,
        String changeType,
        String positionCode,
        String positionName,
        Integer positionSalary,
        Integer gradeSalary,
        Integer technicalGradeSalary,
        Integer performanceAllowance,
        Integer retainedAllowance,
        Integer totalAmount
) {
}
