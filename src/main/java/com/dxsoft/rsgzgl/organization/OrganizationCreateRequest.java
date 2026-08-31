package com.dxsoft.rsgzgl.organization;

public record OrganizationCreateRequest(
        String organizationCode,
        String name,
        String shortName,
        String property,
        String category,
        String payrollCategory,
        String allowanceStandard,
        Integer personnelQuota,
        Integer establishmentCount,
        Integer actualCount,
        String organizationLevel,
        String systemCategory,
        Integer performanceAllowanceEnabled,
        Integer performanceCategory,
        String performanceRatio,
        Integer yearAllowanceCategory,
        String financeSource,
        String housingFundWithheld,
        String pensionWithheld
) {
}
