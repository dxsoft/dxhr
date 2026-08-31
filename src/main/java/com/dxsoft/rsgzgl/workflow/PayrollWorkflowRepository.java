package com.dxsoft.rsgzgl.workflow;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.AppUserPrincipal;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
class PayrollWorkflowRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AccessControlService accessControlService;

    PayrollWorkflowRepository(JdbcTemplate jdbcTemplate, AccessControlService accessControlService) {
        this.jdbcTemplate = jdbcTemplate;
        this.accessControlService = accessControlService;
    }

    long insert(PayrollWorkflowInsert insert) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(
                    """
                    INSERT INTO app_payroll_workflow (
                        workflow_no, uid, organization_code, person_code, person_name,
                        source_type, source_id, payroll_module, expected_jslb, status,
                        personnel_approved_at, created_by, summary
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, insert.workflowNo());
            ps.setInt(2, insert.uid());
            ps.setString(3, insert.organizationCode());
            ps.setString(4, insert.personCode());
            ps.setString(5, insert.personName());
            ps.setString(6, insert.sourceType());
            if (insert.sourceId() == null) {
                ps.setNull(7, java.sql.Types.INTEGER);
            } else {
                ps.setInt(7, insert.sourceId());
            }
            ps.setString(8, insert.payrollModule());
            ps.setString(9, insert.expectedJslb());
            ps.setString(10, insert.status());
            ps.setTimestamp(11, Timestamp.valueOf(insert.personnelApprovedAt()));
            ps.setString(12, insert.createdBy());
            ps.setString(13, insert.summary());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? 0L : key.longValue();
    }

    Optional<PayrollWorkflowRecord> findById(long id) {
        List<PayrollWorkflowRecord> rows = jdbcTemplate.query(
                selectColumns() + " WHERE id = ?",
                this::mapRow,
                id);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    boolean existsPendingBySource(String sourceType, Integer sourceId) {
        if (sourceId == null) {
            return false;
        }
        Long count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM app_payroll_workflow
                WHERE source_type = ?
                  AND source_id = ?
                  AND status = ?
                """,
                Long.class,
                sourceType,
                sourceId,
                PayrollWorkflowStatus.PAYROLL_PENDING);
        return count != null && count > 0;
    }

    Optional<PayrollWorkflowRecord> findEarliestPending(int uid, String payrollModule) {
        List<PayrollWorkflowRecord> rows = jdbcTemplate.query(
                selectColumns()
                        + " WHERE uid = ? AND payroll_module = ? AND status = ? ORDER BY personnel_approved_at ASC, id ASC LIMIT 1",
                this::mapRow,
                uid,
                payrollModule,
                PayrollWorkflowStatus.PAYROLL_PENDING);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    boolean complete(long id, String payrollHistoryId, String completedBy, LocalDateTime completedAt) {
        int updated = jdbcTemplate.update(
                """
                UPDATE app_payroll_workflow
                SET status = ?, payroll_history_id = ?, completed_by = ?, payroll_completed_at = ?
                WHERE id = ? AND status = ?
                """,
                PayrollWorkflowStatus.PAYROLL_DONE,
                payrollHistoryId,
                completedBy,
                Timestamp.valueOf(completedAt),
                id,
                PayrollWorkflowStatus.PAYROLL_PENDING);
        return updated > 0;
    }

    PageResponse<PayrollWorkflowRecord> list(
            String statusFilter,
            String payrollModuleFilter,
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        appendScopeConditions(scope, conditions, params);
        if (statusFilter != null && !statusFilter.isBlank() && !"all".equalsIgnoreCase(statusFilter)) {
            conditions.add("status = ?");
            params.add(statusFilter.trim().toUpperCase());
        }
        if (payrollModuleFilter != null && !payrollModuleFilter.isBlank()) {
            conditions.add("payroll_module = ?");
            params.add(payrollModuleFilter.trim());
        }
        if (organizationCode != null && !organizationCode.isBlank()) {
            conditions.add("organization_code = ?");
            params.add(organizationCode.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            conditions.add("(person_code LIKE ? OR person_name LIKE ? OR summary LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        String where = conditions.isEmpty() ? "" : "WHERE " + String.join(" AND ", conditions);
        long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_payroll_workflow " + where,
                Long.class,
                params.toArray());
        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(pageRequest.size());
        queryParams.add(pageRequest.offset());
        List<PayrollWorkflowRecord> content = jdbcTemplate.query(
                selectColumns() + where + " ORDER BY personnel_approved_at DESC, id DESC LIMIT ? OFFSET ?",
                this::mapRow,
                queryParams.toArray());
        return PageResponse.of(content, pageRequest, total);
    }

    long countPendingForScope(boolean approvalCenterView) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        List<String> conditions = new ArrayList<>(List.of("status = ?"));
        List<Object> params = new ArrayList<>(List.of(PayrollWorkflowStatus.PAYROLL_PENDING));
        appendScopeConditions(scope, conditions, params);
        String where = "WHERE " + String.join(" AND ", conditions);
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_payroll_workflow " + where,
                Long.class,
                params.toArray());
        return count == null ? 0L : count;
    }

    private void appendScopeConditions(OrganizationScope scope, List<String> conditions, List<Object> params) {
        if (scope.all()) {
            return;
        }
        Set<String> orgCodes = scope.organizationCodes();
        if (orgCodes.isEmpty()) {
            conditions.add("1 = 0");
            return;
        }
        List<String> orgConditions = new ArrayList<>();
        for (String org : orgCodes) {
            orgConditions.add("organization_code = ?");
            params.add(org);
        }
        conditions.add("(" + String.join(" OR ", orgConditions) + ")");
    }

    private static String selectColumns() {
        return """
                SELECT id, workflow_no, uid, organization_code, person_code, person_name,
                       source_type, source_id, payroll_module, expected_jslb, status,
                       personnel_approved_at, payroll_completed_at, payroll_history_id,
                       created_by, completed_by, summary
                FROM app_payroll_workflow
                """;
    }

    private PayrollWorkflowRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp approvedAt = rs.getTimestamp("personnel_approved_at");
        Timestamp completedAt = rs.getTimestamp("payroll_completed_at");
        int sourceIdRaw = rs.getInt("source_id");
        Integer sourceId = rs.wasNull() ? null : sourceIdRaw;
        return new PayrollWorkflowRecord(
                rs.getLong("id"),
                rs.getString("workflow_no"),
                rs.getInt("uid"),
                rs.getString("organization_code"),
                rs.getString("person_code"),
                rs.getString("person_name"),
                rs.getString("source_type"),
                sourceId,
                rs.getString("payroll_module"),
                rs.getString("expected_jslb"),
                rs.getString("status"),
                approvedAt == null ? null : approvedAt.toLocalDateTime(),
                completedAt == null ? null : completedAt.toLocalDateTime(),
                rs.getString("payroll_history_id"),
                rs.getString("created_by"),
                rs.getString("completed_by"),
                rs.getString("summary"));
    }

    record PayrollWorkflowInsert(
            String workflowNo,
            int uid,
            String organizationCode,
            String personCode,
            String personName,
            String sourceType,
            Integer sourceId,
            String payrollModule,
            String expectedJslb,
            String status,
            LocalDateTime personnelApprovedAt,
            String createdBy,
            String summary) {
    }
}
