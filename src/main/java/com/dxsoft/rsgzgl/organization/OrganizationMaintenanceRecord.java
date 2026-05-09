package com.dxsoft.rsgzgl.organization;

public record OrganizationMaintenanceRecord(
        Integer id,
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
        Integer activePersonnelCount,
        String organizationLevel,
        Integer performanceAllowanceEnabled,
        Integer performanceCategory,
        Integer yearAllowanceCategory,
        String financeSource,
        String housingFundWithheld,
        String pensionWithheld
) {
}
