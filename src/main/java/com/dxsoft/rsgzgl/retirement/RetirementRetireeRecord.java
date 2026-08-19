package com.dxsoft.rsgzgl.retirement;

public record RetirementRetireeRecord(
        Integer id,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String gender,
        String idCard,
        String retirementCategory,
        String retirementDate,
        String postCategory,
        String positionCode,
        String positionName,
        Integer salaryYears,
        Integer totalAmount,
        String approvalStatus,
        String sourceOrganizationCode,
        String sourcePersonCode) {
}
