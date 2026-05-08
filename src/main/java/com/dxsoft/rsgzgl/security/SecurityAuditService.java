package com.dxsoft.rsgzgl.security;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
class SecurityAuditService {

    private final JdbcTemplate jdbcTemplate;
    private final AccessControlService accessControlService;

    SecurityAuditService(JdbcTemplate jdbcTemplate, AccessControlService accessControlService) {
        this.jdbcTemplate = jdbcTemplate;
        this.accessControlService = accessControlService;
    }

    void record(String action, String targetType, Object targetId, String summary) {
        recordAs(accessControlService.currentUser().getUsername(), action, targetType, targetId, summary);
    }

    void recordAs(String actorUsername, String action, String targetType, Object targetId, String summary) {
        jdbcTemplate.update("""
                INSERT INTO app_security_audit_log (actor_username, action, target_type, target_id, summary)
                VALUES (?, ?, ?, ?, ?)
                """,
                actorUsername,
                action,
                targetType,
                String.valueOf(targetId),
                summary);
    }

    List<SecurityAuditLog> recent(int limit) {
        return jdbcTemplate.query("""
                SELECT id, actor_username, action, target_type, target_id, summary, created_at
                FROM app_security_audit_log
                ORDER BY id DESC
                LIMIT ?
                """, (rs, rowNum) -> new SecurityAuditLog(
                rs.getLong("id"),
                rs.getString("actor_username"),
                rs.getString("action"),
                rs.getString("target_type"),
                rs.getString("target_id"),
                rs.getString("summary"),
                rs.getTimestamp("created_at").toLocalDateTime()), Math.max(1, Math.min(limit, 200)));
    }
}
