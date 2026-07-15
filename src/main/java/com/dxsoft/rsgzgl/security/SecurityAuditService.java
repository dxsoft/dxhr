package com.dxsoft.rsgzgl.security;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SecurityAuditService {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final AccessControlService accessControlService;

    SecurityAuditService(
            JdbcTemplate jdbcTemplate,
            NamedParameterJdbcTemplate namedJdbcTemplate,
            AccessControlService accessControlService) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = namedJdbcTemplate;
        this.accessControlService = accessControlService;
    }

    public void record(String action, String targetType, Object targetId, String summary) {
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

    public List<SecurityAuditLog> recent(int limit) {
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

    public PageResponse<SecurityAuditLog> search(String keyword, PageRequest pageRequest) {
        MapSqlParameterSource parameters = keywordParameters(keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        List<SecurityAuditLog> content = namedJdbcTemplate.query("""
                SELECT id, actor_username, action, target_type, target_id, summary, created_at
                FROM app_security_audit_log
                WHERE (:keyword IS NULL
                   OR actor_username LIKE :keywordLike
                   OR action LIKE :keywordLike
                   OR target_type LIKE :keywordLike
                   OR target_id LIKE :keywordLike
                   OR summary LIKE :keywordLike)
                ORDER BY id DESC
                LIMIT :limit OFFSET :offset
                """, parameters, (rs, rowNum) -> new SecurityAuditLog(
                rs.getLong("id"),
                rs.getString("actor_username"),
                rs.getString("action"),
                rs.getString("target_type"),
                rs.getString("target_id"),
                rs.getString("summary"),
                rs.getTimestamp("created_at").toLocalDateTime()));
        Long total = namedJdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM app_security_audit_log
                WHERE (:keyword IS NULL
                   OR actor_username LIKE :keywordLike
                   OR action LIKE :keywordLike
                   OR target_type LIKE :keywordLike
                   OR target_id LIKE :keywordLike
                   OR summary LIKE :keywordLike)
                """, keywordParameters(keyword), Long.class);
        return PageResponse.of(content, pageRequest, total == null ? 0 : total);
    }

    private MapSqlParameterSource keywordParameters(String keyword) {
        String trimmedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return new MapSqlParameterSource()
                .addValue("keyword", trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null ? null : "%" + trimmedKeyword + "%");
    }
}
