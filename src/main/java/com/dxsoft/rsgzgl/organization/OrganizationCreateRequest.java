package com.dxsoft.rsgzgl.organization;

public record OrganizationCreateRequest(
        String organizationCode,
        String name,
        String shortName,
        String property,
        String category,
        String payrollCategory,
        String allowanceStandard) {
}
