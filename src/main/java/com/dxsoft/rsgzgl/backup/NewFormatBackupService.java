package com.dxsoft.rsgzgl.backup;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.sql.DataSource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

@Service
class NewFormatBackupService {

    private static final int EXPORT_FETCH_SIZE = 500;
    private static final int EXPORT_PARALLELISM = 2;
    private static final DateTimeFormatter TS = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final DataSource dataSource;
    private final BackupRestoreProperties restoreProperties;

    NewFormatBackupService(DataSource dataSource, BackupRestoreProperties restoreProperties) {
        this.dataSource = dataSource;
        this.restoreProperties = restoreProperties;
    }

    /**
     * Build a backup archive on disk (caller deletes the returned path after streaming).
     */
    Path createBackupArchive(Collection<String> scopeIds) throws IOException, SQLException {
        Path zipPath = Files.createTempFile("rsgzgl-bak-", ".rsbak");
        Path workDir = Files.createTempDirectory("rsgzgl-bak-new-");
        try {
            Path tablesDir = workDir.resolve("tables");
            Files.createDirectories(tablesDir);
            List<String> allTables;
            try (Connection connection = dataSource.getConnection()) {
                allTables = listBaseTables(connection);
            }
            List<String> tables = BackupTableScopes.resolveTables(scopeIds, allTables);
            if (tables.isEmpty()) {
                throw new IllegalArgumentException("所选范围在当前库中没有可导出的表。");
            }
            exportTablesParallel(tables, tablesDir);

            String scopesJson = BackupTableScopes.normalizeScopeIds(scopeIds).stream()
                    .map(id -> "\"" + id + "\"")
                    .collect(Collectors.joining(","));
            if (scopesJson.isBlank()) {
                scopesJson = "\"" + BackupTableScopes.ALL + "\"";
            }
            String tablesJson = tables.stream()
                    .map(name -> "\"" + name.replace("\"", "\\\"") + "\"")
                    .collect(Collectors.joining(","));
            String manifest = """
                    {
                      "format": "%s",
                      "createdAt": "%s",
                      "tableCount": %d,
                      "scopes": [%s],
                      "tables": [%s],
                      "encoding": "UTF-8",
                      "csvDir": "tables"
                    }
                    """.formatted(
                    BackupPackageInspector.NEW_FORMAT_ID,
                    Instant.now().toString(),
                    tables.size(),
                    scopesJson,
                    tablesJson);
            Files.writeString(workDir.resolve(BackupPackageInspector.NEW_MANIFEST), manifest, StandardCharsets.UTF_8);
            Files.writeString(
                    workDir.resolve(BackupPackageInspector.NEW_MARKER),
                    BackupPackageInspector.NEW_FORMAT_ID,
                    StandardCharsets.UTF_8);

            zipDirectory(workDir, zipPath, List.of(
                    BackupPackageInspector.NEW_MANIFEST,
                    BackupPackageInspector.NEW_MARKER,
                    "tables"));
            return zipPath;
        } catch (IOException | SQLException | RuntimeException ex) {
            Files.deleteIfExists(zipPath);
            throw ex;
        } finally {
            BackupPackageInspector.deleteRecursively(workDir);
        }
    }

