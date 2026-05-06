package com.dxsoft.rsgzgl.organization;

public record OrganizationSummary(
        Integer id,
        String organizationCode,
        String name,
        String shortName,
        String category,
        String property,
        Integer personnelQuota,
        Integer activePersonnelCount
) {
}
