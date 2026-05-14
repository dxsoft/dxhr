package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record PayrollChangeComparison(
        String payrollHistoryId,
        String previousPayrollHistoryId,
        String organizationCode,
        String personCode,
        String name,
        String calculationPeriod,
        String changeType,
        String previousPositionName,
        String currentPositionName,
        String previousGradeLevel,
        String currentGradeLevel,
        String previousStepOrSalaryLevel,
        String currentStepOrSalaryLevel,
        String previousCalculationPeriod,
        String previousChangeType,
        List<PayrollChangeComponentComparison> components
) {
}