    private void exportTablesParallel(List<String> tables, Path tablesDir) throws SQLException, IOException {
        if (tables.size() <= 1) {
            try (Connection connection = dataSource.getConnection()) {
                for (String table : tables) {
                    exportTableCsv(connection, table, tablesDir.resolve(table + ".csv"));
                }
            }
            return;
        }
        ExecutorService pool = Executors.newFixedThreadPool(Math.min(EXPORT_PARALLELISM, tables.size()));
        try {
            List<Future<?>> futures = new ArrayList<>(tables.size());
            for (String table : tables) {
                futures.add(pool.submit(() -> {
                    try (Connection connection = dataSource.getConnection()) {
                        exportTableCsv(connection, table, tablesDir.resolve(table + ".csv"));
                    }
                    return null;
                }));
            }
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IOException("导出被中断", ex);
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    if (cause instanceof SQLException sqlEx) {
                        throw sqlEx;
                    }
                    if (cause instanceof IOException ioEx) {
                        throw ioEx;
                    }
                    throw new SQLException("导出表失败: " + cause.getMessage(), cause);
                }
            }
        } finally {
            pool.shutdownNow();
        }
    }

    BackupRestoreResult restore(Path extractDir, Collection<String> scopeIds) throws IOException, SQLException {
        long startedAt = System.currentTimeMillis();
        Path tablesDir = extractDir.resolve("tables");
        if (!Files.isDirectory(tablesDir)) {
            throw new IllegalArgumentException("新系统备份缺少 tables/ 目录。");
        }
        List<String> csvFiles = Files.list(tablesDir)
                .filter(Files::isRegularFile)
                .map(p -> p.getFileName().toString())
                .filter(name -> name.toLowerCase(Locale.ROOT).endsWith(".csv"))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        List<String> packageTables = csvFiles.stream()
                .map(name -> name.substring(0, name.length() - 4))
                .toList();
        List<String> selected = BackupTableScopes.resolveTables(scopeIds, packageTables);
        var selectedSet = selected.stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        Map<String, Integer> rowCounts = new LinkedHashMap<>();
        List<String> restored = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        int totalRows = 0;

        Connection connection = BackupJdbcSupport.openRestoreConnectionWithRetry(dataSource);
        try {
            for (String csvName : csvFiles) {
                String tableName = csvName.substring(0, csvName.length() - 4);
                if (!selectedSet.contains(tableName.toLowerCase(Locale.ROOT))) {
                    skipped.add(tableName + "（未选择该分组）");
                    continue;
                }
                try {
                    String resolvedTable = BackupTableNameSupport.resolveTableName(connection, tableName);
                    if (resolvedTable == null) {
                        skipped.add(tableName + "（目标库无此表）");
                        continue;
                    }
                    int rows = restoreTableCsv(connection, resolvedTable, tablesDir.resolve(csvName));
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
                        int rows = restoreTableCsv(connection, resolvedTable, tablesDir.resolve(csvName));
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
        String scopeLabel = describeScopes(scopeIds);
        return new BackupRestoreResult(
                BackupFormat.NEW,
                "新系统备份",
                restored.size(),
                totalRows,
                restored,
                skipped,
                rowCounts,
                durationMs,
                "新系统备份恢复完成（" + scopeLabel + "）：已恢复 " + restored.size() + " 张表，共 " + totalRows
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

    private static String describeScopes(Collection<String> scopeIds) {
        var scopes = BackupTableScopes.normalizeScopeIds(scopeIds);
        if (scopes.isEmpty() || scopes.contains(BackupTableScopes.ALL)) {
            return "全部表";
        }
        return scopes.stream()
                .map(BackupTableScopes::labelOf)
                .collect(Collectors.joining("、"));
    }

    private void exportTableCsv(Connection connection, String tableName, Path csvPath)
            throws SQLException, IOException {
        try (var st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            st.setFetchSize(EXPORT_FETCH_SIZE);
            try (ResultSet rs = st.executeQuery("SELECT * FROM `" + BackupColumnSupport.sanitizeIdent(tableName) + "`");
                 BufferedWriter writer = Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8);
                 CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();
                List<String> headers = new ArrayList<>(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    headers.add(meta.getColumnLabel(i));
                }
                printer.printRecord(headers);
                while (rs.next()) {
                    List<Object> values = new ArrayList<>(columnCount);
                    for (int i = 1; i <= columnCount; i++) {
                        values.add(serializeCell(rs, i, meta.getColumnType(i)));
                    }
                    printer.printRecord(values);
                }
            }
        }
    }

    private int restoreTableCsv(Connection connection, String tableName, Path csvPath)
            throws IOException, SQLException {
        Map<String, BackupColumnSupport.ColumnMeta> targetColumns =
                BackupColumnSupport.loadColumns(connection, tableName);
        try (Reader reader = new InputStreamReader(Files.newInputStream(csvPath), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {
            List<String> headers = parser.getHeaderNames().stream()
                    .map(h -> h == null ? "" : h.trim())
                    .toList();
            List<String> insertColumns = new ArrayList<>();
            Set<String> sourceColumns = new HashSet<>();
            for (String header : headers) {
                String key = header.toLowerCase(Locale.ROOT);
                if (targetColumns.containsKey(key)) {
                    insertColumns.add(key);
                    sourceColumns.add(key);
                }
            }
            for (String column : BackupColumnSupport.missingRequiredInsertColumns(targetColumns, sourceColumns)) {
                insertColumns.add(column);
            }
            if (insertColumns.isEmpty()) {
                throw new IllegalStateException("表 " + tableName + " 与 CSV 无共同字段。");
            }

            Map<String, String> columnToHeader =
                    BackupCsvColumnMapping.buildColumnHeaderMap(headers, insertColumns);
            BackupTableClearSupport.clearTable(
                    connection, tableName, restoreProperties.truncateBeforeInsert());

            String placeholders = String.join(",", insertColumns.stream().map(c -> "?").toList());
            String columnSql = String.join(",", insertColumns.stream().map(c -> "`" + c + "`").toList());
            String sql = "INSERT INTO `" + BackupColumnSupport.sanitizeIdent(tableName) + "` (" + columnSql
                    + ") VALUES (" + placeholders + ")";

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                BackupRestoreBatchWriter batchWriter =
                        new BackupRestoreBatchWriter(connection, ps, restoreProperties);
                for (CSVRecord record : parser) {
                    for (int i = 0; i < insertColumns.size(); i++) {
                        String column = insertColumns.get(i);
                        BackupColumnSupport.ColumnMeta meta = targetColumns.get(column);
                        String raw = BackupCsvColumnMapping.readRawValue(
                                record, column, columnToHeader, sourceColumns);
                        Object value = BackupColumnSupport.coerceForInsert(
                                raw == null && !sourceColumns.contains(column)
                                        ? null
                                        : deserializeCell(raw, meta.sqlType()),
                                meta);
                        if (value == null) {
                            ps.setNull(i + 1, Types.NULL);
                        } else {
                            ps.setObject(i + 1, value);
                        }
                    }
                    try {
                        batchWriter.addBatch();
                    } catch (SQLException ex) {
                        throw wrapSql(tableName, batchWriter.totalRows(), ex);
                    }
                }
                try {
                    return batchWriter.finish();
                } catch (SQLException ex) {
                    throw wrapSql(tableName, batchWriter.totalRows(), ex);
                }
            }
        }
    }

    private static SQLException wrapSql(String tableName, int rows, SQLException ex) {
        return new SQLException("恢复表 " + tableName + " 失败（约第 " + rows + " 行）: " + ex.getMessage(), ex);
    }

    private Object serializeCell(ResultSet rs, int index, int sqlType) throws SQLException {
        Object value = rs.getObject(index);
        if (value == null || rs.wasNull()) {
            return "";
        }
        if (value instanceof byte[] bytes) {
            return "base64:" + Base64.getEncoder().encodeToString(bytes);
        }
        if (value instanceof Blob blob) {
            byte[] bytes = blob.getBytes(1, (int) Math.min(blob.length(), Integer.MAX_VALUE));
            return "base64:" + Base64.getEncoder().encodeToString(bytes);
        }
        if (value instanceof Clob clob) {
            return clob.getSubString(1, (int) Math.min(clob.length(), Integer.MAX_VALUE));
        }
        if (value instanceof LocalDateTime ldt) {
            return TS.format(ldt);
        }
        if (value instanceof LocalDate ld) {
            return ld.toString();
        }
        if (value instanceof java.sql.Timestamp ts) {
            return TS.format(ts.toLocalDateTime());
        }
        if (value instanceof java.sql.Date d) {
            return d.toLocalDate().toString();
        }
        if (value instanceof Boolean bool) {
            return bool ? "1" : "0";
        }
        return String.valueOf(value);
    }

    private Object deserializeCell(String raw, int sqlType) {
        if (raw == null) {
            return null;
        }
        String text = raw.trim();
        if (text.isEmpty()) {
            return null;
        }
        if (text.startsWith("base64:")) {
            return Base64.getDecoder().decode(text.substring("base64:".length()));
        }
        return switch (sqlType) {
            case Types.TINYINT, Types.SMALLINT, Types.INTEGER -> Integer.valueOf(text);
            case Types.BIGINT -> Long.valueOf(text);
            case Types.DECIMAL, Types.NUMERIC -> new BigDecimal(text);
            case Types.DOUBLE, Types.FLOAT, Types.REAL -> Double.valueOf(text);
            case Types.BOOLEAN, Types.BIT -> "1".equals(text) || "true".equalsIgnoreCase(text) || "T".equalsIgnoreCase(text);
            case Types.DATE -> LocalDate.parse(text.length() > 10 ? text.substring(0, 10) : text);
            case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE -> {
                if (text.endsWith("Z")) {
                    yield LocalDateTime.ofInstant(Instant.parse(text), ZoneOffset.systemDefault());
                }
                yield LocalDateTime.parse(text.contains("T") ? text : text.replace(' ', 'T'));
            }
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> {
                if (text.startsWith("0x") || text.startsWith("0X")) {
                    yield HexFormat.of().parseHex(text.substring(2));
                }
                yield text.getBytes(StandardCharsets.UTF_8);
            }
            default -> text;
        };
    }

    private List<String> listBaseTables(Connection connection) throws SQLException {
        List<String> tables = new ArrayList<>();
        String catalog = connection.getCatalog();
        try (ResultSet rs = connection.getMetaData().getTables(catalog, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                String name = rs.getString("TABLE_NAME");
                if (name != null && !name.isBlank()) {
                    tables.add(name);
                }
            }
        }
        tables.sort(String.CASE_INSENSITIVE_ORDER);
        return tables;
    }

    private void zipDirectory(Path workDir, Path zipPath, List<String> roots) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            zos.setLevel(Deflater.BEST_SPEED);
            for (String rootName : roots) {
                Path root = workDir.resolve(rootName);
                if (Files.isRegularFile(root)) {
                    zos.putNextEntry(new ZipEntry(rootName));
                    Files.copy(root, zos);
                    zos.closeEntry();
                    continue;
                }
                if (!Files.isDirectory(root)) {
                    continue;
                }
                try (var walk = Files.walk(root)) {
                    for (Path file : walk.filter(Files::isRegularFile).toList()) {
                        String entryName = workDir.relativize(file).toString().replace('\\', '/');
                        zos.putNextEntry(new ZipEntry(entryName));
                        Files.copy(file, zos);
                        zos.closeEntry();
                    }
                }
            }
        }
    }
}
