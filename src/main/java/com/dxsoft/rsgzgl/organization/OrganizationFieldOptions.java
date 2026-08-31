package com.dxsoft.rsgzgl.organization;

import java.util.List;

public record OrganizationFieldOptions(
        List<OrganizationFieldOption> properties,
        List<OrganizationFieldOption> categories,
        List<OrganizationFieldOption> organizationLevels,
        List<OrganizationFieldOption> systemCategories,
        List<OrganizationFieldOption> payrollCategories,
        List<OrganizationFieldOption> allowanceStandards,
        List<OrganizationFieldOption> performanceEnabled,
        List<OrganizationFieldOption> performanceCategories,
        List<OrganizationFieldOption> yearAllowanceCategories,
        List<OrganizationFieldOption> financeSources,
        List<OrganizationFieldOption> housingFundWithheld,
        List<OrganizationFieldOption> pensionWithheld
) {
}
