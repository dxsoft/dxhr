package com.dxsoft.rsgzgl.license;

import java.util.List;

public record LicenseIssuePreview(
        String organizationCode,
        String organizationName,
        String organizationLevel,
        String city,
        int organizationCount,
        List<String> organizationCodes,
        boolean includeSubordinates,
        boolean includeAllOrganizations
) {
}
