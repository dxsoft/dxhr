package com.dxsoft.rsgzgl.exchange.notification;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
class ExchangeNotificationSchemaInitializer {

    ExchangeNotificationSchemaInitializer(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS app_exchange_notification (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    notification_type VARCHAR(40) NOT NULL,
                    direction VARCHAR(20) NOT NULL,
                    audience_scope VARCHAR(20) NOT NULL,
                    source_org_code VARCHAR(9) NULL,
                    target_org_code VARCHAR(9) NULL,
                    organization_code VARCHAR(9) NULL,
                    organization_codes VARCHAR(500) NULL,
                    package_type VARCHAR(20) NULL,
                    batch_id VARCHAR(36) NULL,
                    person_count INT NOT NULL DEFAULT 0,
                    summary VARCHAR(500) NOT NULL,
                    action_tab VARCHAR(40) NULL,
                    status VARCHAR(10) NOT NULL DEFAULT 'UNREAD',
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    read_at TIMESTAMP NULL,
                    read_by VARCHAR(80) NULL,
                    workflow_id BIGINT NULL,
                    person_uid INT NULL,
                    source_id INT NULL,
                    source_type VARCHAR(20) NULL
                )
                """);
        ensureIndex(jdbcTemplate, "app_exchange_notification", "idx_exchange_notify_status_created", "status, created_at");
        ensureIndex(jdbcTemplate, "app_exchange_notification", "idx_exchange_notify_audience_created", "audience_scope, created_at");
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
}
