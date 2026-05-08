package com.dxsoft.rsgzgl.security;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class SecuritySchemaInitializer {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final boolean initializeSchema;

    SecuritySchemaInitializer(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            @Value("${rsgzgl.security.initialize-schema:true}") boolean initializeSchema) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.initializeSchema = initializeSchema;
        initialize();
    }

    private void initialize() {
        if (!initializeSchema) {
            return;
        }
        createTables();
        seedPermissions();
        seedAdmin();
    }

    private void createTables() {
        List.of(
                """
                CREATE TABLE IF NOT EXISTS app_user (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(80) NOT NULL UNIQUE,
                    password_hash VARCHAR(120) NOT NULL,
                    display_name VARCHAR(80) NOT NULL,
                    enabled TINYINT(1) NOT NULL DEFAULT 1,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS app_role (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    code VARCHAR(80) NOT NULL UNIQUE,
                    name VARCHAR(80) NOT NULL,
                    data_scope VARCHAR(20) NOT NULL DEFAULT 'CUSTOM'
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS app_permission (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    code VARCHAR(120) NOT NULL UNIQUE,
                    name VARCHAR(120) NOT NULL,
                    category VARCHAR(40) NOT NULL
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS app_user_role (
                    user_id BIGINT NOT NULL,
                    role_id BIGINT NOT NULL,
                    PRIMARY KEY (user_id, role_id)
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS app_role_permission (
                    role_id BIGINT NOT NULL,
                    permission_id BIGINT NOT NULL,
                    PRIMARY KEY (role_id, permission_id)
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS app_role_org_scope (
                    role_id BIGINT NOT NULL,
                    organization_code CHAR(9) NOT NULL,
                    PRIMARY KEY (role_id, organization_code)
                )
                """).forEach(jdbcTemplate::execute);
    }

    private void seedPermissions() {
        upsertPermission("ORG_READ", "单位查询", "ORGANIZATION");
        upsertPermission("PERSONNEL_READ", "人员信息查询", "PERSONNEL");
        upsertPermission("PAYROLL_READ", "工资试算查询", "PAYROLL");
        upsertPermission("AUDIT_READ", "工资批量对账", "PAYROLL");
        upsertPermission("SECURITY_ADMIN", "权限管理", "SYSTEM");
    }

    private void seedAdmin() {
        jdbcTemplate.update("""
                INSERT INTO app_role (code, name, data_scope)
                SELECT 'ADMIN', '系统管理员', 'ALL'
                WHERE NOT EXISTS (SELECT 1 FROM app_role WHERE code = 'ADMIN')
                """);
        Integer userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_user WHERE username = 'admin'",
                Integer.class);
        if (userCount == null || userCount == 0) {
            jdbcTemplate.update("""
                    INSERT INTO app_user (username, password_hash, display_name, enabled)
                    VALUES (?, ?, ?, 1)
                    """, "admin", passwordEncoder.encode("admin123"), "系统管理员");
        }
        jdbcTemplate.update("""
                INSERT IGNORE INTO app_user_role (user_id, role_id)
                SELECT u.id, r.id
                FROM app_user u, app_role r
                WHERE u.username = 'admin' AND r.code = 'ADMIN'
                """);
        jdbcTemplate.update("""
                INSERT IGNORE INTO app_role_permission (role_id, permission_id)
                SELECT r.id, p.id
                FROM app_role r, app_permission p
                WHERE r.code = 'ADMIN'
                """);
    }

    private void upsertPermission(String code, String name, String category) {
        jdbcTemplate.update("""
                INSERT INTO app_permission (code, name, category)
                SELECT ?, ?, ?
                WHERE NOT EXISTS (SELECT 1 FROM app_permission WHERE code = ?)
                """, code, name, category, code);
    }
}
