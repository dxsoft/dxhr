package com.dxsoft.rsgzgl.backup;

import com.linuxense.javadbf.DBFDataType;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;

@Service
class LegacyDbfRestoreService {

    private static final Set<String> SKIP_TABLES = Set.of(
            "YHGL", "BZ06_ZW_GW", "BZ06_ZW_JB_XJ", "CYXX", "RPTINFO");
    private static final Charset DBF_CHARSET = Charset.forName("GBK");

    private final DataSource dataSource;
    private final BackupRestoreProperties restoreProperties;

    LegacyDbfRestoreService(DataSource dataSource, BackupRestoreProperties restoreProperties) {
        this.dataSource = dataSource;
        this.restoreProperties = restoreProperties;
    }

    BackupRestoreResult restore(Path extractDir, Collection<String> scopeIds) throws IOException, SQLException {
        long startedAt = System.currentTimeMillis();
        List<String> tableNames = BackupPackageInspector.collectDbfTableNames(extractDir);
        List<String> selected = BackupTableScopes.resolveTables(scopeIds, tableNames);
        var selectedSet = selected.stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        Map<String, Path> dbfIndex = indexDbfFiles(extractDir);
        Map<String, Integer> rowCounts = new LinkedHashMap<>();
        List<String> restored = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        int totalRows = 0;

        Connection connection = BackupJdbcSupport.openRestoreConnectionWithRetry(dataSource);
        try {
            for (String tableName : tableNames) {
                String upper = tableName.toUpperCase(Locale.ROOT);
                if (SKIP_TABLES.contains(upper)) {
                    skipped.add(tableName + "（旧系统恢复时跳过）");
                    continue;
                }
                if (!selectedSet.contains(tableName.toLowerCase(Locale.ROOT))) {
                    skipped.add(tableName + "（未选择该分组）");
                    continue;
                }
                Path dbf = dbfIndex.get((tableName + "2.dbf").toLowerCase(Locale.ROOT));
                if (dbf == null) {
                    skipped.add(tableName + "（缺少 " + tableName + "2.dbf）");
                    continue;
                }
                try {
                    String resolvedTable = BackupTableNameSupport.resolveTableName(connection, tableName);
                    if (resolvedTable == null) {
                        skipped.add(tableName + "（目标库无此表）");
                        continue;
                    }
                    int rows = restoreTable(connection, resolvedTable, dbf);
                    connection.commit();
                    rowCounts.put(tableName, rows);
                    restored.add(tableName);
                    totalRows += rows;
                } catch (Exception firstError) {
                    BackupJdbcSupport.safeRollback(connection);
                    if (!BackupJdbcSupport.isConnectionClosed(firstError)) {
                        throw firstError instanceof SQLException sqlEx
                                ? sqlEx
                                : new SQLException(BackupJdbcSupport.rootMessage(firstError), firstError);
                    }
                    BackupJdbcSupport.safeClose(connection);
                    connection = BackupJdbcSupport.openRestoreConnectionWithRetry(dataSource);
                    try {
                        String resolvedTable = BackupTableNameSupport.resolveTableName(connection, tableName);
                        if (resolvedTable == null) {
                            skipped.add(tableName + "（目标库无此表）");
                            continue;
                        }
                        int rows = restoreTable(connection, resolvedTable, dbf);
                        connection.commit();
                        rowCounts.put(tableName, rows);
                        restored.add(tableName);
                        totalRows += rows;
                    } catch (Exception retryError) {
                        BackupJdbcSupport.safeRollback(connection);
                        throw retryError instanceof SQLException sqlEx
                                ? sqlEx
                                : new SQLException(
                                        "恢复表 " + tableName + " 失败: " + BackupJdbcSupport.rootMessage(retryError),
                                        retryError);
                    }
                }
            }
            BackupJdbcSupport.finishRestoreSession(connection);
        } catch (Exception ex) {
            BackupJdbcSupport.safeRollback(connection);
            if (ex instanceof SQLException sqlEx) {
                throw sqlEx;
            }
            if (ex instanceof IOException ioEx) {
                throw ioEx;
            }
            throw new SQLException("数据恢复失败: " + BackupJdbcSupport.rootMessage(ex), ex);
        } finally {
            BackupJdbcSupport.safeClose(connection);
        }

        long durationMs = System.currentTimeMillis() - startedAt;
        var scopes = BackupTableScopes.normalizeScopeIds(scopeIds);
        String scopeLabel = scopes.isEmpty() || scopes.contains(BackupTableScopes.ALL)
                ? "全部表"
                : scopes.stream().map(BackupTableScopes::labelOf).collect(Collectors.joining("、"));
        return new BackupRestoreResult(
                BackupFormat.LEGACY,
                "旧系统备份",
                restored.size(),
                totalRows,
                restored,
                skipped,
                rowCounts,
                durationMs,
                "旧系统备份恢复完成（" + scopeLabel + "）：已恢复 " + restored.size() + " 张表，共 " + totalRows
                        + " 行，耗时 " + formatDuration(durationMs) + "。",
                List.of());
    }

    private static String formatDuration(long durationMs) {
        if (durationMs < 1000) {
            return durationMs + " 毫秒";
        }
        double seconds = durationMs / 1000.0;
        return seconds >= 10 ? String.format(Locale.ROOT, "%.0f 秒", seconds)
                : String.format(Locale.ROOT, "%.1f 秒", seconds);
    }

