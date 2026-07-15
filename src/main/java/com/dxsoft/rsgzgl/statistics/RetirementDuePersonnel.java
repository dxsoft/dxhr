package com.dxsoft.rsgzgl.statistics;

public record RetirementDuePersonnel(
        Integer uid,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String gender,
        String birthYearMonth,
        String positionCode,
        String positionName,
        String retirementCategory,
        Integer delayMonths,
        String calculatedRetirementMonth,
        String storedRetirementMonth,
        String referencePeriod) {
}
