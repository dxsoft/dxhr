package com.dxsoft.rsgzgl.setup;

import java.util.Map;

public record SystemInitializationPreview(
        Map<String, Long> tableCounts,
        long totalPersonnelRecords,
        String warningMessage) {
}
