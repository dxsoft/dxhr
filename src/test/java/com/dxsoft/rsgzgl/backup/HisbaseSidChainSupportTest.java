package com.dxsoft.rsgzgl.backup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.junit.jupiter.api.Test;

class HisbaseSidChainSupportTest {

    @Test
    void recognizesChainTables() {
        assertTrue(HisbaseSidChainSupport.isChainTable("hisbase"));
        assertTrue(HisbaseSidChainSupport.isChainTable("HISBASEB"));
        assertFalse(HisbaseSidChainSupport.isChainTable("dryjbxx"));
    }

    @Test
    void repairSummaryDetectsIssues() {
        HisbaseSidChainRepairSummary healthy = new HisbaseSidChainRepairSummary(
                "hisbase", 0, 0, 0, 100, 0, 0, 0, 0);
        assertTrue(healthy.isHealthyAfter());
        assertFalse(healthy.hadChainIssuesBefore());

        HisbaseSidChainRepairSummary fixed = new HisbaseSidChainRepairSummary(
                "hisbase", 3, 2, 5500, 126801, 5500, 0, 0, 0);
        assertTrue(fixed.hadChainIssuesBefore());
        assertTrue(fixed.isHealthyAfter());
    }

    @Test
    void resolvesUppercaseHisbaseNameForChainRepair() throws Exception {
        try (Connection connection =
                DriverManager.getConnection("jdbc:h2:mem:hisbase_chain;MODE=MySQL;DB_CLOSE_DELAY=-1")) {
            try (Statement st = connection.createStatement()) {
                st.execute("""
                        CREATE TABLE hisbase (
                          id VARCHAR(20) PRIMARY KEY,
                          sid VARCHAR(20)
                        )
                        """);
            }
            assertThat(BackupTableNameSupport.resolveTableName(connection, "HISBASE"))
                    .isNotNull()
                    .matches(name -> name.equalsIgnoreCase("hisbase"));
            assertThat(BackupTableNameSupport.resolveTableName(connection, "HISBASEB"))
                    .isNull();
        }
    }
}
