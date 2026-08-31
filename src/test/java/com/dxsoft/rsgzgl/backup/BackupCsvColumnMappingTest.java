package com.dxsoft.rsgzgl.backup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.Test;

class BackupCsvColumnMappingTest {

    @Test
    void buildColumnHeaderMapMatchesCaseInsensitively() {
        Map<String, String> map = BackupCsvColumnMapping.buildColumnHeaderMap(
                List.of("UID", "Xm", "CJGZNY"),
                List.of("uid", "xm", "cjgzny", "zzny"));

        assertEquals("UID", map.get("uid"));
        assertEquals("Xm", map.get("xm"));
        assertEquals("CJGZNY", map.get("cjgzny"));
        assertNull(map.get("zzny"));
    }

    @Test
    void readRawValueUsesPrecomputedHeader() throws Exception {
        Map<String, String> map = BackupCsvColumnMapping.buildColumnHeaderMap(
                List.of("UID", "XM"),
                List.of("uid", "xm"));
        try (CSVParser parser = CSVFormat.DEFAULT.builder()
                .setHeader("UID", "XM")
                .setSkipHeaderRecord(false)
                .build()
                .parse(new StringReader("1,张三\n"))) {
            var record = parser.iterator().next();
            assertEquals("1", BackupCsvColumnMapping.readRawValue(record, "uid", map, Set.of("uid", "xm")));
            assertEquals("张三", BackupCsvColumnMapping.readRawValue(record, "xm", map, Set.of("uid", "xm")));
            assertNull(BackupCsvColumnMapping.readRawValue(record, "missing", map, Set.of("uid", "xm")));
        }
    }
}
