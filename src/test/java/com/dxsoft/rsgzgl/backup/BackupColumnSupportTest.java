package com.dxsoft.rsgzgl.backup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BackupColumnSupportTest {

    @Test
    void missingRequiredInsertColumnsSkipsNullableAutoIncrementAndDefaultedColumns() {
        Map<String, BackupColumnSupport.ColumnMeta> target = new LinkedHashMap<>();
        target.put("uid", new BackupColumnSupport.ColumnMeta(Types.INTEGER, false, true, false, false));
        target.put("dwbm", new BackupColumnSupport.ColumnMeta(Types.VARCHAR, false, false, false, false));
        target.put("bbz", new BackupColumnSupport.ColumnMeta(Types.VARCHAR, true, false, false, false));
        target.put("yctxsj", new BackupColumnSupport.ColumnMeta(Types.INTEGER, false, false, false, false));
        target.put("tc", new BackupColumnSupport.ColumnMeta(Types.VARCHAR, false, false, false, true));

        List<String> missing = BackupColumnSupport.missingRequiredInsertColumns(target, Set.of("dwbm", "tc"));

        assertEquals(List.of("yctxsj"), missing);
    }

    @Test
    void coerceForInsertFillsRequiredIntegerWithZero() {
        BackupColumnSupport.ColumnMeta meta =
                new BackupColumnSupport.ColumnMeta(Types.INTEGER, false, false, false, false);

        assertEquals(0, BackupColumnSupport.coerceForInsert(null, meta));
    }

    @Test
    void coerceForInsertFillsRequiredStringWithEmptyString() {
        BackupColumnSupport.ColumnMeta meta =
                new BackupColumnSupport.ColumnMeta(Types.VARCHAR, false, false, false, false);

        assertEquals("", BackupColumnSupport.coerceForInsert(null, meta));
        assertTrue(((String) BackupColumnSupport.coerceForInsert(null, meta)).isEmpty());
    }
}
