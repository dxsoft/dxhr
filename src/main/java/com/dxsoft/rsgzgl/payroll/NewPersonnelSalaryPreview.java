package com.dxsoft.rsgzgl.payroll;

public record NewPersonnelSalaryPreview(
        Integer uid,
        String payrollHistoryId,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String joinYearMonth,
        String joinType,
        String calculationPeriod,
        String positionCode,
        String positionName,
        String positionStartYearMonth,
        Integer calculatedTotal,
        String standardNote,
        Boolean eligible,
        Boolean applyEligible,
        Boolean rollbackEligible) {
}
