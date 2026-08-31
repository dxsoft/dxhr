package com.dxsoft.rsgzgl.ops.security;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class OpsPasswordService {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    OpsPasswordService(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    public void changePassword(String username, OpsPasswordChangeRequest request) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("未登录");
        }
        if (request == null) {
            throw new IllegalArgumentException("请填写密码");
        }
        String current = request.currentPassword() == null ? "" : request.currentPassword();
        String next = request.newPassword() == null ? "" : request.newPassword();
        String confirm = request.confirmPassword() == null ? "" : request.confirmPassword();
        if (next.length() < 8) {
            throw new IllegalArgumentException("新密码长度至少 8 位");
        }
        if (!next.equals(confirm)) {
            throw new IllegalArgumentException("两次输入的新密码不一致");
        }
        if (next.equals(current)) {
            throw new IllegalArgumentException("新密码不能与当前密码相同");
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT password_hash FROM ops_admin WHERE username = ?",
                username);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("当前用户不存在");
        }
        String hash = String.valueOf(rows.getFirst().get("password_hash"));
        if (!passwordEncoder.matches(current, hash)) {
            throw new IllegalArgumentException("当前密码错误");
        }
        jdbcTemplate.update(
                "UPDATE ops_admin SET password_hash = ? WHERE username = ?",
                passwordEncoder.encode(next),
                username);
    }
}
