package com.dxsoft.rsgzgl.security;

import com.dxsoft.rsgzgl.common.SqlText;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AppUserDetailsService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<Map<String, Object>> users = jdbcTemplate.queryForList("""
                SELECT id, username, password_hash, display_name, enabled
                FROM app_user
                WHERE username = :username
                """, new MapSqlParameterSource("username", username));
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        Map<String, Object> user = users.getFirst();
        Long userId = ((Number) user.get("id")).longValue();
        Set<String> permissions = loadPermissions(userId);
        Set<String> organizationCodes = loadOrganizationCodes(userId);
        boolean allOrganizations = hasAllOrganizations(userId);

        return new AppUserPrincipal(
                userId,
                SqlText.trim((String) user.get("username")),
                (String) user.get("password_hash"),
                SqlText.trim((String) user.get("display_name")),
                booleanValue(user.get("enabled")),
                permissions.stream().map(SimpleGrantedAuthority::new).toList(),
                permissions,
                allOrganizations,
                organizationCodes);
    }

    private Set<String> loadPermissions(Long userId) {
        List<String> rows = jdbcTemplate.queryForList("""
                SELECT DISTINCT p.code
                FROM app_user_role ur
                JOIN app_role_permission rp ON rp.role_id = ur.role_id
                JOIN app_permission p ON p.id = rp.permission_id
                WHERE ur.user_id = :userId
                ORDER BY p.code
                """, new MapSqlParameterSource("userId", userId), String.class);
        return new LinkedHashSet<>(rows);
    }

    private Set<String> loadOrganizationCodes(Long userId) {
        List<String> rows = jdbcTemplate.queryForList("""
                SELECT DISTINCT ros.organization_code
                FROM app_user_role ur
                JOIN app_role_org_scope ros ON ros.role_id = ur.role_id
                WHERE ur.user_id = :userId
                ORDER BY ros.organization_code
                """, new MapSqlParameterSource("userId", userId), String.class);
        return new LinkedHashSet<>(rows);
    }

    private boolean hasAllOrganizations(Long userId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM app_user_role ur
                JOIN app_role r ON r.id = ur.role_id
                WHERE ur.user_id = :userId AND r.data_scope = 'ALL'
                """, new MapSqlParameterSource("userId", userId), Integer.class);
        return count != null && count > 0;
    }

    private boolean booleanValue(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() == 1;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }
}