    private int restoreTable(Connection connection, String tableName, Path dbfPath)
            throws IOException, SQLException {
        Map<String, BackupColumnSupport.ColumnMeta> targetColumns =
                BackupColumnSupport.loadColumns(connection, tableName);
        try (InputStream in = new BufferedInputStream(Files.newInputStream(dbfPath));
             DBFReader reader = new DBFReader(in, DBF_CHARSET)) {
            int fieldCount = reader.getFieldCount();
            List<String> insertColumns = new ArrayList<>();
            List<Integer> fieldIndexes = new ArrayList<>();
            Set<String> sourceColumns = new HashSet<>();
            for (int i = 0; i < fieldCount; i++) {
                DBFField field = reader.getField(i);
                String fieldName = field.getName().trim().toLowerCase(Locale.ROOT);
                if (targetColumns.containsKey(fieldName)) {
                    insertColumns.add(fieldName);
                    fieldIndexes.add(i);
                    sourceColumns.add(fieldName);
                }
            }
            for (String column : BackupColumnSupport.missingRequiredInsertColumns(targetColumns, sourceColumns)) {
                insertColumns.add(column);
                fieldIndexes.add(-1);
            }
            if (insertColumns.isEmpty()) {
                throw new IllegalStateException("表 " + tableName + " 与 DBF 无共同字段，无法恢复。");
            }

            BackupTableClearSupport.clearTable(
                    connection, tableName, restoreProperties.truncateBeforeInsert());

            String placeholders = String.join(",", insertColumns.stream().map(c -> "?").toList());
            String columnSql = String.join(",", insertColumns.stream().map(c -> "`" + c + "`").toList());
            String sql = "INSERT INTO `" + BackupColumnSupport.sanitizeIdent(tableName) + "` (" + columnSql
                    + ") VALUES (" + placeholders + ")";

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                BackupRestoreBatchWriter batchWriter =
                        new BackupRestoreBatchWriter(connection, ps, restoreProperties);
                Object[] record;
                while ((record = reader.nextRecord()) != null) {
                    for (int col = 0; col < insertColumns.size(); col++) {
                        int fieldIndex = fieldIndexes.get(col);
                        BackupColumnSupport.ColumnMeta meta = targetColumns.get(insertColumns.get(col));
                        Object value;
                        if (fieldIndex < 0) {
                            value = BackupColumnSupport.coerceForInsert(null, meta);
                        } else {
                            DBFField field = reader.getField(fieldIndex);
                            value = BackupColumnSupport.coerceForInsert(
                                    normalizeValue(field, record[fieldIndex], meta.sqlType()), meta);
                        }
                        if (value == null) {
                            ps.setNull(col + 1, Types.NULL);
                        } else {
                            ps.setObject(col + 1, value);
                        }
                    }
                    try {
                        batchWriter.addBatch();
                    } catch (SQLException ex) {
                        throw new SQLException(
                                "恢复表 " + tableName + " 失败（约第 " + batchWriter.totalRows() + " 行）: "
                                        + ex.getMessage(),
                                ex);
                    }
                }
                try {
                    return batchWriter.finish();
                } catch (SQLException ex) {
                    throw new SQLException(
                            "恢复表 " + tableName + " 失败（约第 " + batchWriter.totalRows() + " 行）: "
                                    + ex.getMessage(),
                            ex);
                }
            }
        }
    }

    private Object normalizeValue(DBFField field, Object raw, int sqlType) {
        if (raw == null) {
            return null;
        }
        if (field.getType() == DBFDataType.LOGICAL) {
            boolean flag = raw instanceof Boolean b ? b : "T".equalsIgnoreCase(String.valueOf(raw));
            if (sqlType == Types.BOOLEAN || sqlType == Types.BIT || sqlType == Types.TINYINT
                    || sqlType == Types.SMALLINT || sqlType == Types.INTEGER) {
                return flag ? 1 : 0;
            }
            return flag ? "1" : "0";
        }
        if (raw instanceof String text) {
            String trimmed = text.trim();
            return trimmed.isEmpty() ? null : trimmed;
        }
        if (raw instanceof Date date) {
            if (sqlType == Types.DATE) {
                return new java.sql.Date(date.getTime());
            }
            if (sqlType == Types.TIMESTAMP || sqlType == Types.TIMESTAMP_WITH_TIMEZONE) {
                return new java.sql.Timestamp(date.getTime());
            }
            return new java.sql.Timestamp(date.getTime());
        }
        if (raw instanceof Number number) {
            if (sqlType == Types.DECIMAL || sqlType == Types.NUMERIC) {
                return BigDecimal.valueOf(number.doubleValue());
            }
            return number;
        }
        return raw;
    }

    private static Map<String, Path> indexDbfFiles(Path extractDir) throws IOException {
        Map<String, Path> index = new HashMap<>();
        try (var stream = Files.walk(extractDir)) {
            stream.filter(Files::isRegularFile).forEach(path -> index.putIfAbsent(
                    path.getFileName().toString().toLowerCase(Locale.ROOT), path));
        }
        return index;
    }
}
