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
                SELECT id, username, display_name, enabled, ukey_id, sm2_user_id, sm2_pubkey_x, sm2_pubkey_y,
                       enc_algo_key, ukey_auth_modes, ukey_required, home_organization_code, all_organizations
                FROM app_user
                ORDER BY username
                """, (rs, rowNum) -> mapUser(rs));
    }

    List<SecurityAdminService.UserAdminView> users(String keyword, PageRequest pageRequest) {
        MapSqlParameterSource parameters = pagingParameters(keyword, pageRequest);
        return namedJdbcTemplate.query("""
                SELECT id, username, display_name, enabled, ukey_id, sm2_user_id, sm2_pubkey_x, sm2_pubkey_y,
                       enc_algo_key, ukey_auth_modes, ukey_required, home_organization_code, all_organizations
                FROM app_user
                WHERE (:keyword IS NULL OR username LIKE :keywordLike OR display_name LIKE :keywordLike
                   OR ukey_id LIKE :keywordLike OR sm2_user_id LIKE :keywordLike)
                ORDER BY username
                LIMIT :limit OFFSET :offset
                """, parameters, (rs, rowNum) -> mapUser(rs));
    }

    long countUsers(String keyword) {
        Long count = namedJdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM app_user
                WHERE (:keyword IS NULL OR username LIKE :keywordLike OR display_name LIKE :keywordLike
                   OR ukey_id LIKE :keywordLike OR sm2_user_id LIKE :keywordLike)
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
                SELECT id, code, title, path, permission_code, parent_id, sort_order, enabled
                FROM app_menu
                WHERE (:keyword IS NULL
                   OR code LIKE :keywordLike
                   OR title LIKE :keywordLike
                   OR path LIKE :keywordLike
                   OR permission_code LIKE :keywordLike)
                ORDER BY sort_order, id
                LIMIT :limit OFFSET :offset
                """, parameters, (rs, rowNum) -> mapMenu(rs));
    }

    List<SecurityAdminService.MenuAdminView> menusAll(String keyword) {
        return namedJdbcTemplate.query("""
                SELECT id, code, title, path, permission_code, parent_id, sort_order, enabled
                FROM app_menu
                WHERE (:keyword IS NULL
                   OR code LIKE :keywordLike
                   OR title LIKE :keywordLike
                   OR path LIKE :keywordLike
                   OR permission_code LIKE :keywordLike)
                ORDER BY sort_order, id
                """, keywordParameters(keyword), (rs, rowNum) -> mapMenu(rs));
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

    void updateUsersEnabled(List<Long> userIds, boolean enabled) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("enabled", enabled ? 1 : 0)
                .addValue("ids", userIds);
        namedJdbcTemplate.update("UPDATE app_user SET enabled = :enabled WHERE id IN (:ids)", parameters);
    }

    void updateUserPassword(Long userId, String passwordHash) {
        jdbcTemplate.update("UPDATE app_user SET password_hash = ? WHERE id = ?", passwordHash, userId);
    }

    void updateUserUkey(
            Long userId,
            String ukeyId,
            String sm2UserId,
            String sm2PubkeyX,
            String sm2PubkeyY,
            String encAlgoKey,
            String ukeyAuthModes,
            Integer ukeyRequired) {
        jdbcTemplate.update("""
                UPDATE app_user
                SET ukey_id = ?, sm2_user_id = ?, sm2_pubkey_x = ?, sm2_pubkey_y = ?,
                    enc_algo_key = ?, ukey_auth_modes = ?, ukey_required = ?
                WHERE id = ?
                """, ukeyId, sm2UserId, sm2PubkeyX, sm2PubkeyY, encAlgoKey, ukeyAuthModes, ukeyRequired, userId);
    }

    /** Binding import: preserve existing ukey_required. */
    void updateUserUkeyBinding(
            Long userId,
            String ukeyId,
            String sm2UserId,
            String sm2PubkeyX,
            String sm2PubkeyY,
            String encAlgoKey,
            String ukeyAuthModes) {
        jdbcTemplate.update("""
                UPDATE app_user
                SET ukey_id = ?, sm2_user_id = ?, sm2_pubkey_x = ?, sm2_pubkey_y = ?,
                    enc_algo_key = ?, ukey_auth_modes = ?
                WHERE id = ?
                """, ukeyId, sm2UserId, sm2PubkeyX, sm2PubkeyY, encAlgoKey, ukeyAuthModes, userId);
    }

    Long findUserIdByUkeyId(String ukeyId) {
        List<Long> ids = jdbcTemplate.queryForList(
                "SELECT id FROM app_user WHERE ukey_id = ?",
                Long.class,
                ukeyId);
        return ids.isEmpty() ? null : ids.getFirst();
    }

    Long findUserIdByUsername(String username) {
        List<Long> ids = jdbcTemplate.queryForList(
                "SELECT id FROM app_user WHERE username = ?",
                Long.class,
                username);
        return ids.isEmpty() ? null : ids.getFirst();
    }

    void updateUserDataScope(Long userId, boolean allOrganizations, String organizationCode) {
        jdbcTemplate.update(
                """
                UPDATE app_user
                SET all_organizations = ?, home_organization_code = ?
                WHERE id = ?
                """,
                allOrganizations ? 1 : 0,
                organizationCode,
                userId);
    }

    boolean userHasAnyRole(Long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_user_role WHERE user_id = ?",
                Integer.class,
                userId);
        return count != null && count > 0;
    }

    boolean userAllOrganizations(Long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT all_organizations FROM app_user WHERE id = ?",
                Integer.class,
                userId);
        return count != null && count == 1;
    }

    String homeOrganizationCodeForUser(Long userId) {
        List<String> rows = jdbcTemplate.queryForList(
                "SELECT home_organization_code FROM app_user WHERE id = ?",
                String.class,
                userId);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private SecurityAdminService.UserAdminView mapUser(java.sql.ResultSet rs) throws java.sql.SQLException {
        long id = rs.getLong("id");
        Integer ukeyRequired = (Integer) rs.getObject("ukey_required");
        return new SecurityAdminService.UserAdminView(
                id,
                rs.getString("username"),
                rs.getString("display_name"),
                rs.getBoolean("enabled"),
                rolesForUser(id),
                rs.getBoolean("all_organizations"),
                rs.getString("home_organization_code"),
                rs.getString("ukey_id"),
                rs.getString("sm2_user_id"),
                rs.getString("sm2_pubkey_x"),
                rs.getString("sm2_pubkey_y"),
                rs.getString("enc_algo_key"),
                rs.getString("ukey_auth_modes"),
                ukeyRequired);
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

    Long createMenu(
            String code,
            String title,
            String path,
            String permissionCode,
            Long parentId,
            Integer sortOrder,
            boolean enabled) {
        jdbcTemplate.update("""
                INSERT INTO app_menu (code, title, path, permission_code, parent_id, sort_order, enabled)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, code, title, path, permissionCode, parentId, sortOrder == null ? 0 : sortOrder, enabled ? 1 : 0);
        return jdbcTemplate.queryForObject("SELECT id FROM app_menu WHERE code = ?", Long.class, code);
    }

    void updateMenu(
            Long menuId,
            String title,
            String path,
            String permissionCode,
            Long parentId,
            Integer sortOrder,
            boolean enabled) {
        jdbcTemplate.update("""
                UPDATE app_menu
                SET title = ?, path = ?, permission_code = ?, parent_id = ?, sort_order = ?, enabled = ?
                WHERE id = ?
                """, title, path, permissionCode, parentId, sortOrder == null ? 0 : sortOrder, enabled ? 1 : 0, menuId);
    }

    void reorderMenus(List<SecurityAdminService.MenuOrderItem> items) {
        for (SecurityAdminService.MenuOrderItem item : items) {
            if (item == null || item.id() == null) {
                continue;
            }
            jdbcTemplate.update(
                    "UPDATE app_menu SET parent_id = ?, sort_order = ? WHERE id = ?",
                    item.parentId(),
                    item.sortOrder() == null ? 0 : item.sortOrder(),
                    item.id());
        }
    }

    private SecurityAdminService.MenuAdminView mapMenu(java.sql.ResultSet rs) throws java.sql.SQLException {
        Object parentId = rs.getObject("parent_id");
        return new SecurityAdminService.MenuAdminView(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("title"),
                rs.getString("path"),
                rs.getString("permission_code"),
                parentId == null ? null : ((Number) parentId).longValue(),
                rs.getInt("sort_order"),
                rs.getBoolean("enabled"));
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
