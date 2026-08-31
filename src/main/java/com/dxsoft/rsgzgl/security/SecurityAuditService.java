package com.dxsoft.rsgzgl.security;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SecurityAuditService {

    private static final int SUMMARY_MAX_LENGTH = 500;
    private static final DateTimeFormatter EXPORT_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

    public void recordAs(String actorUsername, String action, String targetType, Object targetId, String summary) {
        jdbcTemplate.update("""
                INSERT INTO app_security_audit_log (actor_username, action, target_type, target_id, summary)
                VALUES (?, ?, ?, ?, ?)
                """,
                actorUsername,
                action,
                targetType,
                String.valueOf(targetId),
                normalizeSummary(summary));
    }

    public List<SecurityAuditLog> recent(int limit) {
        return jdbcTemplate.query("""
                SELECT id, actor_username, action, target_type, target_id, summary, created_at
                FROM app_security_audit_log
                ORDER BY id DESC
                LIMIT ?
                """, (rs, rowNum) -> mapRow(rs), Math.max(1, Math.min(limit, 200)));
    }

    public PageResponse<SecurityAuditLog> search(String keyword, PageRequest pageRequest) {
        return search(keyword, null, null, null, pageRequest);
    }

    public PageResponse<SecurityAuditLog> search(
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            PageRequest pageRequest) {
        return search(keyword, fromDate, toDate, null, pageRequest);
    }

    public PageResponse<SecurityAuditLog> search(
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            String actionPrefix,
            PageRequest pageRequest) {
        MapSqlParameterSource parameters = searchParameters(keyword, fromDate, toDate, actionPrefix)
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
                  AND (:fromAt IS NULL OR created_at >= :fromAt)
                  AND (:toAt IS NULL OR created_at <= :toAt)
                  AND (:actionPrefix IS NULL OR action LIKE :actionPrefixLike)
                ORDER BY id DESC
                LIMIT :limit OFFSET :offset
                """, parameters, (rs, rowNum) -> mapRow(rs));
        Long total = namedJdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM app_security_audit_log
                WHERE (:keyword IS NULL
                   OR actor_username LIKE :keywordLike
                   OR action LIKE :keywordLike
                   OR target_type LIKE :keywordLike
                   OR target_id LIKE :keywordLike
                   OR summary LIKE :keywordLike)
                  AND (:fromAt IS NULL OR created_at >= :fromAt)
                  AND (:toAt IS NULL OR created_at <= :toAt)
                  AND (:actionPrefix IS NULL OR action LIKE :actionPrefixLike)
                """, searchParameters(keyword, fromDate, toDate, actionPrefix), Long.class);
        return PageResponse.of(content, pageRequest, total == null ? 0 : total);
    }

    public Optional<AuditActorMoment> findLatestByTargetAndActions(
            String targetType,
            String targetId,
            List<String> actions) {
        if (targetType == null || targetType.isBlank() || targetId == null || targetId.isBlank() || actions == null || actions.isEmpty()) {
            return Optional.empty();
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("targetType", targetType.trim())
                .addValue("targetId", targetId.trim())
                .addValue("actions", actions);
        List<AuditActorMoment> rows = namedJdbcTemplate.query("""
                SELECT actor_username, created_at
                FROM app_security_audit_log
                WHERE target_type = :targetType
                  AND TRIM(target_id) = TRIM(:targetId)
                  AND action IN (:actions)
                ORDER BY id DESC
                LIMIT 1
                """, parameters, (rs, rowNum) -> new AuditActorMoment(
                rs.getString("actor_username"),
                rs.getTimestamp("created_at").toLocalDateTime()));
        return rows.stream().findFirst();
    }

    public Map<AuditTargetKey, AuditActorMoment> findLatestByTargetsAndActions(
            List<AuditTargetKey> targets,
            List<String> actions) {
        if (targets == null || targets.isEmpty() || actions == null || actions.isEmpty()) {
            return Map.of();
        }
        List<AuditTargetKey> distinctTargets = targets.stream()
                .filter(key -> key != null && !key.targetType().isBlank() && !key.targetId().isBlank())
                .distinct()
                .toList();
        if (distinctTargets.isEmpty()) {
            return Map.of();
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("actions", actions);
        StringBuilder inClause = new StringBuilder();
        for (int index = 0; index < distinctTargets.size(); index++) {
            AuditTargetKey key = distinctTargets.get(index);
            String typeParam = "targetType" + index;
            String idParam = "targetId" + index;
            parameters.addValue(typeParam, key.targetType());
            parameters.addValue(idParam, key.targetId());
            if (index > 0) {
                inClause.append(", ");
            }
            inClause.append("(:%s, :%s)".formatted(typeParam, idParam));
        }
        List<AuditTargetRow> rows = namedJdbcTemplate.query("""
                SELECT target_type, target_id, actor_username, created_at, id
                FROM app_security_audit_log
                WHERE action IN (:actions)
                  AND (target_type, target_id) IN (%s)
                ORDER BY id DESC
                """.formatted(inClause), parameters, (rs, rowNum) -> new AuditTargetRow(
                rs.getString("target_type"),
                rs.getString("target_id"),
                rs.getString("actor_username"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getLong("id")));
        Map<AuditTargetKey, AuditActorMoment> latest = new LinkedHashMap<>();
        for (AuditTargetRow row : rows) {
            AuditTargetKey key = new AuditTargetKey(row.targetType(), row.targetId());
            latest.putIfAbsent(key, new AuditActorMoment(row.actorUsername(), row.createdAt()));
        }
        return latest;
    }

    private record AuditTargetRow(
            String targetType,
            String targetId,
            String actorUsername,
            LocalDateTime createdAt,
            long id) {
    }

    public byte[] exportCsv(String keyword, LocalDate fromDate, LocalDate toDate) {
        List<SecurityAuditLog> rows = namedJdbcTemplate.query("""
                SELECT id, actor_username, action, target_type, target_id, summary, created_at
                FROM app_security_audit_log
                WHERE (:keyword IS NULL
                   OR actor_username LIKE :keywordLike
                   OR action LIKE :keywordLike
                   OR target_type LIKE :keywordLike
                   OR target_id LIKE :keywordLike
                   OR summary LIKE :keywordLike)
                  AND (:fromAt IS NULL OR created_at >= :fromAt)
                  AND (:toAt IS NULL OR created_at <= :toAt)
                ORDER BY id DESC
                LIMIT 20000
                """, searchParameters(keyword, fromDate, toDate, null), (rs, rowNum) -> mapRow(rs));
        StringBuilder csv = new StringBuilder();
        csv.append('\uFEFF');
        csv.append("ID,操作者,动作,对象类型,对象ID,摘要,时间\n");
        for (SecurityAuditLog log : rows) {
            csv.append(csvCell(String.valueOf(log.id()))).append(',')
                    .append(csvCell(log.actorUsername())).append(',')
                    .append(csvCell(log.action())).append(',')
                    .append(csvCell(log.targetType())).append(',')
                    .append(csvCell(log.targetId())).append(',')
                    .append(csvCell(log.summary())).append(',')
                    .append(csvCell(log.createdAt() == null ? "" : EXPORT_TIME.format(log.createdAt())))
                    .append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private SecurityAuditLog mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new SecurityAuditLog(
                rs.getLong("id"),
                rs.getString("actor_username"),
                rs.getString("action"),
                rs.getString("target_type"),
                rs.getString("target_id"),
                rs.getString("summary"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }

    private MapSqlParameterSource searchParameters(
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            String actionPrefix) {
        String trimmedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        String trimmedActionPrefix = actionPrefix == null || actionPrefix.isBlank() ? null : actionPrefix.trim();
        LocalDateTime fromAt = fromDate == null ? null : fromDate.atStartOfDay();
        LocalDateTime toAt = toDate == null ? null : toDate.atTime(LocalTime.MAX);
        return new MapSqlParameterSource()
                .addValue("keyword", trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null ? null : "%" + trimmedKeyword + "%")
                .addValue("fromAt", fromAt)
                .addValue("toAt", toAt)
                .addValue("actionPrefix", trimmedActionPrefix)
                .addValue("actionPrefixLike", trimmedActionPrefix == null ? null : trimmedActionPrefix + "%");
    }

    private String normalizeSummary(String summary) {
        if (summary == null) {
            return "";
        }
        if (summary.length() <= SUMMARY_MAX_LENGTH) {
            return summary;
        }
        return summary.substring(0, SUMMARY_MAX_LENGTH - 3) + "...";
    }

    private String csvCell(String value) {
        String text = value == null ? "" : value;
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
