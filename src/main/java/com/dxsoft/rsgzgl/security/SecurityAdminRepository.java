package com.dxsoft.rsgzgl.security;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class SecurityAdminRepository {

    private final JdbcTemplate jdbcTemplate;

    SecurityAdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    List<SecurityAdminService.UserAdminView> users() {
        return jdbcTemplate.query("""
                SELECT id, username, display_name, enabled
                FROM app_user
                ORDER BY username
                """, (rs, rowNum) -> new SecurityAdminService.UserAdminView(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("display_name"),
                rs.getBoolean("enabled"),
                rolesForUser(rs.getLong("id"))));
    }

    List<SecurityAdminService.RoleAdminView> roles() {
        return jdbcTemplate.query("""
                SELECT id, code, name, data_scope
                FROM app_role
                ORDER BY code
                """, (rs, rowNum) -> new SecurityAdminService.RoleAdminView(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("data_scope"),
                permissionsForRole(rs.getLong("id")),
                organizationCodesForRole(rs.getLong("id"))));
    }

    List<SecurityAdminService.PermissionView> permissions() {
        return jdbcTemplate.query("""
                SELECT code, name, category
                FROM app_permission
                ORDER BY category, code
                """, (rs, rowNum) -> new SecurityAdminService.PermissionView(
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("category")));
    }

    Long createUser(String username, String passwordHash, String displayName, boolean enabled) {
        jdbcTemplate.update("""
                INSERT INTO app_user (username, password_hash, display_name, enabled)
                VALUES (?, ?, ?, ?)
                """, username, passwordHash, displayName, enabled ? 1 : 0);
        return jdbcTemplate.queryForObject("SELECT id FROM app_user WHERE username = ?", Long.class, username);
    }

    void updateUserEnabled(Long userId, boolean enabled) {
        jdbcTemplate.update("UPDATE app_user SET enabled = ? WHERE id = ?", enabled ? 1 : 0, userId);
    }

    void updateUserPassword(Long userId, String passwordHash) {
        jdbcTemplate.update("UPDATE app_user SET password_hash = ? WHERE id = ?", passwordHash, userId);
    }

    void replaceUserRoles(Long userId, List<String> roleCodes) {
        jdbcTemplate.update("DELETE FROM app_user_role WHERE user_id = ?", userId);
        roleCodes.forEach(code -> jdbcTemplate.update("""
                INSERT IGNORE INTO app_user_role (user_id, role_id)
                SELECT ?, id FROM app_role WHERE code = ?
                """, userId, code));
    }

    Long createRole(String code, String name, String dataScope) {
        jdbcTemplate.update("""
                INSERT INTO app_role (code, name, data_scope)
                VALUES (?, ?, ?)
                """, code, name, dataScope);
        return jdbcTemplate.queryForObject("SELECT id FROM app_role WHERE code = ?", Long.class, code);
    }

    void replaceRolePermissions(Long roleId, List<String> permissionCodes) {
        jdbcTemplate.update("DELETE FROM app_role_permission WHERE role_id = ?", roleId);
        permissionCodes.forEach(code -> jdbcTemplate.update("""
                INSERT IGNORE INTO app_role_permission (role_id, permission_id)
                SELECT ?, id FROM app_permission WHERE code = ?
                """, roleId, code));
    }

    void replaceRoleOrganizations(Long roleId, List<String> organizationCodes) {
        jdbcTemplate.update("DELETE FROM app_role_org_scope WHERE role_id = ?", roleId);
        organizationCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(String::trim)
                .forEach(code -> jdbcTemplate.update(
                        "INSERT IGNORE INTO app_role_org_scope (role_id, organization_code) VALUES (?, ?)",
                        roleId,
                        code));
    }

    private List<String> rolesForUser(Long userId) {
        return jdbcTemplate.queryForList("""
                SELECT r.code
                FROM app_user_role ur
                JOIN app_role r ON r.id = ur.role_id
                WHERE ur.user_id = ?
                ORDER BY r.code
                """, String.class, userId);
    }

    private List<String> permissionsForRole(Long roleId) {
        return jdbcTemplate.queryForList("""
                SELECT p.code
                FROM app_role_permission rp
                JOIN app_permission p ON p.id = rp.permission_id
                WHERE rp.role_id = ?
                ORDER BY p.code
                """, String.class, roleId);
    }

    private List<String> organizationCodesForRole(Long roleId) {
        return jdbcTemplate.queryForList("""
                SELECT organization_code
                FROM app_role_org_scope
                WHERE role_id = ?
                ORDER BY organization_code
                """, String.class, roleId);
    }
}
