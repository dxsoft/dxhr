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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
    private static final int BATCH_SIZE = 200;

    private final DataSource dataSource;

    LegacyDbfRestoreService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    BackupRestoreResult restore(Path extractDir, Collection<String> scopeIds) throws IOException, SQLException {
        List<String> tableNames = BackupPackageInspector.collectDbfTableNames(extractDir);
        List<String> selected = BackupTableScopes.resolveTables(scopeIds, tableNames);
        var selectedSet = selected.stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
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
                Path dbf = findDbf(extractDir, tableName + "2.dbf");
                if (dbf == null) {
                    skipped.add(tableName + "（缺少 " + tableName + "2.dbf）");
                    continue;
                }
                try {
                    if (!tableExists(connection, tableName)) {
                        skipped.add(tableName + "（目标库无此表）");
                        continue;
                    }
                    int rows = restoreTable(connection, tableName, dbf);
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
                        if (!tableExists(connection, tableName)) {
                            skipped.add(tableName + "（目标库无此表）");
                            continue;
                        }
                        int rows = restoreTable(connection, tableName, dbf);
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
                "旧系统备份恢复完成（" + scopeLabel + "）：已恢复 " + restored.size() + " 张表，共 " + totalRows + " 行。");
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
            for (int i = 0; i < fieldCount; i++) {
                DBFField field = reader.getField(i);
                String fieldName = field.getName().trim().toLowerCase(Locale.ROOT);
                if (targetColumns.containsKey(fieldName)) {
                    insertColumns.add(fieldName);
                    fieldIndexes.add(i);
                }
            }
            if (insertColumns.isEmpty()) {
                throw new IllegalStateException("表 " + tableName + " 与 DBF 无共同字段，无法恢复。");
            }

            try (var st = connection.createStatement()) {
                st.execute("DELETE FROM `" + BackupColumnSupport.sanitizeIdent(tableName) + "`");
            }

            String placeholders = String.join(",", insertColumns.stream().map(c -> "?").toList());
            String columnSql = String.join(",", insertColumns.stream().map(c -> "`" + c + "`").toList());
            String sql = "INSERT INTO `" + BackupColumnSupport.sanitizeIdent(tableName) + "` (" + columnSql
                    + ") VALUES (" + placeholders + ")";

            int rows = 0;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                Object[] record;
                while ((record = reader.nextRecord()) != null) {
                    for (int col = 0; col < insertColumns.size(); col++) {
                        int fieldIndex = fieldIndexes.get(col);
                        DBFField field = reader.getField(fieldIndex);
                        BackupColumnSupport.ColumnMeta meta = targetColumns.get(insertColumns.get(col));
                        Object value = BackupColumnSupport.coerceForInsert(
                                normalizeValue(field, record[fieldIndex], meta.sqlType()), meta);
                        if (value == null) {
                            ps.setNull(col + 1, Types.NULL);
                        } else {
                            ps.setObject(col + 1, value);
                        }
                    }
                    ps.addBatch();
                    rows++;
                    if (rows % BATCH_SIZE == 0) {
                        ps.executeBatch();
                        ps.clearBatch();
                        connection.commit();
                    }
                }
                if (rows % BATCH_SIZE != 0) {
                    ps.executeBatch();
                    connection.commit();
                }
            } catch (SQLException ex) {
                throw new SQLException("恢复表 " + tableName + " 失败（约第 " + rows + " 行）: " + ex.getMessage(), ex);
            }
            return rows;
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

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        String safe = BackupColumnSupport.sanitizeIdent(tableName);
        if (tableExistsExact(connection, safe)) {
            return true;
        }
        if (tableExistsExact(connection, safe.toLowerCase(Locale.ROOT))) {
            return true;
        }
        return tableExistsExact(connection, safe.toUpperCase(Locale.ROOT));
    }

    private boolean tableExistsExact(Connection connection, String tableName) throws SQLException {
        try (var st = connection.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = '"
                             + tableName + "' LIMIT 1")) {
            return rs.next();
        } catch (SQLException ex) {
            try (ResultSet rs = connection.getMetaData().getTables(
                    connection.getCatalog(), null, tableName, new String[]{"TABLE"})) {
                return rs.next();
            }
        }
    }

    private Path findDbf(Path extractDir, String fileName) throws IOException {
        try (var stream = Files.walk(extractDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equalsIgnoreCase(fileName))
                    .findFirst()
                    .orElse(null);
        }
    }
}
