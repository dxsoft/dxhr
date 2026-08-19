package com.dxsoft.rsgzgl.personnel;

public record PersonnelMaintenanceRequest(
        String organizationCode,
        String personCode,
        String name,
        String idCard,
        String gender,
        String birthYearMonth,
        String personnelCategory,
        String organizationType,
        String postCategory,
        String workStartYearMonth,
        String regularizationYearMonth,
        Integer salaryYears,
        String educationCode,
        String highestEducation,
        String currentPositionLevel,
        String currentRankCode,
        String currentPosition,
        String currentPositionStartYearMonth,
        String ethnicity,
        String politicalStatus,
        String archiveNumber,
        String joinYearMonth,
        String joinType
) {
}
