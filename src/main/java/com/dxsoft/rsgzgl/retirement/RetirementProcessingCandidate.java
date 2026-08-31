package com.dxsoft.rsgzgl.retirement;

public record RetirementProcessingCandidate(
        Integer uid,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String gender,
        String birthYearMonth,
        String postCategory,
        String positionCode,
        String positionName,
        String workStartYearMonth,
        Integer salaryYears,
        String calculatedRetirementMonth,
        String retirementCategory,
        Integer currentTotal,
        Boolean alreadySeeded,
        String note) {
}
