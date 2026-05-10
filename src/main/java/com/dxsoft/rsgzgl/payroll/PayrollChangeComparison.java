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
        String previousCalculationPeriod,
        String previousChangeType,
        List<PayrollChangeComponentComparison> components
) {
}
