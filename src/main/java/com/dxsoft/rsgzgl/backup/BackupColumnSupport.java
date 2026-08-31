package com.dxsoft.rsgzgl.backup;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class BackupColumnSupport {

    record ColumnMeta(int sqlType, boolean nullable, boolean autoIncrement, boolean generated, boolean hasDefault) {
    }

    private BackupColumnSupport() {
    }

    static Map<String, ColumnMeta> loadColumns(Connection connection, String tableName) throws SQLException {
        String safe = sanitizeIdent(tableName);
        // Prefer information_schema / SELECT over DatabaseMetaData.getColumns — MySQL Connector/J
        // metadata ResultSets have caused "statement closed" issues under pooled connections.
        Map<String, ColumnMeta> columns = loadColumnsFromInformationSchema(connection, safe);
        if (columns.isEmpty()) {
            columns = loadColumnsFromInformationSchema(connection, safe.toLowerCase(Locale.ROOT));
        }
        if (columns.isEmpty()) {
            columns = loadColumnsFromInformationSchema(connection, safe.toUpperCase(Locale.ROOT));
        }
        if (columns.isEmpty()) {
            columns = loadColumnsFromSelect(connection, safe);
        }
        if (columns.isEmpty()) {
            throw new SQLException("无法读取表结构: " + tableName);
        }
        return columns;
    }

    private static Map<String, ColumnMeta> loadColumnsFromInformationSchema(Connection connection, String tableName)
            throws SQLException {
        Map<String, ColumnMeta> columns = new LinkedHashMap<>();
        String sql = """
                SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_TYPE, COLUMN_DEFAULT, EXTRA
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?
                ORDER BY ORDINAL_POSITION
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("COLUMN_NAME");
                    if (name == null || name.isBlank()) {
                        continue;
                    }
                    int sqlType = mapMysqlType(rs.getString("DATA_TYPE"), rs.getString("COLUMN_TYPE"));
                    boolean nullable = !"NO".equalsIgnoreCase(rs.getString("IS_NULLABLE"));
                    String extra = rs.getString("EXTRA");
                    String extraLower = extra == null ? "" : extra.toLowerCase(Locale.ROOT);
                    boolean autoIncrement = extraLower.contains("auto_increment");
                    boolean generated = extraLower.contains("generated");
                    boolean hasDefault = rs.getString("COLUMN_DEFAULT") != null;
                    columns.putIfAbsent(
                            name.toLowerCase(Locale.ROOT),
                            new ColumnMeta(sqlType, nullable, autoIncrement, generated, hasDefault));
                }
            }
        } catch (SQLException ex) {
            // H2 / engines without information_schema
            return Map.of();
        }
        return columns;
    }

    private static int mapMysqlType(String dataType, String columnType) {
        String type = dataType == null ? "" : dataType.toLowerCase(Locale.ROOT);
        String full = columnType == null ? type : columnType.toLowerCase(Locale.ROOT);
        return switch (type) {
            case "tinyint" -> full.contains("tinyint(1)") ? Types.BOOLEAN : Types.TINYINT;
            case "smallint" -> Types.SMALLINT;
            case "mediumint", "int", "integer" -> Types.INTEGER;
            case "bigint" -> Types.BIGINT;
            case "decimal", "numeric", "dec" -> Types.DECIMAL;
            case "float" -> Types.FLOAT;
            case "double", "real", "double precision" -> Types.DOUBLE;
            case "bit" -> Types.BIT;
            case "bool", "boolean" -> Types.BOOLEAN;
            case "date" -> Types.DATE;
            case "datetime", "timestamp" -> Types.TIMESTAMP;
            case "time" -> Types.TIME;
            case "year" -> Types.INTEGER;
            case "binary", "varbinary" -> Types.VARBINARY;
            case "tinyblob", "blob", "mediumblob", "longblob" -> Types.BLOB;
            case "tinytext", "text", "mediumtext", "longtext", "json", "enum", "set",
                 "char", "varchar" -> Types.VARCHAR;
            default -> Types.VARCHAR;
        };
    }

    private static Map<String, ColumnMeta> loadColumnsFromSelect(Connection connection, String tableName)
            throws SQLException {
        Map<String, ColumnMeta> columns = new LinkedHashMap<>();
        // Statement must be closed explicitly — do not chain createStatement().executeQuery() into
        // try-with-resources on ResultSet alone (MySQL may mark the statement closed mid-use).
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM `" + tableName + "` WHERE 1=0")) {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                boolean nullable = meta.isNullable(i) != ResultSetMetaData.columnNoNulls;
                columns.put(
                        meta.getColumnLabel(i).toLowerCase(Locale.ROOT),
                        new ColumnMeta(meta.getColumnType(i), nullable, false, false, false));
            }
        }
        return columns;
    }

    /**
     * Target columns that are required on insert but absent from the backup source
     * (e.g. {@code yctxsj} added after an old VFP DBF export).
     */
    static List<String> missingRequiredInsertColumns(
            Map<String, ColumnMeta> targetColumns, Set<String> sourceColumns) {
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, ColumnMeta> entry : targetColumns.entrySet()) {
            String column = entry.getKey();
            if (sourceColumns.contains(column)) {
                continue;
            }
            ColumnMeta meta = entry.getValue();
            if (meta.autoIncrement() || meta.generated()) {
                continue;
            }
            if (meta.nullable() || meta.hasDefault()) {
                continue;
            }
            missing.add(column);
        }
        missing.sort(String.CASE_INSENSITIVE_ORDER);
        return missing;
    }

    static Object coerceForInsert(Object value, ColumnMeta meta) {
        if (value != null) {
            return value;
        }
        if (meta == null || meta.nullable()) {
            return null;
        }
        return switch (meta.sqlType()) {
            case Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT,
                 Types.DECIMAL, Types.NUMERIC, Types.DOUBLE, Types.FLOAT, Types.REAL -> 0;
            case Types.BOOLEAN, Types.BIT -> false;
            case Types.DATE -> java.sql.Date.valueOf("1970-01-01");
            case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE ->
                    java.sql.Timestamp.valueOf("1970-01-01 00:00:00");
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB -> new byte[0];
            default -> "";
        };
    }

    static Object emptyNumericZero(int sqlType) {
        return switch (sqlType) {
            case Types.BIGINT -> 0L;
            case Types.DECIMAL, Types.NUMERIC -> BigDecimal.ZERO;
            case Types.DOUBLE, Types.FLOAT, Types.REAL -> 0d;
            default -> 0;
        };
    }

    static String sanitizeIdent(String name) {
        if (name == null || !name.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("非法表名: " + name);
        }
        return name;
    }
}
