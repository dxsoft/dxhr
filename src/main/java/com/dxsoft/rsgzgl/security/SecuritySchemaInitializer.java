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
    private final String adminUsername;
    private final String adminPassword;
    private final String adminDisplayName;

    SecuritySchemaInitializer(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            @Value("${rsgzgl.security.initialize-schema:true}") boolean initializeSchema,
            @Value("${rsgzgl.security.admin.username:admin}") String adminUsername,
            @Value("${rsgzgl.security.admin.password:}") String adminPassword,
            @Value("${rsgzgl.security.admin.display-name:系统管理员}") String adminDisplayName) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.initializeSchema = initializeSchema;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.adminDisplayName = adminDisplayName;
        initialize();
    }

    private void initialize() {
        if (!initializeSchema) {
            return;
        }
        createTables();
        seedPermissions();
        seedMenus();
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
                """,
                """
                CREATE TABLE IF NOT EXISTS app_security_audit_log (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    actor_username VARCHAR(80) NOT NULL,
                    action VARCHAR(80) NOT NULL,
                    target_type VARCHAR(40) NOT NULL,
                    target_id VARCHAR(80) NOT NULL,
                    summary VARCHAR(500) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS app_menu (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    code VARCHAR(80) NOT NULL UNIQUE,
                    title VARCHAR(80) NOT NULL,
                    path VARCHAR(120) NOT NULL,
                    permission_code VARCHAR(120) NOT NULL,
                    sort_order INT NOT NULL DEFAULT 0,
                    enabled TINYINT(1) NOT NULL DEFAULT 1
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

    private void seedMenus() {
        upsertMenu("PERSONNEL", "人员查询", "#personnel", "PERSONNEL_READ", 10);
        upsertMenu("PAYROLL", "工资试算", "#payroll", "PAYROLL_READ", 20);
        upsertMenu("AUDIT", "批量对账", "#audit", "AUDIT_READ", 30);
        upsertMenu("SECURITY", "权限管理", "#security", "SECURITY_ADMIN", 90);
    }

    private void seedAdmin() {
        jdbcTemplate.update("""
                INSERT INTO app_role (code, name, data_scope)
                SELECT 'ADMIN', '系统管理员', 'ALL'
                WHERE NOT EXISTS (SELECT 1 FROM app_role WHERE code = 'ADMIN')
                """);
        if (adminPassword == null || adminPassword.isBlank()) {
            return;
        }
        Integer userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_user WHERE username = ?",
                Integer.class,
                adminUsername);
        if (userCount == null || userCount == 0) {
            jdbcTemplate.update("""
                    INSERT INTO app_user (username, password_hash, display_name, enabled)
                    VALUES (?, ?, ?, 1)
                    """, adminUsername, passwordEncoder.encode(adminPassword), adminDisplayName);
        }
        jdbcTemplate.update("""
                INSERT IGNORE INTO app_user_role (user_id, role_id)
                SELECT u.id, r.id
                FROM app_user u, app_role r
                WHERE u.username = ? AND r.code = 'ADMIN'
                """, adminUsername);
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

    private void upsertMenu(String code, String title, String path, String permissionCode, int sortOrder) {
        jdbcTemplate.update("""
                INSERT INTO app_menu (code, title, path, permission_code, sort_order, enabled)
                SELECT ?, ?, ?, ?, ?, 1
                WHERE NOT EXISTS (SELECT 1 FROM app_menu WHERE code = ?)
                """, code, title, path, permissionCode, sortOrder, code);
    }
}
