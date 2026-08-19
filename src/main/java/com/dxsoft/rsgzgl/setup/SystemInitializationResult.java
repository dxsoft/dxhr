package com.dxsoft.rsgzgl.setup;

import java.util.Map;

public record SystemInitializationResult(
        Map<String, Integer> deletedCounts,
        int totalDeletedRows,
        boolean clearedOrganizationsAndLicense,
        String message) {
}
