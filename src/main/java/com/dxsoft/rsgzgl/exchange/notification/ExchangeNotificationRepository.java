package com.dxsoft.rsgzgl.exchange.notification;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.AppUserPrincipal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
class ExchangeNotificationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AccessControlService accessControlService;

    ExchangeNotificationRepository(JdbcTemplate jdbcTemplate, AccessControlService accessControlService) {
        this.jdbcTemplate = jdbcTemplate;
        this.accessControlService = accessControlService;
    }

    long insert(ExchangeNotificationInsert insert) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(
                    """
                    INSERT INTO app_exchange_notification (
                        notification_type, direction, audience_scope,
                        source_org_code, target_org_code, organization_code, organization_codes,
                        package_type, batch_id, person_count, summary, action_tab, status,
                        workflow_id, person_uid, source_id, source_type
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'UNREAD', ?, ?, ?, ?)
                    """,
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, insert.notificationType());
            ps.setString(2, insert.direction());
            ps.setString(3, insert.audienceScope());
            ps.setString(4, insert.sourceOrgCode());
            ps.setString(5, insert.targetOrgCode());
            ps.setString(6, insert.organizationCode());
            ps.setString(7, insert.organizationCodes());
            ps.setString(8, insert.packageType());
            ps.setString(9, insert.batchId());
            ps.setInt(10, insert.personCount());
            ps.setString(11, insert.summary());
            ps.setString(12, insert.actionTab());
            if (insert.workflowId() == null) {
                ps.setNull(13, java.sql.Types.BIGINT);
            } else {
                ps.setLong(13, insert.workflowId());
            }
            if (insert.personUid() == null) {
                ps.setNull(14, java.sql.Types.INTEGER);
            } else {
                ps.setInt(14, insert.personUid());
            }
            if (insert.sourceId() == null) {
                ps.setNull(15, java.sql.Types.INTEGER);
            } else {
                ps.setInt(15, insert.sourceId());
            }
            ps.setString(16, insert.sourceType());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? 0L : key.longValue();
    }

    PageResponse<ExchangeNotificationRecord> list(String statusFilter, PageRequest pageRequest) {
        AppUserPrincipal user = accessControlService.currentUser();
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        appendVisibilityConditions(user, conditions, params);
        if ("unread".equalsIgnoreCase(statusFilter)) {
            conditions.add("status = 'UNREAD'");
        }
        String where = conditions.isEmpty() ? "" : "WHERE " + String.join(" AND ", conditions);
        long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_exchange_notification " + where,
                Long.class,
                params.toArray());
        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(pageRequest.size());
        queryParams.add(pageRequest.offset());
        List<ExchangeNotificationRecord> content = jdbcTemplate.query(
                """
                SELECT id, notification_type, direction, audience_scope,
                       source_org_code, target_org_code, organization_code, organization_codes,
                       package_type, batch_id, person_count, summary, action_tab, status,
                       created_at, read_at, read_by, workflow_id, person_uid, source_id, source_type
                FROM app_exchange_notification
                """
                        + where
                        + " ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?",
                this::mapRow,
                queryParams.toArray());
        return PageResponse.of(content, pageRequest, total);
    }

    long countUnread() {
        AppUserPrincipal user = accessControlService.currentUser();
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        appendVisibilityConditions(user, conditions, params);
        conditions.add("status = 'UNREAD'");
        String where = "WHERE " + String.join(" AND ", conditions);
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_exchange_notification " + where,
                Long.class,
                params.toArray());
        return count == null ? 0L : count;
    }

    boolean markRead(long id, String username) {
        AppUserPrincipal user = accessControlService.currentUser();
        List<String> conditions = new ArrayList<>(List.of("id = ?"));
        List<Object> params = new ArrayList<>(List.of(id));
        appendVisibilityConditions(user, conditions, params);
        int updated = jdbcTemplate.update(
                """
                UPDATE app_exchange_notification
                SET status = 'READ', read_at = CURRENT_TIMESTAMP, read_by = ?
                """
                        + " WHERE "
                        + String.join(" AND ", conditions),
                prepend(username, params));
        return updated > 0;
    }

    int markAllRead(String username) {
        AppUserPrincipal user = accessControlService.currentUser();
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        appendVisibilityConditions(user, conditions, params);
        conditions.add("status = 'UNREAD'");
        List<Object> updateParams = new ArrayList<>();
        updateParams.add(username);
        updateParams.addAll(params);
        return jdbcTemplate.update(
                """
                UPDATE app_exchange_notification
                SET status = 'READ', read_at = CURRENT_TIMESTAMP, read_by = ?
                """
                        + " WHERE "
                        + String.join(" AND ", conditions),
                updateParams.toArray());
    }

    private static Object[] prepend(String username, List<Object> params) {
        List<Object> all = new ArrayList<>();
        all.add(username);
        all.addAll(params);
        return all.toArray();
    }

    private void appendVisibilityConditions(AppUserPrincipal user, List<String> conditions, List<Object> params) {
        if (user.allOrganizations()) {
            return;
        }
        Set<String> orgCodes = user.organizationCodes();
        if (orgCodes.isEmpty()) {
            conditions.add("1 = 0");
            return;
        }
        List<String> orgConditions = new ArrayList<>();
        for (String org : orgCodes) {
            orgConditions.add(
                    "(organization_code = ? OR FIND_IN_SET(?, REPLACE(COALESCE(organization_codes, ''), ' ', '')) > 0"
                            + " OR source_org_code = ? OR target_org_code = ?)");
            params.add(org);
            params.add(org);
            params.add(org);
            params.add(org);
        }
        conditions.add("(" + String.join(" OR ", orgConditions) + ")");
    }

    static String joinOrganizationCodes(Set<String> organizationCodes) {
        if (organizationCodes == null || organizationCodes.isEmpty()) {
            return null;
        }
        return organizationCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(String::trim)
                .distinct()
                .sorted()
                .collect(Collectors.joining(","));
    }

    static Set<String> organizationCodesFromList(List<String> codes) {
        Set<String> set = new LinkedHashSet<>();
        if (codes == null) {
            return set;
        }
        for (String code : codes) {
            if (code != null && !code.isBlank()) {
                set.add(code.trim());
            }
        }
        return set;
    }

    private ExchangeNotificationRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp readAt = rs.getTimestamp("read_at");
        return new ExchangeNotificationRecord(
                rs.getLong("id"),
                rs.getString("notification_type"),
                rs.getString("direction"),
                rs.getString("audience_scope"),
                rs.getString("source_org_code"),
                rs.getString("target_org_code"),
                rs.getString("organization_code"),
                rs.getString("organization_codes"),
                rs.getString("package_type"),
                rs.getString("batch_id"),
                rs.getInt("person_count"),
                rs.getString("summary"),
                rs.getString("action_tab"),
                rs.getString("status"),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                readAt == null ? null : readAt.toLocalDateTime(),
                rs.getString("read_by"),
                rs.getObject("workflow_id") == null ? null : rs.getLong("workflow_id"),
                rs.getObject("person_uid") == null ? null : rs.getInt("person_uid"),
                rs.getObject("source_id") == null ? null : rs.getInt("source_id"),
                rs.getString("source_type"));
    }

    record ExchangeNotificationInsert(
            String notificationType,
            String direction,
            String audienceScope,
            String sourceOrgCode,
            String targetOrgCode,
            String organizationCode,
            String organizationCodes,
            String packageType,
            String batchId,
            int personCount,
            String summary,
            String actionTab,
            Long workflowId,
            Integer personUid,
            Integer sourceId,
            String sourceType) {

        ExchangeNotificationInsert(
                String notificationType,
                String direction,
                String audienceScope,
                String sourceOrgCode,
                String targetOrgCode,
                String organizationCode,
                String organizationCodes,
                String packageType,
                String batchId,
                int personCount,
                String summary,
                String actionTab) {
            this(notificationType, direction, audienceScope, sourceOrgCode, targetOrgCode, organizationCode,
                    organizationCodes, packageType, batchId, personCount, summary, actionTab,
                    null, null, null, null);
        }
    }
}
