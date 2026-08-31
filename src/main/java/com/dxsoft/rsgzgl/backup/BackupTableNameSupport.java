package com.dxsoft.rsgzgl.backup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

final class BackupTableNameSupport {

    private BackupTableNameSupport() {
    }

    static boolean tableExists(Connection connection, String tableName) throws SQLException {
        return resolveTableName(connection, tableName) != null;
    }

    /**
     * Returns the table name as stored in the current schema (case-sensitive on Linux MySQL).
     */
    static String resolveTableName(Connection connection, String tableName) throws SQLException {
        String safe = BackupColumnSupport.sanitizeIdent(tableName);
        String fromInformationSchema = lookupTableName(connection, safe);
        if (fromInformationSchema != null) {
            return fromInformationSchema;
        }
        if (existsExact(connection, safe)) {
            return safe;
        }
        String lower = safe.toLowerCase(Locale.ROOT);
        if (existsExact(connection, lower)) {
            return lower;
        }
        String upper = safe.toUpperCase(Locale.ROOT);
        if (existsExact(connection, upper)) {
            return upper;
        }
        return null;
    }

    private static String lookupTableName(Connection connection, String tableName) throws SQLException {
        String sql = """
                SELECT TABLE_NAME
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = LOWER(?)
                LIMIT 1
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String resolved = rs.getString(1);
                    return resolved == null || resolved.isBlank() ? null : resolved.trim();
                }
            }
        } catch (SQLException ex) {
            return null;
        }
        return null;
    }

    private static boolean existsExact(Connection connection, String tableName) throws SQLException {
        try (var st = connection.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = '"
                             + tableName
                             + "' LIMIT 1")) {
            if (rs.next()) {
                return true;
            }
        } catch (SQLException ignored) {
            // H2 / engines without information_schema
        }
        String catalog = connection.getCatalog();
        if (catalog != null && !catalog.isBlank()) {
            try (ResultSet rs = connection.getMetaData().getTables(
                    catalog, null, tableName, new String[] {"TABLE"})) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        try (ResultSet rs = connection.getMetaData().getTables(null, null, tableName, new String[] {"TABLE"})) {
            return rs.next();
        }
    }
}
