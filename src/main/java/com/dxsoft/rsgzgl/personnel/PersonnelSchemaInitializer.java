package com.dxsoft.rsgzgl.personnel;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
class PersonnelSchemaInitializer {

    PersonnelSchemaInitializer(
            JdbcTemplate jdbcTemplate,
            MobileAttachmentUploadSessionRepository mobileAttachmentUploadSessionRepository) {
        NamedParameterJdbcTemplate named = new NamedParameterJdbcTemplate(jdbcTemplate);
        ensureDryjbxxRemarkColumn(named);
        ensureSubrecordBbzColumns(named);
        ensurePositionLinkedAwardColumn(named);
        ensureSimplifiedApprovalStatuses(named);
        ensurePersonnelTransferTable(jdbcTemplate);
        ensureNewPersonnelSalaryQueryIndexes(jdbcTemplate);
        ensureApprovalTrackingBbzIndexes(jdbcTemplate);
        ensureApprovalActorColumns(named);
        ensureTransferProjectionAuditTable(jdbcTemplate);
        ensureSubrecordAttachmentTable(jdbcTemplate);
        mobileAttachmentUploadSessionRepository.ensureTables();
    }

    private void ensureSubrecordAttachmentTable(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS app_subrecord_attachment (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    table_name VARCHAR(80) NOT NULL,
                    record_id INT NOT NULL,
                    record_key VARCHAR(80) NOT NULL DEFAULT '',
                    original_name VARCHAR(255) NOT NULL,
                    stored_name VARCHAR(120) NOT NULL,
                    content_type VARCHAR(128) NOT NULL DEFAULT '',
                    file_size BIGINT NOT NULL DEFAULT 0,
                    uploaded_by VARCHAR(80) NOT NULL DEFAULT '',
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (id),
                    INDEX idx_subrecord_attachment_target (table_name, record_id, record_key)
                )
                """);
        ensureSubrecordAttachmentRecordKeyColumn(jdbcTemplate);
    }

    private void ensureSubrecordAttachmentRecordKeyColumn(JdbcTemplate jdbcTemplate) {
        try {
            Integer count = jdbcTemplate.query(
                    """
                    SELECT COUNT(*)
                    FROM information_schema.columns
                    WHERE table_schema = DATABASE()
                      AND table_name = 'app_subrecord_attachment'
                      AND column_name = 'record_key'
                    """,
                    rs -> rs.next() ? rs.getInt(1) : 0);
            if (count != null && count == 0) {
                jdbcTemplate.execute("""
                        ALTER TABLE app_subrecord_attachment
                        ADD COLUMN record_key VARCHAR(80) NOT NULL DEFAULT '' AFTER record_id
                        """);
            }
        } catch (Exception ignored) {
            // H2 / non-MySQL / insufficient privilege — skip
        }
    }

    private void ensureApprovalActorColumns(NamedParameterJdbcTemplate jdbcTemplate) {
        ensureTableApprovalActorColumns(jdbcTemplate, "dryjbxx");
        ensureTableApprovalActorColumns(jdbcTemplate, "dryjbxxb");
        for (String tableName : List.of(
                "dxl", "dryzwbh", "dndkh", "hjxx", "jx",
                "dxlb", "dryzwbhb", "dndkhb", "hjxxb", "jxb")) {
            ensureTableApprovalActorColumns(jdbcTemplate, tableName);
        }
        ensureIndex(jdbcTemplate.getJdbcTemplate(), "dryjbxx", "idx_dryjbxx_shsj", "shsj");
        ensureIndex(jdbcTemplate.getJdbcTemplate(), "dndkh", "idx_dndkh_shsj", "shsj");
    }

    private void ensureTableApprovalActorColumns(NamedParameterJdbcTemplate jdbcTemplate, String tableName) {
        Integer tableCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name = :tableName
                """, new MapSqlParameterSource("tableName", tableName), Integer.class);
        if (tableCount == null || tableCount == 0) {
            return;
        }
        ensureColumnAfterBbz(jdbcTemplate, tableName, "tjr", "VARCHAR(80) NULL COMMENT '提交人'");
        ensureColumnAfterBbz(jdbcTemplate, tableName, "tjsj", "TIMESTAMP NULL COMMENT '提交时间'");
        ensureColumnAfterBbz(jdbcTemplate, tableName, "shr", "VARCHAR(80) NULL COMMENT '审核人'");
        ensureColumnAfterBbz(jdbcTemplate, tableName, "shsj", "TIMESTAMP NULL COMMENT '审核时间'");
    }

    private void ensureColumnAfterBbz(
            NamedParameterJdbcTemplate jdbcTemplate,
            String tableName,
            String columnName,
            String columnDefinition) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = :tableName
                  AND column_name = :columnName
                """, new MapSqlParameterSource()
                .addValue("tableName", tableName)
                .addValue("columnName", columnName), Integer.class);
        if (count != null && count > 0) {
            return;
        }
        Integer bbzCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = :tableName
                  AND column_name = 'bbz'
                """, new MapSqlParameterSource("tableName", tableName), Integer.class);
        String afterClause = bbzCount != null && bbzCount > 0 ? " AFTER bbz" : "";
        jdbcTemplate.getJdbcTemplate().execute("""
                ALTER TABLE `%s`
                ADD COLUMN %s %s%s
                """.formatted(tableName, columnName, columnDefinition, afterClause));
    }

    /** Speeds approval-tracking list when filtering by bbz without keyword. */
    private void ensureApprovalTrackingBbzIndexes(JdbcTemplate jdbcTemplate) {
        ensureIndex(jdbcTemplate, "dryjbxx", "idx_dryjbxx_bbz", "bbz");
        ensureIndex(jdbcTemplate, "dxl", "idx_dxl_bbz", "bbz");
        ensureIndex(jdbcTemplate, "dryzwbh", "idx_dryzwbh_bbz", "bbz");
        ensureIndex(jdbcTemplate, "dndkh", "idx_dndkh_bbz", "bbz");
        ensureIndex(jdbcTemplate, "hjxx", "idx_hjxx_bbz", "bbz");
        ensureIndex(jdbcTemplate, "jx", "idx_jx_bbz", "bbz");
    }

    private void ensureSubrecordBbzColumns(NamedParameterJdbcTemplate jdbcTemplate) {
        ensureTableBbzColumn(jdbcTemplate, "dxl", "bz");
        ensureTableBbzColumn(jdbcTemplate, "dryzwbh", "jsbz");
        ensureTableBbzColumn(jdbcTemplate, "dndkh", "khjg");
        ensureTableBbzColumn(jdbcTemplate, "dxlb", "bz");
        ensureTableBbzColumn(jdbcTemplate, "dryzwbhb", "jsbz");
        ensureTableBbzColumn(jdbcTemplate, "dndkhb", "khjg");
        ensureTableBbzColumn(jdbcTemplate, "hjxx", "jljb");
        ensureTableBbzColumn(jdbcTemplate, "hjxxb", "jljb");
        ensureTableBbzColumn(jdbcTemplate, "jx", "lb");
        ensureTableBbzColumn(jdbcTemplate, "jxb", "lb");
    }

    private void ensureTableBbzColumn(NamedParameterJdbcTemplate jdbcTemplate, String tableName, String afterColumn) {
        Integer tableCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name = :tableName
                """, new MapSqlParameterSource("tableName", tableName), Integer.class);
        if (tableCount == null || tableCount == 0) {
            return;
        }
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = :tableName
                  AND column_name = 'bbz'
                """, new MapSqlParameterSource("tableName", tableName), Integer.class);
        if (count != null && count > 0) {
            jdbcTemplate.getJdbcTemplate().update("""
                    UPDATE `%s` SET bbz = '草稿' WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库'
                    """.formatted(tableName));
            return;
        }
        jdbcTemplate.getJdbcTemplate().execute("""
                ALTER TABLE `%s`
                ADD COLUMN bbz CHAR(8) NULL DEFAULT '草稿' COMMENT '审批状态'
                AFTER `%s`
                """.formatted(tableName, afterColumn));
        jdbcTemplate.getJdbcTemplate().update("""
                UPDATE `%s` SET bbz = '草稿' WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库'
                """.formatted(tableName));
    }

    private void ensurePositionLinkedAwardColumn(NamedParameterJdbcTemplate jdbcTemplate) {
        for (String tableName : List.of("dryzwbh", "dryzwbhb")) {
            Integer tableCount = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM information_schema.tables
                    WHERE table_schema = DATABASE()
                      AND table_name = :tableName
                    """, new MapSqlParameterSource("tableName", tableName), Integer.class);
            if (tableCount == null || tableCount == 0) {
                continue;
            }
            Integer count = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM information_schema.columns
                    WHERE table_schema = DATABASE()
                      AND table_name = :tableName
                      AND column_name = 'linked_award_id'
                    """, new MapSqlParameterSource("tableName", tableName), Integer.class);
            if (count != null && count > 0) {
                continue;
            }
            jdbcTemplate.getJdbcTemplate().execute("""
                    ALTER TABLE `%s`
                    ADD COLUMN linked_award_id INT NULL COMMENT '关联奖惩记录ID'
                    AFTER jsbz
                    """.formatted(tableName));
        }
    }

    private void ensureSimplifiedApprovalStatuses(NamedParameterJdbcTemplate jdbcTemplate) {
        Integer tableCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name = 'dryjbxx'
                """, new MapSqlParameterSource(), Integer.class);
        if (tableCount == null || tableCount == 0) {
            return;
        }
        jdbcTemplate.getJdbcTemplate().update("""
                UPDATE dryjbxx
                SET bbz = '草稿'
                WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库'
                """);
        for (String tableName : List.of(
                "dxl", "dryzwbh", "dndkh", "dxlb", "dryzwbhb", "dndkhb", "hjxx", "hjxxb", "jx", "jxb")) {
            Integer exists = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM information_schema.tables
                    WHERE table_schema = DATABASE()
                      AND table_name = :tableName
                    """, new MapSqlParameterSource("tableName", tableName), Integer.class);
            if (exists == null || exists == 0) {
                continue;
            }
            jdbcTemplate.getJdbcTemplate().update("""
                    UPDATE `%s` SET bbz = '草稿' WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库'
                    """.formatted(tableName));
        }
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
