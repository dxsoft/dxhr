package com.dxsoft.rsgzgl.backup;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Precomputed CSV header lookup for restore (column name → actual CSV header).
 */
final class BackupCsvColumnMapping {

    private BackupCsvColumnMapping() {
    }

    static Map<String, String> buildColumnHeaderMap(List<String> headers, List<String> insertColumns) {
        Map<String, String> columnToHeader = new LinkedHashMap<>();
        for (String column : insertColumns) {
            for (String header : headers) {
                if (header.equalsIgnoreCase(column)) {
                    columnToHeader.put(column, header);
                    break;
                }
            }
        }
        return columnToHeader;
    }

    static String readRawValue(
            org.apache.commons.csv.CSVRecord record,
            String column,
            Map<String, String> columnToHeader,
            java.util.Set<String> sourceColumns) {
        String header = columnToHeader.get(column);
        if (header == null) {
            return null;
        }
        if (!sourceColumns.contains(column.toLowerCase(Locale.ROOT))) {
            return null;
        }
        return record.isMapped(header) ? record.get(header) : null;
    }
}
