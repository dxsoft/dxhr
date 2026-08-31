package com.dxsoft.rsgzgl.backup;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.junit.jupiter.api.Test;

class BackupTableNameSupportTest {

    @Test
    void resolvesLowercaseTableWhenBackupUsesUppercaseName() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:backup_table_name;MODE=MySQL;DB_CLOSE_DELAY=-1")) {
            try (Statement st = connection.createStatement()) {
                st.execute("CREATE TABLE djxgz (id INT PRIMARY KEY, ny VARCHAR(10))");
            }
            assertThat(BackupTableNameSupport.resolveTableName(connection, "DJXGZ"))
                    .isNotNull()
                    .matches(name -> name.equalsIgnoreCase("djxgz"));
            assertThat(BackupTableNameSupport.tableExists(connection, "DJXGZ")).isTrue();
            assertThat(BackupTableNameSupport.resolveTableName(connection, "missing")).isNull();
        }
    }
}
