package com.dxsoft.rsgzgl.personnel;

public record PersonnelTransferRecord(
        Long id,
        Integer personUid,
        String idCard,
        String personName,
        String sourceOrganizationCode,
        String sourceOrganizationName,
        String sourcePersonCode,
        String targetOrganizationCode,
        String targetOrganizationName,
        String targetPersonCode,
        String transferPeriod,
        String changeType,
        String remark,
        String createdAt
) {
}
