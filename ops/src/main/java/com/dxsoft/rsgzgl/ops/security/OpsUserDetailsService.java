package com.dxsoft.rsgzgl.ops.security;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class OpsUserDetailsService implements UserDetailsService {

    private final JdbcTemplate jdbcTemplate;

    public OpsUserDetailsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT username, password_hash, enabled FROM ops_admin WHERE username = ?",
                username);
        if (rows.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        Map<String, Object> row = rows.getFirst();
        boolean enabled = booleanValue(row.get("enabled"));
        return User.builder()
                .username(String.valueOf(row.get("username")))
                .password(String.valueOf(row.get("password_hash")))
                .disabled(!enabled)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_OPS")))
                .build();
    }

    private boolean booleanValue(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }
}
