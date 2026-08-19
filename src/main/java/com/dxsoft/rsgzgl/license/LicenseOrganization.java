package com.dxsoft.rsgzgl.license;

public record LicenseOrganization(
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
        Integer performanceAllowanceEnabled,
        Integer performanceCategory,
        Integer yearAllowanceCategory,
        String financeSource,
        String housingFundWithheld,
        String pensionWithheld
) {
}
