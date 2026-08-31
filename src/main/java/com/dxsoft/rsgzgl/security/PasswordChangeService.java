package com.dxsoft.rsgzgl.security;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
class PasswordChangeService {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final AccessControlService accessControlService;
    private final SecurityAuditService auditService;

    PasswordChangeService(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            AccessControlService accessControlService,
            SecurityAuditService auditService) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.accessControlService = accessControlService;
        this.auditService = auditService;
    }

    void changeCurrentUserPassword(String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("新密码长度至少 8 位");
        }
        AppUserPrincipal currentUser = accessControlService.currentUser();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT password_hash FROM app_user WHERE id = ?",
                currentUser.userId());
        if (rows.isEmpty()) {
            throw new BadCredentialsException("当前用户不存在");
        }
        String passwordHash = (String) rows.getFirst().get("password_hash");
        if (!passwordEncoder.matches(currentPassword == null ? "" : currentPassword, passwordHash)) {
            throw new BadCredentialsException("当前密码错误");
        }
        jdbcTemplate.update(
                "UPDATE app_user SET password_hash = ? WHERE id = ?",
                passwordEncoder.encode(newPassword),
                currentUser.userId());
        auditService.record("CHANGE_OWN_PASSWORD", "USER", currentUser.userId(), "用户修改自己的密码");
    }
}
