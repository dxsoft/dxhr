package com.dxsoft.rsgzgl.setup;

import java.util.Map;

public record SystemInitializationPreview(
        Map<String, Long> tableCounts,
        long totalPersonnelRecords,
        long organizationCount,
        long subjectCount,
        long licenseCount,
        boolean clearOrganizationsAndLicense,
        String warningMessage) {
}
