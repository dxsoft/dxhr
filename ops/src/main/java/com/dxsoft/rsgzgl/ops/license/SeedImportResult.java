package com.dxsoft.rsgzgl.ops.license;

public record SeedImportResult(
        int organizationsSaved,
        String format,
        boolean localPolicySynced
) {
}
