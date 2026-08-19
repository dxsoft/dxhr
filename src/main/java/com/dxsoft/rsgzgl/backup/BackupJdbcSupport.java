package com.dxsoft.rsgzgl.backup;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

/**
 * Shared JDBC helpers for long-running backup restore sessions.
 */
final class BackupJdbcSupport {

    private static final int RECONNECT_ATTEMPTS = 12;
    private static final long RECONNECT_SLEEP_MS = 3000L;

    private BackupJdbcSupport() {
    }

    static Connection openRestoreConnection(DataSource dataSource) throws SQLException {
        Connection connection = dataSource.getConnection();
        prepareRestoreSession(connection);
        connection.setAutoCommit(false);
        return connection;
    }

    /**
     * Open a restore connection, waiting if MySQL was just restarted (e.g. after OOM kill).
     */
    static Connection openRestoreConnectionWithRetry(DataSource dataSource) throws SQLException {
        SQLException last = null;
        for (int attempt = 1; attempt <= RECONNECT_ATTEMPTS; attempt++) {
            try {
                return openRestoreConnection(dataSource);
            } catch (SQLException ex) {
                last = ex;
                if (attempt == RECONNECT_ATTEMPTS) {
                    break;
                }
                try {
                    Thread.sleep(RECONNECT_SLEEP_MS);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw ex;
                }
            }
        }
        throw new SQLException(
                "无法重新连接数据库（已重试 " + RECONNECT_ATTEMPTS + " 次）。"
                        + " 若服务器内存不足，MySQL 可能被系统杀掉，请检查内存/交换分区后重试。"
                        + " 原因: " + rootMessage(last),
                last);
    }

    static void prepareRestoreSession(Connection connection) throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("SET FOREIGN_KEY_CHECKS=0");
            quietExecute(st, "SET UNIQUE_CHECKS=0");
            try {
                st.execute("SET NAMES utf8mb4");
            } catch (SQLException ignored) {
                // H2 / other dialects
            }
            // Keep the session alive during large multi-table restores.
            quietExecute(st, "SET SESSION wait_timeout=28800");
            quietExecute(st, "SET SESSION interactive_timeout=28800");
            quietExecute(st, "SET SESSION net_read_timeout=3600");
            quietExecute(st, "SET SESSION net_write_timeout=3600");
        }
    }

    static void finishRestoreSession(Connection connection) throws SQLException {
        if (connection == null || connection.isClosed()) {
            return;
        }
        try (Statement st = connection.createStatement()) {
            quietExecute(st, "SET UNIQUE_CHECKS=1");
            st.execute("SET FOREIGN_KEY_CHECKS=1");
        }
        connection.commit();
    }

    static void safeRollback(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            if (!connection.isClosed()) {
                connection.rollback();
            }
        } catch (SQLException ignored) {
            // Connection may already be dead; do not mask the original error.
        }
    }

    static void safeClose(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {
            // ignore
        }
    }

    static boolean isConnectionClosed(Throwable error) {
        Throwable cursor = error;
        while (cursor != null) {
            String message = cursor.getMessage();
            if (message != null) {
                String lower = message.toLowerCase();
                if (lower.contains("connection is closed")
                        || lower.contains("connection closed")
                        || lower.contains("no operations allowed after connection closed")
                        || lower.contains("no operations allowed after statement closed")
                        || lower.contains("communications link failure")
                        || lower.contains("connection reset")
                        || lower.contains("broken pipe")
                        || lower.contains("can not read response from server")
                        || lower.contains("cannot read response from server")
                        || lower.contains("server shutdown")
                        || lower.contains("got an error reading communication packets")) {
                    return true;
                }
            }
            if (cursor instanceof SQLException sqlEx) {
                String state = sqlEx.getSQLState();
                // Class 08 = connection exception
                if (state != null && state.startsWith("08")) {
                    return true;
                }
            }
            cursor = cursor.getCause();
        }
        return false;
    }

    static String rootMessage(Throwable error) {
        Throwable cursor = error;
        String last = error == null ? "未知错误" : String.valueOf(error.getMessage());
        while (cursor != null) {
            if (cursor.getMessage() != null && !cursor.getMessage().isBlank()) {
                last = cursor.getMessage();
            }
            cursor = cursor.getCause();
        }
        return last;
    }

    private static void quietExecute(Statement st, String sql) {
        try {
            st.execute(sql);
        } catch (SQLException ignored) {
            // Optional session knobs; ignore on unsupported engines.
        }
    }
}
