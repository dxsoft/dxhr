package com.dxsoft.rsgzgl.personnel;

public record PersonnelSummary(
        Integer uid,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String idCard,
        String gender,
        String birthYearMonth,
        String personnelCategory,
        String organizationType,
        String postCategory,
        String currentPosition,
        String currentPositionCode,
        String payrollPositionCode,
        String appointmentPosition,
        String approvalStatus,
        boolean retirementDue,
        boolean retirementWithinOneMonth,
        String calculatedRetirementMonth
) {
}
