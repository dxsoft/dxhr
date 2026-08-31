package com.dxsoft.rsgzgl.backup;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

final class BackupTableClearSupport {

    private BackupTableClearSupport() {
    }

    static void clearTable(Connection connection, String tableName, boolean truncate) throws SQLException {
        String safe = BackupColumnSupport.sanitizeIdent(tableName);
        try (Statement st = connection.createStatement()) {
            if (truncate) {
                st.execute("TRUNCATE TABLE `" + safe + "`");
            } else {
                st.execute("DELETE FROM `" + safe + "`");
            }
        }
    }
}
