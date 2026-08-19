package com.dxsoft.rsgzgl.backup;

import java.util.List;
import java.util.Map;

public record BackupRestoreResult(
        BackupFormat format,
        String formatLabel,
        int tablesRestored,
        int rowsRestored,
        List<String> restoredTables,
        List<String> skippedTables,
        Map<String, Integer> rowCounts,
        String message
) {
}
