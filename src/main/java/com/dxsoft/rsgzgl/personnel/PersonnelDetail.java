package com.dxsoft.rsgzgl.personnel;

public record PersonnelDetail(
        Integer uid,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String idCard,
        String gender,
        String birthMonth,
        String personnelType,
        String organizationProperty,
        String jobCategory,
        String workStartMonth,
        String regularizationMonth,
        String currentPositionLevel,
        String currentRankCode,
        String currentPosition,
        String currentPositionStartMonth,
        Integer salaryYears,
        String highestEducation,
        String retirementMonth
) {
}
