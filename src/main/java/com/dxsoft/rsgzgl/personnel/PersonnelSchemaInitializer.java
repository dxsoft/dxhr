package com.dxsoft.rsgzgl.personnel;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
class PersonnelSchemaInitializer {

    PersonnelSchemaInitializer(JdbcTemplate jdbcTemplate) {
        NamedParameterJdbcTemplate named = new NamedParameterJdbcTemplate(jdbcTemplate);
        ensureDryjbxxRemarkColumn(named);
        ensurePersonnelTransferTable(jdbcTemplate);
        ensureNewPersonnelSalaryQueryIndexes(jdbcTemplate);
        ensureTransferProjectionAuditTable(jdbcTemplate);
    }

    private void ensureDryjbxxRemarkColumn(NamedParameterJdbcTemplate jdbcTemplate) {
        Integer tableCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name = 'dryjbxx'
                """, new MapSqlParameterSource(), Integer.class);
        if (tableCount == null || tableCount == 0) {
            return;
        }
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = 'dryjbxx'
                  AND column_name = 'bz'
                """, new MapSqlParameterSource(), Integer.class);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.getJdbcTemplate().execute("""
                ALTER TABLE dryjbxx
                ADD COLUMN bz VARCHAR(200) NULL COMMENT '备注（系统内调动等业务说明；bbz 仍用于审批状态）'
                AFTER bbz
                """);
    }

    private void ensurePersonnelTransferTable(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS app_personnel_transfer (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    person_uid INT NOT NULL,
                    id_card VARCHAR(18) NOT NULL DEFAULT '',
                    person_name VARCHAR(40) NOT NULL DEFAULT '',
                    source_organization_code VARCHAR(20) NOT NULL,
                    source_organization_name VARCHAR(100) NOT NULL DEFAULT '',
                    source_person_code VARCHAR(20) NOT NULL,
                    target_organization_code VARCHAR(20) NOT NULL,
                    target_organization_name VARCHAR(100) NOT NULL DEFAULT '',
                    target_person_code VARCHAR(20) NOT NULL,
                    transfer_period VARCHAR(10) NOT NULL DEFAULT '',
                    change_type VARCHAR(20) NOT NULL DEFAULT '调动',
                    remark VARCHAR(500) NOT NULL DEFAULT '',
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_transfer_uid (person_uid),
                    INDEX idx_transfer_id_card (id_card),
                    INDEX idx_transfer_target (target_organization_code, target_person_code),
                    INDEX idx_transfer_source (source_organization_code, source_person_code)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
    }

    /** Speeds new-personnel salary list: filter tip rows by change type without full hisbase scan. */
    private void ensureNewPersonnelSalaryQueryIndexes(JdbcTemplate jdbcTemplate) {
        ensureIndex(jdbcTemplate, "hisbase", "idx_hisbase_jslb_dwgr", "jslb, dwbm, grbm");
        ensureIndex(jdbcTemplate, "dryjbxx", "idx_dryjbxx_tc_dwbm", "tc, dwbm");
    }

    private void ensureIndex(JdbcTemplate jdbcTemplate, String table, String indexName, String columns) {
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
                jdbcTemplate.execute(
                        "CREATE INDEX `" + indexName + "` ON `" + table + "` (" + columns + ")");
            }
        } catch (Exception ignored) {
            // H2 / non-MySQL / insufficient privilege — skip
        }
    }

    private void ensureTransferProjectionAuditTable(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS gz_ts_projection_audit (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    dwbm VARCHAR(20) NOT NULL,
                    grbm VARCHAR(20) NOT NULL,
                    tip_id VARCHAR(36) NOT NULL,
                    step_no INT NOT NULL,
                    change_period VARCHAR(6) NOT NULL DEFAULT '',
                    change_type VARCHAR(40) NOT NULL DEFAULT '',
                    position_code VARCHAR(20) NOT NULL DEFAULT '',
                    position_name VARCHAR(60) NOT NULL DEFAULT '',
                    grade_level VARCHAR(10) NOT NULL DEFAULT '',
                    grade_step VARCHAR(10) NOT NULL DEFAULT '',
                    level_start_year VARCHAR(4) NOT NULL DEFAULT '',
                    step_start_year VARCHAR(4) NOT NULL DEFAULT '',
                    description VARCHAR(500) NOT NULL DEFAULT '',
                    KEY idx_ts_audit_tip (tip_id),
                    KEY idx_ts_audit_dwgr (dwbm, grbm)
                )
                """);
    }
}
