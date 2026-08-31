package com.dxsoft.rsgzgl.workflow;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
class PayrollWorkflowSchemaInitializer {

    PayrollWorkflowSchemaInitializer(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS app_payroll_workflow (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    workflow_no VARCHAR(36) NOT NULL,
                    uid INT NOT NULL,
                    organization_code VARCHAR(9) NOT NULL,
                    person_code VARCHAR(10) NOT NULL,
                    person_name VARCHAR(80) NULL,
                    source_type VARCHAR(20) NOT NULL,
                    source_id INT NULL,
                    payroll_module VARCHAR(40) NOT NULL,
                    expected_jslb VARCHAR(40) NULL,
                    status VARCHAR(20) NOT NULL,
                    personnel_approved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    payroll_completed_at TIMESTAMP NULL,
                    payroll_history_id VARCHAR(20) NULL,
                    created_by VARCHAR(80) NULL,
                    completed_by VARCHAR(80) NULL,
                    summary VARCHAR(500) NOT NULL
                )
                """);
        ensureIndex(jdbcTemplate, "app_payroll_workflow", "idx_payroll_wf_status_module", "status, payroll_module");
        ensureIndex(jdbcTemplate, "app_payroll_workflow", "idx_payroll_wf_org_status", "organization_code, status");
        ensureUniqueIndex(jdbcTemplate, "app_payroll_workflow", "uk_payroll_wf_source_pending", "source_type, source_id, status");
        ensureColumn(jdbcTemplate, "app_exchange_notification", "workflow_id", "BIGINT NULL");
        ensureColumn(jdbcTemplate, "app_exchange_notification", "person_uid", "INT NULL");
        ensureColumn(jdbcTemplate, "app_exchange_notification", "source_id", "INT NULL");
        ensureColumn(jdbcTemplate, "app_exchange_notification", "source_type", "VARCHAR(20) NULL");
    }

    private static void ensureColumn(JdbcTemplate jdbcTemplate, String table, String column, String definition) {
        try {
            Integer count = jdbcTemplate.query(
                    """
                    SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = ?
                      AND COLUMN_NAME = ?
                    """,
                    rs -> rs.next() ? rs.getInt(1) : 0,
                    table,
                    column);
            if (count != null && count == 0) {
                jdbcTemplate.execute("ALTER TABLE `" + table + "` ADD COLUMN `" + column + "` " + definition);
            }
        } catch (Exception ignored) {
            // H2 / insufficient privilege
        }
    }

    private static void ensureIndex(JdbcTemplate jdbcTemplate, String table, String indexName, String columns) {
        try {
            Integer indexCount = jdbcTemplate.query(
                    """
                    SELECT COUNT(*)
                    FROM information_schema.STATISTICS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = ?
                      AND INDEX_NAME = ?
                    """,
                    rs -> rs.next() ? rs.getInt(1) : 0,
                    table,
                    indexName);
            if (indexCount != null && indexCount == 0) {
                jdbcTemplate.execute("CREATE INDEX `" + indexName + "` ON `" + table + "` (" + columns + ")");
            }
        } catch (Exception ignored) {
            // H2 / insufficient privilege
        }
    }

    private static void ensureUniqueIndex(JdbcTemplate jdbcTemplate, String table, String indexName, String columns) {
        try {
            Integer indexCount = jdbcTemplate.query(
                    """
                    SELECT COUNT(*)
                    FROM information_schema.STATISTICS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = ?
                      AND INDEX_NAME = ?
                    """,
                    rs -> rs.next() ? rs.getInt(1) : 0,
                    table,
                    indexName);
            if (indexCount != null && indexCount == 0) {
                jdbcTemplate.execute("CREATE UNIQUE INDEX `" + indexName + "` ON `" + table + "` (" + columns + ")");
            }
        } catch (Exception ignored) {
            // partial unique may fail on duplicate data; fall back to non-unique
            ensureIndex(jdbcTemplate, table, indexName.replace("uk_", "idx_"), columns);
        }
    }
}
