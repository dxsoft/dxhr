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
    private final OrganizationScopeResolver organizationScopeResolver;

    public AppUserDetailsService(
            NamedParameterJdbcTemplate jdbcTemplate,
            OrganizationScopeResolver organizationScopeResolver) {
        this.jdbcTemplate = jdbcTemplate;
        this.organizationScopeResolver = organizationScopeResolver;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<Map<String, Object>> users = jdbcTemplate.queryForList("""
                SELECT id, username, password_hash, display_name, enabled, ukey_id, ukey_required,
                       home_organization_code, all_organizations
                FROM app_user
                WHERE username = :username
                """, new MapSqlParameterSource("username", username));
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return toPrincipal(users.getFirst());
    }

    public AppUserPrincipal loadUserByUkeyId(String ukeyId) {
        List<Map<String, Object>> users = jdbcTemplate.queryForList("""
                SELECT id, username, password_hash, display_name, enabled,
                       ukey_id, ukey_required, sm2_user_id, sm2_pubkey_x, sm2_pubkey_y,
                       home_organization_code, all_organizations
                FROM app_user
                WHERE ukey_id = :ukeyId
                """, new MapSqlParameterSource("ukeyId", ukeyId));
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("UKey not bound: " + ukeyId);
        }
        return toPrincipal(users.getFirst());
    }

    public Map<String, Object> findUkeyBinding(String ukeyId) {
        List<Map<String, Object>> users = jdbcTemplate.queryForList("""
                SELECT id, username, enabled, ukey_id, ukey_required, sm2_user_id, sm2_pubkey_x, sm2_pubkey_y,
                       enc_algo_key, ukey_auth_modes
                FROM app_user
                WHERE ukey_id = :ukeyId
                """, new MapSqlParameterSource("ukeyId", ukeyId));
        return users.isEmpty() ? null : users.getFirst();
    }

    public Map<String, Object> findUkeyBindingByUsername(String username) {
        List<Map<String, Object>> users = jdbcTemplate.queryForList("""
                SELECT id, username, enabled, ukey_id, ukey_required, sm2_user_id, sm2_pubkey_x, sm2_pubkey_y,
                       enc_algo_key, ukey_auth_modes
                FROM app_user
                WHERE username = :username
                """, new MapSqlParameterSource("username", username));
        return users.isEmpty() ? null : users.getFirst();
    }

    private AppUserPrincipal toPrincipal(Map<String, Object> user) {
        Long userId = ((Number) user.get("id")).longValue();
        Set<String> permissions = loadPermissions(userId);
        boolean allOrganizations = booleanValue(user.get("all_organizations"));
        String homeOrganizationCode = blankToNull((String) user.get("home_organization_code"));
        Set<String> organizationCodes = loadOrganizationCodes(allOrganizations, homeOrganizationCode);

        return new AppUserPrincipal(
                userId,
                SqlText.trim((String) user.get("username")),
                (String) user.get("password_hash"),
                SqlText.trim((String) user.get("display_name")),
                booleanValue(user.get("enabled")),
                permissions.stream().map(SimpleGrantedAuthority::new).toList(),
                permissions,
                allOrganizations,
                organizationCodes,
                homeOrganizationCode,
                SqlText.trim((String) user.get("ukey_id")),
                integerOrNull(user.get("ukey_required")));
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

    private Set<String> loadOrganizationCodes(boolean allOrganizations, String homeOrganizationCode) {
        if (allOrganizations || homeOrganizationCode == null) {
            return Set.of();
        }
        return organizationScopeResolver.expandWithDescendants(List.of(homeOrganizationCode));
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

    private Integer integerOrNull(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text)) {
            return null;
        }
        return Integer.parseInt(text);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
