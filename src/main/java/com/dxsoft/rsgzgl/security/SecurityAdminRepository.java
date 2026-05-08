package com.dxsoft.rsgzgl.security;

import com.dxsoft.rsgzgl.common.PageRequest;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class SecurityAdminRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    SecurityAdminRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = namedJdbcTemplate;
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

    List<SecurityAdminService.UserAdminView> users(String keyword, PageRequest pageRequest) {
        MapSqlParameterSource parameters = pagingParameters(keyword, pageRequest);
        return namedJdbcTemplate.query("""
                SELECT id, username, display_name, enabled
                FROM app_user
                WHERE (:keyword IS NULL OR username LIKE :keywordLike OR display_name LIKE :keywordLike)
                ORDER BY username
                LIMIT :limit OFFSET :offset
                """, parameters, (rs, rowNum) -> new SecurityAdminService.UserAdminView(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("display_name"),
                rs.getBoolean("enabled"),
                rolesForUser(rs.getLong("id"))));
    }

    long countUsers(String keyword) {
        Long count = namedJdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM app_user
                WHERE (:keyword IS NULL OR username LIKE :keywordLike OR display_name LIKE :keywordLike)
                """, keywordParameters(keyword), Long.class);
        return count == null ? 0 : count;
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

    List<SecurityAdminService.RoleAdminView> roles(String keyword, PageRequest pageRequest) {
        MapSqlParameterSource parameters = pagingParameters(keyword, pageRequest);
        return namedJdbcTemplate.query("""
                SELECT id, code, name, data_scope
                FROM app_role
                WHERE (:keyword IS NULL OR code LIKE :keywordLike OR name LIKE :keywordLike OR data_scope LIKE :keywordLike)
                ORDER BY code
                LIMIT :limit OFFSET :offset
                """, parameters, (rs, rowNum) -> new SecurityAdminService.RoleAdminView(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("data_scope"),
                permissionsForRole(rs.getLong("id")),
                organizationCodesForRole(rs.getLong("id"))));
    }

    long countRoles(String keyword) {
        Long count = namedJdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM app_role
                WHERE (:keyword IS NULL OR code LIKE :keywordLike OR name LIKE :keywordLike OR data_scope LIKE :keywordLike)
                """, keywordParameters(keyword), Long.class);
        return count == null ? 0 : count;
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

    List<SecurityAdminService.MenuAdminView> menus(String keyword, PageRequest pageRequest) {
        MapSqlParameterSource parameters = pagingParameters(keyword, pageRequest);
        return namedJdbcTemplate.query("""
                SELECT id, code, title, path, permission_code, sort_order, enabled
                FROM app_menu
                WHERE (:keyword IS NULL
                   OR code LIKE :keywordLike
                   OR title LIKE :keywordLike
                   OR path LIKE :keywordLike
                   OR permission_code LIKE :keywordLike)
                ORDER BY sort_order, id
                LIMIT :limit OFFSET :offset
                """, parameters, (rs, rowNum) -> new SecurityAdminService.MenuAdminView(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("title"),
                rs.getString("path"),
                rs.getString("permission_code"),
                rs.getInt("sort_order"),
                rs.getBoolean("enabled")));
    }

    long countMenus(String keyword) {
        Long count = namedJdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM app_menu
                WHERE (:keyword IS NULL
                   OR code LIKE :keywordLike
                   OR title LIKE :keywordLike
                   OR path LIKE :keywordLike
                   OR permission_code LIKE :keywordLike)
                """, keywordParameters(keyword), Long.class);
        return count == null ? 0 : count;
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

    Long createMenu(String code, String title, String path, String permissionCode, Integer sortOrder, boolean enabled) {
        jdbcTemplate.update("""
                INSERT INTO app_menu (code, title, path, permission_code, sort_order, enabled)
                VALUES (?, ?, ?, ?, ?, ?)
                """, code, title, path, permissionCode, sortOrder == null ? 0 : sortOrder, enabled ? 1 : 0);
        return jdbcTemplate.queryForObject("SELECT id FROM app_menu WHERE code = ?", Long.class, code);
    }

    void updateMenu(Long menuId, String title, String path, String permissionCode, Integer sortOrder, boolean enabled) {
        jdbcTemplate.update("""
                UPDATE app_menu
                SET title = ?, path = ?, permission_code = ?, sort_order = ?, enabled = ?
                WHERE id = ?
                """, title, path, permissionCode, sortOrder == null ? 0 : sortOrder, enabled ? 1 : 0, menuId);
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

    private MapSqlParameterSource pagingParameters(String keyword, PageRequest pageRequest) {
        return keywordParameters(keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
    }

    private MapSqlParameterSource keywordParameters(String keyword) {
        String trimmedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return new MapSqlParameterSource()
                .addValue("keyword", trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null ? null : "%" + trimmedKeyword + "%");
    }
}
