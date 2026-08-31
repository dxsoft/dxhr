package com.dxsoft.rsgzgl.backup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Set;

/**
 * Rebuild {@code sid} linked lists on payroll history tables (VFP {@code crtrel.prg} semantics).
 * Chain tips are stored as {@code sid = ''} (not NULL) so tip joins can use {@code h.sid = ''}.
 */
final class HisbaseSidChainSupport {

    static final Set<String> CHAIN_TABLES = Set.of("hisbase", "hisbaseb");

    private HisbaseSidChainSupport() {
    }

    record ChainIssueStats(int multiTipPersons, int brokenSidRefs, int nullSidTips) {
        boolean isHealthy() {
            return multiTipPersons == 0 && brokenSidRefs == 0 && nullSidTips == 0;
        }
    }

    static boolean isChainTable(String tableName) {
        return tableName != null && CHAIN_TABLES.contains(tableName.toLowerCase(Locale.ROOT));
    }

    static ChainIssueStats inspect(Connection connection, String tableName) throws SQLException {
        String safe = BackupColumnSupport.sanitizeIdent(tableName);
        return new ChainIssueStats(
                countMultiTipPersons(connection, safe),
                countBrokenSidRefs(connection, safe),
                countNullSidTips(connection, safe));
    }

    static int rebuild(Connection connection, String tableName) throws SQLException {
        String safe = BackupColumnSupport.sanitizeIdent(tableName);
        String sql = """
                UPDATE `%s` h
                INNER JOIN (
                    SELECT id, new_sid
                    FROM (
                        SELECT id,
                               COALESCE(
                                   LAG(id) OVER (
                                       PARTITION BY dwbm, grbm
                                       ORDER BY jsnf DESC, jsyf DESC, hj2 DESC, id DESC
                                   ),
                                   ''
                               ) AS new_sid
                        FROM `%s`
                    ) ranked
                ) x ON h.id = x.id
                SET h.sid = x.new_sid
                """.formatted(safe, safe);
        try (Statement st = connection.createStatement()) {
            return st.executeUpdate(sql);
        }
    }

    /** Normalize CSV-restore NULL tips to empty string for {@code sid = ''} index usage. */
    static int normalizeNullSidTips(Connection connection, String tableName) throws SQLException {
        String safe = BackupColumnSupport.sanitizeIdent(tableName);
        String sql = "UPDATE `" + safe + "` SET sid = '' WHERE sid IS NULL";
        try (Statement st = connection.createStatement()) {
            return st.executeUpdate(sql);
        }
    }

    private static int countMultiTipPersons(Connection connection, String tableName) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS cnt
                FROM (
                    SELECT dwbm, grbm
                    FROM `%s`
                    WHERE sid = ''
                    GROUP BY dwbm, grbm
                    HAVING COUNT(*) > 1
                ) t
                """.formatted(tableName);
        return queryInt(connection, sql);
    }

    private static int countNullSidTips(Connection connection, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `" + tableName + "` WHERE sid IS NULL";
        return queryInt(connection, sql);
    }

    private static int countBrokenSidRefs(Connection connection, String tableName) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS cnt
                FROM `%s` h
                LEFT JOIN `%s` n ON TRIM(n.id) = TRIM(h.sid)
                WHERE TRIM(COALESCE(h.sid, '')) <> ''
                  AND n.id IS NULL
                """.formatted(tableName, tableName);
        return queryInt(connection, sql);
    }

    private static int queryInt(Connection connection, String sql) throws SQLException {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
