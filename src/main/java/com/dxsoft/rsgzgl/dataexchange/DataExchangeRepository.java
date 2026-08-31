package com.dxsoft.rsgzgl.dataexchange;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class DataExchangeRepository {

    static final List<String> DISPATCHABLE_APPROVAL_STATUSES = List.of("申报", "已审", "审批通过");

    private static final String PERSONNEL_BASE_TABLE = "dryjbxx";

    private static final List<String> RELATED_TABLES = List.of(
            "hisbase",
            "dryzwbh",
            "dxl",
            "dndkh",
            "jx",
            "dtgxx",
            "tgqgz2006",
            "hjxx");

    private static final List<String> SUBMISSION_RELATED_TABLES = List.of("dryzwbh", "dxl", "dndkh");

    private final JdbcTemplate jdbcTemplate;
    private final AccessControlService accessControlService;
    private final Map<String, Map<String, String>> tableColumnTypeCache = new ConcurrentHashMap<>();
    private Map<String, Integer> activePersonUidCache;

    DataExchangeRepository(JdbcTemplate jdbcTemplate, AccessControlService accessControlService) {
        this.jdbcTemplate = jdbcTemplate;
        this.accessControlService = accessControlService;
    }

    PageResponse<PersonnelExportRecord> exportPersonnel(
            String organizationCode, String keyword, PageRequest pageRequest) {

        OrganizationScope scope = accessControlService.organizationScope(Optional.ofNullable(organizationCode));
        if (scope.noneScope()) {
            return PageResponse.of(List.of(), pageRequest, 0);
        }

        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (!scope.all()) {
            String placeholders = scope.organizationCodes().stream()
                    .map(c -> "?")
                    .collect(Collectors.joining(", "));
            conditions.add("r.dwbm IN (" + placeholders + ")");
            params.addAll(scope.organizationCodes());
        }

        if (organizationCode != null && !organizationCode.isBlank()) {
            conditions.add("(d.dwmc LIKE ? OR r.dwbm LIKE ?)");
            params.add("%" + organizationCode + "%");
            params.add("%" + organizationCode + "%");
        }

        if (keyword != null && !keyword.isBlank()) {
            conditions.add("(r.grbm LIKE ? OR r.xm LIKE ? OR r.sfzh LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }

        String whereClause = conditions.isEmpty() ? "" : "WHERE " + String.join(" AND ", conditions);

        String countSql = """
                SELECT COUNT(*) FROM dryjbxx r
                LEFT JOIN dwbm d ON r.dwbm = d.dwbm
                %s
                """.formatted(whereClause);

        long total = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

        String querySql = """
                SELECT r.dwbm, d.dwmc, r.grbm, r.xm, r.sfzh, r.xb, r.csny,
                       r.ryfl, r.dwsx, r.gwfl, r.cjgzny, r.zzny, r.gznx,
                       r.xlbm, r.zgxl, r.zwjb, r.zjbm, r.xrzw,
                       (SELECT z.srny FROM dryzwbh z WHERE z.dwbm = r.dwbm AND z.grbm = r.grbm ORDER BY z.srny DESC, z.id DESC LIMIT 1) AS rzny,
                       r.mz, r.zzmm, r.dah,
                       h.zwbm2 AS gw, h.zwgw2 AS zw, h.jbgzjb2 AS jb, h.zwgzdc2 AS dc
                FROM dryjbxx r
                LEFT JOIN dwbm d ON r.dwbm = d.dwbm
                LEFT JOIN hisbase h ON h.id = (
                    SELECT h2.id
                    FROM hisbase h2
                    WHERE h2.dwbm = r.dwbm
                      AND h2.grbm = r.grbm
                      AND (h2.sid IS NULL OR TRIM(h2.sid) = '')
                    ORDER BY COALESCE(h2.jsnf, '') DESC, COALESCE(h2.jsyf, '') DESC, h2.id DESC
                    LIMIT 1
                )
                %s
                ORDER BY r.dwbm, r.grbm
                LIMIT ? OFFSET ?
                """.formatted(whereClause);

        params.add(pageRequest.size());
        params.add(pageRequest.offset());

        List<PersonnelExportRecord> content = jdbcTemplate.query(querySql, this::mapPersonnelExport, params.toArray());

        return PageResponse.of(content, pageRequest, total);
    }

    private PersonnelExportRecord mapPersonnelExport(ResultSet rs, int rowNum) throws SQLException {
        return new PersonnelExportRecord(
                rs.getString("dwbm"),
                rs.getString("dwmc"),
                rs.getString("grbm"),
                rs.getString("xm"),
                rs.getString("sfzh"),
                rs.getString("xb"),
                rs.getString("csny"),
                rs.getString("ryfl"),
                rs.getString("dwsx"),
                rs.getString("gwfl"),
                rs.getString("cjgzny"),
                rs.getString("zzny"),
                rs.getObject("gznx", Integer.class),
                rs.getString("xlbm"),
                rs.getString("zgxl"),
                rs.getString("zwjb"),
                rs.getString("zjbm"),
                rs.getString("xrzw"),
                rs.getString("rzny"),
                rs.getString("mz"),
                rs.getString("zzmm"),
                rs.getString("dah"),
                rs.getString("gw"),
                rs.getString("zw"),
                rs.getString("jb"),
                rs.getString("dc"));
    }

    PageResponse<AnnualReportRecord> exportAnnualReport(
            String organizationCode, String period, String keyword, PageRequest pageRequest) {

        OrganizationScope scope = accessControlService.organizationScope(Optional.ofNullable(organizationCode));
        if (scope.noneScope()) {
            return PageResponse.of(List.of(), pageRequest, 0);
        }

        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (!scope.all()) {
            String placeholders = scope.organizationCodes().stream()
                    .map(c -> "?")
                    .collect(Collectors.joining(", "));
            conditions.add("h.dwbm IN (" + placeholders + ")");
            params.addAll(scope.organizationCodes());
        }

        if (organizationCode != null && !organizationCode.isBlank()) {
            conditions.add("(d.dwmc LIKE ? OR h.dwbm LIKE ?)");
            params.add("%" + organizationCode + "%");
            params.add("%" + organizationCode + "%");
        }

        if (period != null && !period.isBlank()) {
            conditions.add("CONCAT(h.jsnf, h.jsyf) = ?");
            params.add(period);
        }

        if (keyword != null && !keyword.isBlank()) {
            conditions.add("(h.grbm LIKE ? OR h.xm LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }

        String whereClause = conditions.isEmpty() ? "" : "WHERE " + String.join(" AND ", conditions);

        String countSql = """
                SELECT COUNT(*) FROM hisbase h
                LEFT JOIN dryjbxx r ON h.dwbm = r.dwbm AND h.grbm = r.grbm
                LEFT JOIN dwbm d ON h.dwbm = d.dwbm
                %s
                """.formatted(whereClause);

        long total = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

        String querySql = """
                SELECT h.dwbm, d.dwmc, h.grbm, h.xm, r.sfzh, r.xb, r.csny, r.ryfl,
                       h.zwbm2 AS gw, h.zwgw2 AS zw, h.jbgzjb2 AS jb, h.zwgzdc2 AS dc, CONCAT(h.jsnf, h.jsyf) AS ny, h.jslb AS bdlb,
                       h.zwgzse2 AS zwgz, h.jbgzse2 AS jbgz, h.jsdjgz2 AS jsdjgz, h.dfbt2 AS jxgz,
                       h.blfb2 AS blfb, h.jxjt, h.njbt,
                       h.jhljt, h.jsfszwtg2 AS tggz, h.fdgz2 AS fdgz, h.jjjy2 AS jjjy, h.pgbc, h.hj2 AS hj
                FROM hisbase h
                LEFT JOIN dryjbxx r ON h.dwbm = r.dwbm AND h.grbm = r.grbm
                LEFT JOIN dwbm d ON h.dwbm = d.dwbm
                %s
                ORDER BY h.dwbm, h.grbm, h.jsnf, h.jsyf
                LIMIT ? OFFSET ?
                """.formatted(whereClause);

        params.add(pageRequest.size());
        params.add(pageRequest.offset());

        List<AnnualReportRecord> content = jdbcTemplate.query(querySql, this::mapAnnualReport, params.toArray());

        return PageResponse.of(content, pageRequest, total);
    }

    private AnnualReportRecord mapAnnualReport(ResultSet rs, int rowNum) throws SQLException {
        return new AnnualReportRecord(
                rs.getString("dwbm"),
                rs.getString("dwmc"),
                rs.getString("grbm"),
                rs.getString("xm"),
                rs.getString("sfzh"),
                rs.getString("xb"),
                rs.getString("csny"),
                rs.getString("ryfl"),
                rs.getString("gw"),
                rs.getString("zw"),
                rs.getString("jb"),
                rs.getString("dc"),
                rs.getString("ny"),
                rs.getString("bdlb"),
                rs.getObject("zwgz", BigDecimal.class),
                rs.getObject("jbgz", BigDecimal.class),
                rs.getObject("jsdjgz", BigDecimal.class),
                rs.getObject("jxgz", BigDecimal.class),
                rs.getObject("blfb", BigDecimal.class),
                rs.getObject("jxjt", BigDecimal.class),
                rs.getObject("njbt", BigDecimal.class),
                rs.getObject("jhljt", BigDecimal.class),
                rs.getObject("tggz", BigDecimal.class),
                rs.getObject("fdgz", BigDecimal.class),
                rs.getObject("jjjy", BigDecimal.class),
                rs.getObject("pgbc", BigDecimal.class),
                rs.getObject("hj", BigDecimal.class));
    }

    List<PersonnelExportRecord> exportAllPersonnelForDownload(String organizationCode, String keyword) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.ofNullable(organizationCode));
        if (scope.noneScope()) {
            return List.of();
        }

        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (!scope.all()) {
            String placeholders = scope.organizationCodes().stream()
                    .map(c -> "?")
                    .collect(Collectors.joining(", "));
            conditions.add("r.dwbm IN (" + placeholders + ")");
            params.addAll(scope.organizationCodes());
        }

        if (organizationCode != null && !organizationCode.isBlank()) {
            conditions.add("(d.dwmc LIKE ? OR r.dwbm LIKE ?)");
            params.add("%" + organizationCode + "%");
            params.add("%" + organizationCode + "%");
        }

        if (keyword != null && !keyword.isBlank()) {
            conditions.add("(r.grbm LIKE ? OR r.xm LIKE ? OR r.sfzh LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }

        String whereClause = conditions.isEmpty() ? "" : "WHERE " + String.join(" AND ", conditions);

        String querySql = """
                SELECT r.dwbm, d.dwmc, r.grbm, r.xm, r.sfzh, r.xb, r.csny,
                       r.ryfl, r.dwsx, r.gwfl, r.cjgzny, r.zzny, r.gznx,
                       r.xlbm, r.zgxl, r.zwjb, r.zjbm, r.xrzw,
                       (SELECT z.srny FROM dryzwbh z WHERE z.dwbm = r.dwbm AND z.grbm = r.grbm ORDER BY z.srny DESC, z.id DESC LIMIT 1) AS rzny,
                       r.mz, r.zzmm, r.dah,
                       h.zwbm2 AS gw, h.zwgw2 AS zw, h.jbgzjb2 AS jb, h.zwgzdc2 AS dc
                FROM dryjbxx r
                LEFT JOIN dwbm d ON r.dwbm = d.dwbm
                LEFT JOIN hisbase h ON h.id = (
                    SELECT h2.id
                    FROM hisbase h2
                    WHERE h2.dwbm = r.dwbm
                      AND h2.grbm = r.grbm
                      AND (h2.sid IS NULL OR TRIM(h2.sid) = '')
                    ORDER BY COALESCE(h2.jsnf, '') DESC, COALESCE(h2.jsyf, '') DESC, h2.id DESC
                    LIMIT 1
                )
                %s
                ORDER BY r.dwbm, r.grbm
                """.formatted(whereClause);

        return jdbcTemplate.query(querySql, this::mapPersonnelExport, params.toArray());
    }

    List<PersonnelExportRecord> exportPersonnelPackageByOrganizations(
            List<String> organizationCodes,
            boolean includeDescendants,
            String keyword) {
        List<String> resolvedCodes = resolveOrganizationCodes(organizationCodes, includeDescendants);
        if (resolvedCodes.isEmpty()) {
            return List.of();
        }
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String placeholders = resolvedCodes.stream().map(code -> "?").collect(Collectors.joining(", "));
        conditions.add("r.dwbm IN (" + placeholders + ")");
        params.addAll(resolvedCodes);
        if (keyword != null && !keyword.isBlank()) {
            conditions.add("(r.grbm LIKE ? OR r.xm LIKE ? OR r.sfzh LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        String whereClause = "WHERE " + String.join(" AND ", conditions);
        String querySql = """
                SELECT r.dwbm, d.dwmc, r.grbm, r.xm, r.sfzh, r.xb, r.csny,
                       r.ryfl, r.dwsx, r.gwfl, r.cjgzny, r.zzny, r.gznx,
                       r.xlbm, r.zgxl, r.zwjb, r.zjbm, r.xrzw,
                       (SELECT z.srny FROM dryzwbh z WHERE z.dwbm = r.dwbm AND z.grbm = r.grbm ORDER BY z.srny DESC, z.id DESC LIMIT 1) AS rzny,
                       r.mz, r.zzmm, r.dah,
                       h.zwbm2 AS gw, h.zwgw2 AS zw, h.jbgzjb2 AS jb, h.zwgzdc2 AS dc
                FROM dryjbxx r
                LEFT JOIN dwbm d ON r.dwbm = d.dwbm
                LEFT JOIN hisbase h ON h.id = (
                    SELECT h2.id
                    FROM hisbase h2
                    WHERE h2.dwbm = r.dwbm
                      AND h2.grbm = r.grbm
                      AND (h2.sid IS NULL OR TRIM(h2.sid) = '')
                    ORDER BY COALESCE(h2.jsnf, '') DESC, COALESCE(h2.jsyf, '') DESC, h2.id DESC
                    LIMIT 1
                )
                %s
                ORDER BY r.dwbm, r.grbm
                """.formatted(whereClause);
        return jdbcTemplate.query(querySql, this::mapPersonnelExport, params.toArray());
    }

    List<PersonnelExportRecord> exportApprovedPersonnelPackageByOrganizations(
            List<String> organizationCodes,
            boolean includeDescendants,
            String keyword) {
        return exportApprovedPersonnelPackageByOrganizations(
                organizationCodes, includeDescendants, keyword, DISPATCHABLE_APPROVAL_STATUSES);
    }

    List<PersonnelExportRecord> exportApprovedPersonnelPackageByOrganizations(
            List<String> organizationCodes,
            boolean includeDescendants,
            String keyword,
            List<String> approvalStatuses) {
        List<String> resolvedCodes = resolveOrganizationCodes(organizationCodes, includeDescendants);
        if (resolvedCodes.isEmpty()) {
            return List.of();
        }
        List<String> statuses = normalizeStatuses(approvalStatuses);
        if (statuses.isEmpty()) {
            return List.of();
        }
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String placeholders = resolvedCodes.stream().map(code -> "?").collect(Collectors.joining(", "));
        conditions.add("r.dwbm IN (" + placeholders + ")");
        params.addAll(resolvedCodes);
        conditions.add(payrollStatusCondition("r.dwbm", "r.grbm", statuses));
        params.addAll(statuses);
        if (keyword != null && !keyword.isBlank()) {
            conditions.add("(r.grbm LIKE ? OR r.xm LIKE ? OR r.sfzh LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        return jdbcTemplate.query(buildPersonnelPackageQuery(conditions), this::mapPersonnelExport, params.toArray());
    }

    List<DataExchangeController.ApprovalStatusCount> countCurrentPayrollStatuses(
            List<String> organizationCodes,
            boolean includeDescendants,
            String keyword) {
        List<String> resolvedCodes = resolveOrganizationCodes(organizationCodes, includeDescendants);
        if (resolvedCodes.isEmpty()) {
            return List.of();
        }
        List<Object> params = new ArrayList<>();
        String placeholders = resolvedCodes.stream().map(code -> "?").collect(Collectors.joining(", "));
        params.addAll(resolvedCodes);
        StringBuilder sql = new StringBuilder("""
                SELECT COALESCE(NULLIF(TRIM(h.bbz), ''), '(空)') AS status_label, COUNT(*) AS cnt
                FROM hisbase h
                INNER JOIN dryjbxx r ON h.dwbm = r.dwbm AND h.grbm = r.grbm
                WHERE r.dwbm IN (%s)
                  AND (h.sid IS NULL OR TRIM(h.sid) = '')
                """.formatted(placeholders));
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (r.grbm LIKE ? OR r.xm LIKE ? OR r.sfzh LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        sql.append(" GROUP BY COALESCE(NULLIF(TRIM(h.bbz), ''), '(空)') ORDER BY cnt DESC");
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) ->
                new DataExchangeController.ApprovalStatusCount(rs.getString("status_label"), rs.getInt("cnt")),
                params.toArray());
    }

    List<PersonnelExportRecord> exportSelectedApprovedPersonnel(List<DataExchangeController.PersonKey> selectedPersonnel) {
        return exportSelectedPersonnelByStatuses(selectedPersonnel, DISPATCHABLE_APPROVAL_STATUSES);
    }

    List<PersonnelExportRecord> exportSelectedPersonnelByStatuses(
            List<DataExchangeController.PersonKey> selectedPersonnel,
            List<String> approvalStatuses) {
        if (selectedPersonnel == null || selectedPersonnel.isEmpty()) {
            return List.of();
        }
        List<String> statuses = normalizeStatuses(approvalStatuses);
        if (statuses.isEmpty()) {
            return List.of();
        }
        List<PersonnelExportRecord> rows = new ArrayList<>();
        String statusPlaceholders = statuses.stream().map(s -> "?").collect(Collectors.joining(", "));
        for (DataExchangeController.PersonKey key : selectedPersonnel) {
            List<Object> params = new ArrayList<>();
            params.add(key.organizationCode());
            params.add(key.personCode());
            params.addAll(statuses);
            rows.addAll(jdbcTemplate.query("""
                    SELECT r.dwbm, d.dwmc, r.grbm, r.xm, r.sfzh, r.xb, r.csny,
                           r.ryfl, r.dwsx, r.gwfl, r.cjgzny, r.zzny, r.gznx,
                           r.xlbm, r.zgxl, r.zwjb, r.zjbm, r.xrzw,
                           (SELECT z.srny FROM dryzwbh z WHERE z.dwbm = r.dwbm AND z.grbm = r.grbm ORDER BY z.srny DESC, z.id DESC LIMIT 1) AS rzny,
                           r.mz, r.zzmm, r.dah,
                           h.zwbm2 AS gw, h.zwgw2 AS zw, h.jbgzjb2 AS jb, h.zwgzdc2 AS dc
                    FROM dryjbxx r
                    LEFT JOIN dwbm d ON r.dwbm = d.dwbm
                    LEFT JOIN hisbase h ON h.id = (
                    SELECT h2.id
                    FROM hisbase h2
                    WHERE h2.dwbm = r.dwbm
                      AND h2.grbm = r.grbm
                      AND (h2.sid IS NULL OR TRIM(h2.sid) = '')
                    ORDER BY COALESCE(h2.jsnf, '') DESC, COALESCE(h2.jsyf, '') DESC, h2.id DESC
                    LIMIT 1
                )
                    WHERE r.dwbm = ? AND r.grbm = ?
                      AND EXISTS (
                          SELECT 1 FROM hisbase ha
                          WHERE ha.dwbm = r.dwbm AND ha.grbm = r.grbm
                            AND (ha.sid IS NULL OR TRIM(ha.sid) = '')
                            AND TRIM(ha.bbz) IN (%s)
                      )
                    """.formatted(statusPlaceholders), this::mapPersonnelExport, params.toArray()));
        }
        return rows;
    }

    int revertPayrollApprovalDispatched(List<PersonnelExportRecord> personnelRows) {
        if (personnelRows == null || personnelRows.isEmpty()) {
            return 0;
        }
        int updated = 0;
        for (PersonnelExportRecord person : personnelRows) {
            updated += jdbcTemplate.update("""
                    UPDATE hisbase
                    SET bbz = '已审'
                    WHERE dwbm = ? AND grbm = ? AND (sid IS NULL OR TRIM(sid) = '')
                      AND TRIM(bbz) = '已下发'
                    """, person.organizationCode(), person.personCode());
        }
        return updated;
    }

    private List<String> normalizeStatuses(List<String> approvalStatuses) {
        if (approvalStatuses == null || approvalStatuses.isEmpty()) {
            return DISPATCHABLE_APPROVAL_STATUSES;
        }
        return approvalStatuses.stream()
                .filter(item -> item != null && !item.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    List<PersonnelExportRecord> exportSelectedPersonnel(List<DataExchangeController.PersonKey> selectedPersonnel) {
        if (selectedPersonnel == null || selectedPersonnel.isEmpty()) {
            return List.of();
        }
        List<PersonnelExportRecord> rows = new ArrayList<>();
        for (DataExchangeController.PersonKey key : selectedPersonnel) {
            rows.addAll(jdbcTemplate.query("""
                    SELECT r.dwbm, d.dwmc, r.grbm, r.xm, r.sfzh, r.xb, r.csny,
                           r.ryfl, r.dwsx, r.gwfl, r.cjgzny, r.zzny, r.gznx,
                           r.xlbm, r.zgxl, r.zwjb, r.zjbm, r.xrzw,
                           (SELECT z.srny FROM dryzwbh z WHERE z.dwbm = r.dwbm AND z.grbm = r.grbm ORDER BY z.srny DESC, z.id DESC LIMIT 1) AS rzny,
                           r.mz, r.zzmm, r.dah,
                           h.zwbm2 AS gw, h.zwgw2 AS zw, h.jbgzjb2 AS jb, h.zwgzdc2 AS dc
                    FROM dryjbxx r
                    LEFT JOIN dwbm d ON r.dwbm = d.dwbm
                    LEFT JOIN hisbase h ON h.id = (
                    SELECT h2.id
                    FROM hisbase h2
                    WHERE h2.dwbm = r.dwbm
                      AND h2.grbm = r.grbm
                      AND (h2.sid IS NULL OR TRIM(h2.sid) = '')
                    ORDER BY COALESCE(h2.jsnf, '') DESC, COALESCE(h2.jsyf, '') DESC, h2.id DESC
                    LIMIT 1
                )
                    WHERE r.dwbm = ? AND r.grbm = ?
                    """, this::mapPersonnelExport, key.organizationCode(), key.personCode()));
        }
        return rows;
    }

    List<DataExchangeService.ExchangeTable> exportRelatedTables(List<PersonnelExportRecord> personnelRows) {
        if (personnelRows == null || personnelRows.isEmpty()) {
            return List.of();
        }
        List<DataExchangeService.ExchangeTable> tables = new ArrayList<>();
        tables.add(new DataExchangeService.ExchangeTable(
                PERSONNEL_BASE_TABLE,
                exportRowsForPeople(PERSONNEL_BASE_TABLE, personnelRows, null)));
        for (String table : RELATED_TABLES) {
            tables.add(new DataExchangeService.ExchangeTable(table, exportRowsForPeople(table, personnelRows, null)));
        }
        return tables;
    }

    List<DataExchangeService.ExchangeTable> exportPayrollTables(List<PersonnelExportRecord> personnelRows) {
        if (personnelRows == null || personnelRows.isEmpty()) {
            return List.of();
        }
        return List.of(new DataExchangeService.ExchangeTable(
                "hisbase",
                exportRowsForPeople("hisbase", personnelRows, "jsnf, jsyf, hj2, id")));
    }

    List<DataExchangeService.ExchangeTable> exportSubmissionRelatedTables(List<PersonnelExportRecord> personnelRows) {
        if (personnelRows == null || personnelRows.isEmpty()) {
            return List.of();
        }
        List<DataExchangeService.ExchangeTable> tables = new ArrayList<>();
        tables.add(new DataExchangeService.ExchangeTable(
                PERSONNEL_BASE_TABLE,
                exportRowsForPeople(PERSONNEL_BASE_TABLE, personnelRows, null)));
        for (String table : SUBMISSION_RELATED_TABLES) {
            tables.add(new DataExchangeService.ExchangeTable(table, exportRowsForPeople(table, personnelRows, null)));
        }
        return tables;
    }

    private List<Map<String, Object>> exportRowsForPeople(
            String tableName,
            List<PersonnelExportRecord> personnelRows,
            String orderBy) {
        List<Map<String, Object>> rows = new ArrayList<>();
        final int batchSize = 80;
        for (int offset = 0; offset < personnelRows.size(); offset += batchSize) {
            List<PersonnelExportRecord> batch = personnelRows.subList(
                    offset, Math.min(offset + batchSize, personnelRows.size()));
            StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName).append(" WHERE (dwbm, grbm) IN (");
            List<Object> params = new ArrayList<>();
            for (int i = 0; i < batch.size(); i++) {
                if (i > 0) {
                    sql.append(',');
                }
                sql.append("(?,?)");
                params.add(batch.get(i).organizationCode());
                params.add(batch.get(i).personCode());
            }
            sql.append(')');
            if (orderBy != null && !orderBy.isBlank()) {
                sql.append(" ORDER BY ").append(orderBy);
            }
            rows.addAll(jdbcTemplate.queryForList(sql.toString(), params.toArray()));
        }
        return rows;
    }

    int replaceReceivedPersonnel(List<PersonnelExportRecord> rows, List<DataExchangeService.ExchangeTable> relatedTables) {
        int count = 0;
        for (PersonnelExportRecord row : rows) {
            deletePersonRelatedRows(row.organizationCode(), row.personCode());
            jdbcTemplate.update("DELETE FROM dryjbxx WHERE dwbm = ? AND grbm = ?", row.organizationCode(), row.personCode());
            insertPersonnelFromPackage(
                    row, relatedTables, row.organizationCode(), row.personCode(), false);
            insertRelatedRowsForPerson(relatedTables, row.organizationCode(), row.personCode(), row.organizationCode(), row.personCode(), false);
            count++;
        }
        return count;
    }

    int appendReceivedPersonnel(List<PersonnelExportRecord> rows, List<DataExchangeService.ExchangeTable> relatedTables, String targetOrganizationCode) {
        int count = 0;
        int nextCode = nextPersonCode(targetOrganizationCode);
        for (PersonnelExportRecord row : rows) {
            String personCode = "%05d".formatted(nextCode++);
            insertPersonnelFromPackage(row, relatedTables, targetOrganizationCode, personCode, true);
            insertRelatedRowsForPerson(relatedTables, row.organizationCode(), row.personCode(), targetOrganizationCode, personCode, true);
            count++;
        }
        return count;
    }

    boolean personExists(String organizationCode, String personCode) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dryjbxx WHERE dwbm = ? AND grbm = ?",
                Integer.class,
                organizationCode,
                personCode);
        return count != null && count > 0;
    }

    Set<String> existingPersonKeys(List<PersonnelExportRecord> rows) {
        if (rows == null || rows.isEmpty()) {
            return Set.of();
        }
        Set<String> found = new HashSet<>();
        final int chunkSize = 400;
        for (int offset = 0; offset < rows.size(); offset += chunkSize) {
            List<PersonnelExportRecord> chunk = rows.subList(offset, Math.min(offset + chunkSize, rows.size()));
            StringBuilder sql = new StringBuilder("SELECT dwbm, grbm FROM dryjbxx WHERE (dwbm, grbm) IN (");
            List<Object> args = new ArrayList<>(chunk.size() * 2);
            for (int i = 0; i < chunk.size(); i++) {
                if (i > 0) {
                    sql.append(',');
                }
                sql.append("(?,?)");
                PersonnelExportRecord row = chunk.get(i);
                args.add(row.organizationCode());
                args.add(row.personCode());
            }
            sql.append(')');
            List<String> keys = jdbcTemplate.query(
                    sql.toString(),
                    args.toArray(),
                    (rs, rowNum) -> trimKey(rs.getString("dwbm")) + "|" + trimKey(rs.getString("grbm")));
            found.addAll(keys);
        }
        return found;
    }

    private static String trimKey(String value) {
        return value == null ? "" : value.trim();
    }

    boolean organizationExists(String organizationCode) {
        if (organizationCode == null || organizationCode.isBlank()) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dwbm WHERE dwbm = ?",
                Integer.class,
                organizationCode);
        return count != null && count > 0;
    }

    String previewNextPersonCode(String organizationCode) {
        if (organizationCode == null || organizationCode.isBlank()) {
            return "";
        }
        return "%05d".formatted(nextPersonCode(organizationCode));
    }

    List<DataExchangeController.CodeMapping> plannedAppendMappings(List<PersonnelExportRecord> rows, String targetOrganizationCode) {
        List<DataExchangeController.CodeMapping> mappings = new ArrayList<>();
        int nextCode = nextPersonCode(targetOrganizationCode);
        for (PersonnelExportRecord row : rows) {
            mappings.add(new DataExchangeController.CodeMapping(
                    row.organizationCode(),
                    row.personCode(),
                    targetOrganizationCode,
                    "%05d".formatted(nextCode++),
                    row.name()));
        }
        return mappings;
    }

    private List<String> resolveOrganizationCodes(List<String> organizationCodes, boolean includeDescendants) {
        List<String> selected = organizationCodes == null ? List.of() : organizationCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        if (selected.isEmpty()) {
            return List.of();
        }
        if (!includeDescendants) {
            return selected;
        }
        List<String> allCodes = jdbcTemplate.queryForList("SELECT dwbm FROM dwbm ORDER BY dwbm", String.class);
        Set<String> resolved = allCodes.stream()
                .filter(code -> selected.stream().anyMatch(code::startsWith))
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        return new ArrayList<>(resolved);
    }

    private int nextPersonCode(String organizationCode) {
        return jdbcTemplate.queryForList(
                        "SELECT grbm FROM dryjbxx WHERE dwbm = ?",
                        String.class,
                        organizationCode)
                .stream()
                .map(this::parseIntOrZero)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }

    private int parseIntOrZero(String value) {
        try {
            return Integer.parseInt(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Integer findPersonUid(String organizationCode, String personCode) {
        return jdbcTemplate.queryForList(
                        "SELECT uid FROM dryjbxx WHERE dwbm = ? AND grbm = ?",
                        Integer.class,
                        organizationCode,
                        personCode)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private void insertPersonnelFromPackage(
            PersonnelExportRecord row,
            ExchangePackageIndex packageIndex,
            String targetOrganizationCode,
            String targetPersonCode,
            boolean appendMode) {
        Optional<Map<String, Object>> baseRow = packageIndex.dryjbxxRow(row.organizationCode(), row.personCode());
        if (baseRow.isPresent()) {
            insertGenericRelatedRow(
                    PERSONNEL_BASE_TABLE,
                    baseRow.get(),
                    targetOrganizationCode,
                    targetPersonCode,
                    appendMode);
            return;
        }
        insertPersonnel(row, targetOrganizationCode, targetPersonCode);
    }

    private void insertPersonnelFromPackage(
            PersonnelExportRecord row,
            List<DataExchangeService.ExchangeTable> relatedTables,
            String targetOrganizationCode,
            String targetPersonCode,
            boolean appendMode) {
        insertPersonnelFromPackage(
                row,
                ExchangePackageIndex.fromRelatedTables(relatedTables),
                targetOrganizationCode,
                targetPersonCode,
                appendMode);
    }

    private Optional<Map<String, Object>> findPersonnelBaseRow(
            List<DataExchangeService.ExchangeTable> relatedTables,
            String organizationCode,
            String personCode) {
        if (relatedTables == null || relatedTables.isEmpty()) {
            return Optional.empty();
        }
        for (DataExchangeService.ExchangeTable table : relatedTables) {
            if (!PERSONNEL_BASE_TABLE.equalsIgnoreCase(table.tableName()) || table.rows() == null) {
                continue;
            }
            for (Map<String, Object> sourceRow : table.rows()) {
                if (matchesPerson(sourceRow, organizationCode, personCode)) {
                    return Optional.of(sourceRow);
                }
            }
        }
        return Optional.empty();
    }

    private void insertPersonnel(PersonnelExportRecord row, String organizationCode, String personCode) {
        Map<String, String> columnTypes = tableColumnTypes("dryjbxx");
        Map<String, Object> values = new LinkedHashMap<>();
        for (Map.Entry<String, String> column : columnTypes.entrySet()) {
            String name = column.getKey();
            if ("uid".equalsIgnoreCase(name) || "id".equalsIgnoreCase(name)) {
                continue;
            }
            values.put(name, personnelColumnValue(name, column.getValue(), row, organizationCode, personCode));
        }
        String columns = values.keySet().stream()
                .map(this::quoteIdentifier)
                .collect(Collectors.joining(", "));
        String placeholders = values.keySet().stream().map(key -> "?").collect(Collectors.joining(", "));
        jdbcTemplate.update("INSERT INTO dryjbxx (" + columns + ") VALUES (" + placeholders + ")", values.values().toArray());
    }

    private Object personnelColumnValue(String columnName, String typeName, PersonnelExportRecord row, String organizationCode, String personCode) {
        return switch (columnName.toLowerCase(Locale.ROOT)) {
            case "dwbm" -> organizationCode;
            case "grbm" -> personCode;
            case "xm" -> valueOrDefault(row.name());
            case "sfzh" -> valueOrDefault(row.idCard());
            case "xb" -> valueOrDefault(row.gender());
            case "csny" -> valueOrDefault(row.birthYearMonth());
            case "ryfl" -> valueOrDefault(row.personnelCategory());
            case "dwsx" -> valueOrDefault(row.organizationType());
            case "gwfl" -> valueOrDefault(row.postCategory());
            case "cjgzny" -> valueOrDefault(row.workStart());
            case "zzny" -> valueOrDefault(row.regularization());
            case "gznx" -> row.salaryYears() == null ? 0 : row.salaryYears();
            case "xlbm" -> valueOrDefault(row.educationCode());
            case "zgxl" -> valueOrDefault(row.highestEducation());
            case "zwjb" -> valueOrDefault(row.positionLevel());
            case "zjbm" -> valueOrDefault(row.rankCode());
            case "xrzw" -> valueOrDefault(row.currentPosition());
            case "dah" -> valueOrDefault(row.archiveNumber());
            case "mz" -> valueOrDefault(row.ethnicity());
            case "zzmm" -> valueOrDefault(row.politicalStatus());
            default -> defaultValueForType(typeName);
        };
    }

    private String valueOrDefault(String value) {
        return value == null ? "" : value;
    }

    private Object defaultValueForType(String typeName) {
        String normalized = typeName == null ? "" : typeName.toUpperCase(Locale.ROOT);
        if (normalized.contains("INT") || normalized.contains("DECIMAL") || normalized.contains("NUMERIC")
                || normalized.contains("DOUBLE") || normalized.contains("FLOAT")) {
            return 0;
        }
        if (normalized.contains("BIT") || normalized.contains("BOOL")) {
            return false;
        }
        return "";
    }

    private void deletePersonRelatedRows(String organizationCode, String personCode) {
        for (String table : RELATED_TABLES) {
            jdbcTemplate.update("DELETE FROM " + table + " WHERE dwbm = ? AND grbm = ?", organizationCode, personCode);
        }
    }

    private void insertRelatedRowsForPerson(
            ExchangePackageIndex packageIndex,
            String sourceOrganizationCode,
            String sourcePersonCode,
            String targetOrganizationCode,
            String targetPersonCode,
            boolean appendMode) {
        if (packageIndex == null) {
            return;
        }
        for (String tableName : RELATED_TABLES) {
            for (Map<String, Object> sourceRow : packageIndex.relatedRows(
                    sourceOrganizationCode, sourcePersonCode, tableName)) {
                insertGenericRelatedRow(tableName, sourceRow, targetOrganizationCode, targetPersonCode, appendMode);
            }
        }
    }

    private void insertRelatedRowsForPerson(
            List<DataExchangeService.ExchangeTable> relatedTables,
            String sourceOrganizationCode,
            String sourcePersonCode,
            String targetOrganizationCode,
            String targetPersonCode,
            boolean appendMode) {
        insertRelatedRowsForPerson(
                ExchangePackageIndex.fromRelatedTables(relatedTables),
                sourceOrganizationCode,
                sourcePersonCode,
                targetOrganizationCode,
                targetPersonCode,
                appendMode);
    }

    private boolean matchesPerson(Map<String, Object> row, String organizationCode, String personCode) {
        return textValue(row, "dwbm").equals(organizationCode) && textValue(row, "grbm").equals(personCode);
    }

    private void insertGenericRelatedRow(
            String tableName,
            Map<String, Object> sourceRow,
            String targetOrganizationCode,
            String targetPersonCode,
            boolean appendMode) {
        Map<String, String> columnTypes = tableColumnTypes(tableName);
        Map<String, Object> row = new LinkedHashMap<>();
        for (Map.Entry<String, String> column : columnTypes.entrySet()) {
            Object value = valueIgnoreCase(sourceRow, column.getKey());
            if ("dwbm".equalsIgnoreCase(column.getKey())) {
                value = targetOrganizationCode;
            } else if ("grbm".equalsIgnoreCase(column.getKey())) {
                value = targetPersonCode;
            } else if ("id".equalsIgnoreCase(column.getKey())) {
                if (isIntegerType(column.getValue())) {
                    continue;
                }
                if (appendMode || value == null || String.valueOf(value).isBlank()) {
                    value = UUID.randomUUID().toString().toUpperCase(Locale.ROOT);
                }
            } else if ("uid".equalsIgnoreCase(column.getKey())) {
                if (PERSONNEL_BASE_TABLE.equalsIgnoreCase(tableName)) {
                    continue;
                }
                value = cachedPersonUid(targetOrganizationCode, targetPersonCode);
            }
            if (value != null) {
                row.put(column.getKey(), value);
            }
        }
        if (row.isEmpty()) {
            return;
        }
        String columns = row.keySet().stream()
                .map(this::quoteIdentifier)
                .collect(Collectors.joining(", "));
        String placeholders = row.keySet().stream().map(key -> "?").collect(Collectors.joining(", "));
        jdbcTemplate.update("INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")", row.values().toArray());
    }

    private String quoteIdentifier(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }

    private Map<String, String> tableColumnTypes(String tableName) {
        String cacheKey = tableName.toLowerCase(Locale.ROOT);
        return tableColumnTypeCache.computeIfAbsent(cacheKey, this::loadTableColumnTypes);
    }

    private Map<String, String> loadTableColumnTypes(String tableName) {
        return jdbcTemplate.execute((ConnectionCallback<Map<String, String>>) connection -> {
            Map<String, String> columns = new LinkedHashMap<>();
            String catalog = connection.getCatalog();
            try (ResultSet rs = connection.getMetaData().getColumns(catalog, null, tableName, null)) {
                while (rs.next()) {
                    columns.put(rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME"));
                }
            }
            if (columns.isEmpty()) {
                try (ResultSet rs = connection.getMetaData().getColumns(catalog, null, tableName.toUpperCase(Locale.ROOT), null)) {
                    while (rs.next()) {
                        columns.put(rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME"));
                    }
                }
            }
            return columns;
        });
    }

    private Integer cachedPersonUid(String organizationCode, String personCode) {
        if (activePersonUidCache != null) {
            String key = ExchangePackageIndex.personKey(organizationCode, personCode);
            return activePersonUidCache.computeIfAbsent(key, ignored -> findPersonUid(organizationCode, personCode));
        }
        return findPersonUid(organizationCode, personCode);
    }

    private boolean isIntegerType(String typeName) {
        String normalized = typeName == null ? "" : typeName.toUpperCase(Locale.ROOT);
        return normalized.contains("INT") || normalized.contains("SERIAL");
    }

    private Object valueIgnoreCase(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String textValue(Map<String, Object> row, String key) {
        Object value = valueIgnoreCase(row, key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    void markPayrollApprovalDispatched(List<PersonnelExportRecord> personnelRows) {
        if (personnelRows == null || personnelRows.isEmpty()) {
            return;
        }
        for (PersonnelExportRecord person : personnelRows) {
            jdbcTemplate.update("""
                    UPDATE hisbase
                    SET bbz = '已下发'
                    WHERE dwbm = ? AND grbm = ? AND (sid IS NULL OR TRIM(sid) = '')
                      AND TRIM(bbz) IN ('已审', '审批通过', '申报')
                    """, person.organizationCode(), person.personCode());
        }
    }

    void markPayrollSubmitted(List<PersonnelExportRecord> personnelRows) {
        if (personnelRows == null || personnelRows.isEmpty()) {
            return;
        }
        for (PersonnelExportRecord person : personnelRows) {
            jdbcTemplate.update("""
                    UPDATE hisbase
                    SET bbz = '申报'
                    WHERE dwbm = ? AND grbm = ? AND (sid IS NULL OR TRIM(sid) = '')
                    """, person.organizationCode(), person.personCode());
        }
    }

    int applyApprovedSubmission(
            List<PersonnelExportRecord> personnelRows,
            List<DataExchangeService.ExchangeTable> payrollTables,
            List<DataExchangeService.ExchangeTable> relatedTables) {
        int count = 0;
        for (PersonnelExportRecord row : personnelRows) {
            if (!personExists(row.organizationCode(), row.personCode())) {
                insertPersonnelFromPackage(
                        row, relatedTables, row.organizationCode(), row.personCode(), false);
            }
            jdbcTemplate.update("DELETE FROM hisbase WHERE dwbm = ? AND grbm = ?", row.organizationCode(), row.personCode());
            insertPayrollRowsForPerson(payrollTables, row.organizationCode(), row.personCode());
            insertSubmissionRelatedRowsForPerson(relatedTables, row.organizationCode(), row.personCode());
            markPayrollApproved(row.organizationCode(), row.personCode());
            count++;
        }
        return count;
    }

    int applyApprovalReceive(
            List<PersonnelExportRecord> personnelRows,
            List<DataExchangeService.ExchangeTable> relatedTables,
            String mode) {
        ExchangePackageIndex packageIndex = ExchangePackageIndex.fromRelatedTables(relatedTables);
        Set<String> existingPersonKeys = existingPersonKeys(personnelRows);
        activePersonUidCache = new HashMap<>();
        try {
            if ("REPLACE".equalsIgnoreCase(mode)) {
                int count = 0;
                for (PersonnelExportRecord row : personnelRows) {
                    replaceApprovalReceivePerson(row, packageIndex);
                    count++;
                }
                return count;
            }
            if (!"UPDATE".equalsIgnoreCase(mode)) {
                throw new IllegalArgumentException("不支持的审批接收模式：" + mode);
            }
            int count = 0;
            for (PersonnelExportRecord row : personnelRows) {
                applyApprovalReceiveUpdate(
                        row,
                        packageIndex,
                        existingPersonKeys.contains(ExchangePackageIndex.personKey(
                                row.organizationCode(), row.personCode())));
                count++;
            }
            return count;
        } finally {
            activePersonUidCache = null;
        }
    }

    Map<String, Map<String, Integer>> findPersonnelReformFieldValuesBatch(List<PersonnelExportRecord> rows) {
        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }
        Map<String, Map<String, Integer>> found = new HashMap<>();
        final int chunkSize = 400;
        for (int offset = 0; offset < rows.size(); offset += chunkSize) {
            List<PersonnelExportRecord> chunk = rows.subList(offset, Math.min(offset + chunkSize, rows.size()));
            StringBuilder sql = new StringBuilder("""
                    SELECT dwbm, grbm,
                           COALESCE(bjglxlnx, 0) AS bjglxlnx,
                           COALESCE(zdgznx, 0) AS zdgznx,
                           COALESCE(gznx, 0) AS gznx
                    FROM dryjbxx
                    WHERE (dwbm, grbm) IN (
                    """);
            List<Object> args = new ArrayList<>(chunk.size() * 2);
            for (int i = 0; i < chunk.size(); i++) {
                if (i > 0) {
                    sql.append(',');
                }
                sql.append("(?,?)");
                PersonnelExportRecord row = chunk.get(i);
                args.add(row.organizationCode());
                args.add(row.personCode());
            }
            sql.append(')');
            jdbcTemplate.query(sql.toString(), args.toArray(), rs -> {
                while (rs.next()) {
                    String key = ExchangePackageIndex.personKey(
                            trimKey(rs.getString("dwbm")),
                            trimKey(rs.getString("grbm")));
                    found.put(key, Map.of(
                            "bjglxlnx", rs.getInt("bjglxlnx"),
                            "zdgznx", rs.getInt("zdgznx"),
                            "gznx", rs.getInt("gznx")));
                }
                return null;
            });
        }
        return found;
    }

    Set<String> existingOrganizationCodes(List<PersonnelExportRecord> rows) {
        if (rows == null || rows.isEmpty()) {
            return Set.of();
        }
        List<String> codes = rows.stream()
                .map(PersonnelExportRecord::organizationCode)
                .filter(code -> code != null && !code.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        if (codes.isEmpty()) {
            return Set.of();
        }
        String placeholders = codes.stream().map(code -> "?").collect(Collectors.joining(", "));
        List<String> found = jdbcTemplate.queryForList(
                "SELECT dwbm FROM dwbm WHERE dwbm IN (" + placeholders + ")",
                String.class,
                codes.toArray());
        return new HashSet<>(found);
    }

    Map<String, PersonnelExportRecord> findPersonnelExportRecordsBatch(List<PersonnelExportRecord> rows) {
        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }
        Map<String, PersonnelExportRecord> found = new LinkedHashMap<>();
        final int chunkSize = 200;
        for (int offset = 0; offset < rows.size(); offset += chunkSize) {
            List<PersonnelExportRecord> chunk = rows.subList(offset, Math.min(offset + chunkSize, rows.size()));
            StringBuilder inClause = new StringBuilder("(r.dwbm, r.grbm) IN (");
            List<Object> params = new ArrayList<>(chunk.size() * 2);
            for (int i = 0; i < chunk.size(); i++) {
                if (i > 0) {
                    inClause.append(',');
                }
                inClause.append("(?,?)");
                PersonnelExportRecord row = chunk.get(i);
                params.add(row.organizationCode());
                params.add(row.personCode());
            }
            inClause.append(')');
            String sql = buildPersonnelPackageQuery(List.of(inClause.toString()));
            jdbcTemplate.query(sql, this::mapPersonnelExport, params.toArray()).forEach(record ->
                    found.put(ExchangePackageIndex.personKey(record.organizationCode(), record.personCode()), record));
        }
        return found;
    }

    Map<String, Integer> findPersonnelReformFieldValues(String organizationCode, String personCode) {
        return jdbcTemplate.query("""
                SELECT COALESCE(bjglxlnx, 0) AS bjglxlnx,
                       COALESCE(zdgznx, 0) AS zdgznx,
                       COALESCE(gznx, 0) AS gznx
                FROM dryjbxx
                WHERE dwbm = ? AND grbm = ?
                LIMIT 1
                """, rs -> {
            if (!rs.next()) {
                return Map.of();
            }
            return Map.of(
                    "bjglxlnx", rs.getInt("bjglxlnx"),
                    "zdgznx", rs.getInt("zdgznx"),
                    "gznx", rs.getInt("gznx"));
        }, organizationCode, personCode);
    }

    private void applyApprovalReceiveUpdate(
            PersonnelExportRecord row,
            ExchangePackageIndex packageIndex,
            boolean personExists) {
        if (personExists) {
            updatePersonnelFromPackage(packageIndex, row.organizationCode(), row.personCode());
        } else {
            insertPersonnelFromPackage(
                    row, packageIndex, row.organizationCode(), row.personCode(), false);
        }
        replaceAllRelatedTablesForPerson(packageIndex, row.organizationCode(), row.personCode());
        markPayrollApproved(row.organizationCode(), row.personCode());
    }

    private void replaceApprovalReceivePerson(
            PersonnelExportRecord row,
            ExchangePackageIndex packageIndex) {
        deletePersonRelatedRows(row.organizationCode(), row.personCode());
        jdbcTemplate.update("DELETE FROM dryjbxx WHERE dwbm = ? AND grbm = ?", row.organizationCode(), row.personCode());
        insertPersonnelFromPackage(row, packageIndex, row.organizationCode(), row.personCode(), false);
        insertRelatedRowsForPerson(
                packageIndex,
                row.organizationCode(),
                row.personCode(),
                row.organizationCode(),
                row.personCode(),
                false);
        markPayrollApproved(row.organizationCode(), row.personCode());
    }

    private void replaceAllRelatedTablesForPerson(
            ExchangePackageIndex packageIndex,
            String organizationCode,
            String personCode) {
        for (String table : RELATED_TABLES) {
            jdbcTemplate.update("DELETE FROM " + table + " WHERE dwbm = ? AND grbm = ?", organizationCode, personCode);
        }
        insertRelatedRowsForPerson(packageIndex, organizationCode, personCode, organizationCode, personCode, false);
    }

    private void updatePersonnelFromPackage(
            ExchangePackageIndex packageIndex,
            String organizationCode,
            String personCode) {
        Optional<Map<String, Object>> baseRow = packageIndex.dryjbxxRow(organizationCode, personCode);
        if (baseRow.isEmpty()) {
            return;
        }
        Map<String, String> columnTypes = tableColumnTypes(PERSONNEL_BASE_TABLE);
        List<String> setClauses = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (Map.Entry<String, String> column : columnTypes.entrySet()) {
            String name = column.getKey();
            if ("uid".equalsIgnoreCase(name)
                    || "id".equalsIgnoreCase(name)
                    || "dwbm".equalsIgnoreCase(name)
                    || "grbm".equalsIgnoreCase(name)) {
                continue;
            }
            Object value = valueIgnoreCase(baseRow.get(), name);
            if (value == null) {
                continue;
            }
            setClauses.add(quoteIdentifier(name) + " = ?");
            values.add(value);
        }
        if (setClauses.isEmpty()) {
            return;
        }
        values.add(organizationCode);
        values.add(personCode);
        jdbcTemplate.update(
                "UPDATE " + PERSONNEL_BASE_TABLE + " SET "
                        + String.join(", ", setClauses)
                        + " WHERE dwbm = ? AND grbm = ?",
                values.toArray());
    }

    private void markPayrollApproved(String organizationCode, String personCode) {
        jdbcTemplate.update("""
                UPDATE hisbase
                SET bbz = '已审'
                WHERE dwbm = ? AND grbm = ? AND (sid IS NULL OR TRIM(sid) = '')
                """, organizationCode, personCode);
    }

    private void insertPayrollRowsForPerson(
            List<DataExchangeService.ExchangeTable> payrollTables,
            String organizationCode,
            String personCode) {
        if (payrollTables == null || payrollTables.isEmpty()) {
            return;
        }
        for (DataExchangeService.ExchangeTable table : payrollTables) {
            if (!"hisbase".equalsIgnoreCase(table.tableName()) || table.rows() == null) {
                continue;
            }
            for (Map<String, Object> sourceRow : table.rows()) {
                if (!matchesPerson(sourceRow, organizationCode, personCode)) {
                    continue;
                }
                insertGenericRelatedRow("hisbase", sourceRow, organizationCode, personCode, false);
            }
        }
    }

    private void insertSubmissionRelatedRowsForPerson(
            List<DataExchangeService.ExchangeTable> relatedTables,
            String organizationCode,
            String personCode) {
        if (relatedTables == null || relatedTables.isEmpty()) {
            return;
        }
        for (String tableName : SUBMISSION_RELATED_TABLES) {
            jdbcTemplate.update("DELETE FROM " + tableName + " WHERE dwbm = ? AND grbm = ?", organizationCode, personCode);
        }
        insertRelatedRowsForSubmissionTables(relatedTables, organizationCode, personCode, organizationCode, personCode);
    }

    private void insertRelatedRowsForSubmissionTables(
            List<DataExchangeService.ExchangeTable> relatedTables,
            String sourceOrganizationCode,
            String sourcePersonCode,
            String targetOrganizationCode,
            String targetPersonCode) {
        if (relatedTables == null || relatedTables.isEmpty()) {
            return;
        }
        for (DataExchangeService.ExchangeTable table : relatedTables) {
            if (!SUBMISSION_RELATED_TABLES.contains(table.tableName()) || table.rows() == null) {
                continue;
            }
            for (Map<String, Object> sourceRow : table.rows()) {
                if (!matchesPerson(sourceRow, sourceOrganizationCode, sourcePersonCode)) {
                    continue;
                }
                insertGenericRelatedRow(table.tableName(), sourceRow, targetOrganizationCode, targetPersonCode, false);
            }
        }
    }

    private String buildPersonnelPackageQuery(List<String> conditions) {
        String whereClause = "WHERE " + String.join(" AND ", conditions);
        return """
                SELECT r.dwbm, d.dwmc, r.grbm, r.xm, r.sfzh, r.xb, r.csny,
                       r.ryfl, r.dwsx, r.gwfl, r.cjgzny, r.zzny, r.gznx,
                       r.xlbm, r.zgxl, r.zwjb, r.zjbm, r.xrzw,
                       (SELECT z.srny FROM dryzwbh z WHERE z.dwbm = r.dwbm AND z.grbm = r.grbm ORDER BY z.srny DESC, z.id DESC LIMIT 1) AS rzny,
                       r.mz, r.zzmm, r.dah,
                       h.zwbm2 AS gw, h.zwgw2 AS zw, h.jbgzjb2 AS jb, h.zwgzdc2 AS dc
                FROM dryjbxx r
                LEFT JOIN dwbm d ON r.dwbm = d.dwbm
                LEFT JOIN hisbase h ON h.id = (
                    SELECT h2.id
                    FROM hisbase h2
                    WHERE h2.dwbm = r.dwbm
                      AND h2.grbm = r.grbm
                      AND (h2.sid IS NULL OR TRIM(h2.sid) = '')
                    ORDER BY COALESCE(h2.jsnf, '') DESC, COALESCE(h2.jsyf, '') DESC, h2.id DESC
                    LIMIT 1
                )
                %s
                ORDER BY r.dwbm, r.grbm
                """.formatted(whereClause);
    }

    private String payrollStatusCondition(String organizationColumn, String personColumn, List<String> statuses) {
        String placeholders = statuses.stream().map(status -> "?").collect(Collectors.joining(", "));
        return """
                EXISTS (
                    SELECT 1 FROM hisbase ha
                    WHERE ha.dwbm = %s AND ha.grbm = %s
                      AND (ha.sid IS NULL OR TRIM(ha.sid) = '')
                      AND TRIM(ha.bbz) IN (%s)
                )
                """.formatted(organizationColumn, personColumn, placeholders);
    }

    Map<String, Object> findCurrentPayrollSummary(String organizationCode, String personCode) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT jslb, CONCAT(jsnf, jsyf) AS period, hj2, jzgb, bbz
                FROM hisbase
                WHERE dwbm = ? AND grbm = ? AND (sid IS NULL OR TRIM(sid) = '')
                LIMIT 1
                """, organizationCode, personCode);
        return rows.isEmpty() ? Map.of() : rows.getFirst();
    }

    PersonnelExportRecord findPersonnelExportRecord(String organizationCode, String personCode) {
        if (organizationCode == null || organizationCode.isBlank() || personCode == null || personCode.isBlank()) {
            return null;
        }
        String sql = buildPersonnelPackageQuery(List.of("r.dwbm = ?", "r.grbm = ?"));
        List<PersonnelExportRecord> rows = jdbcTemplate.query(
                sql + " LIMIT 1",
                this::mapPersonnelExport,
                organizationCode,
                personCode);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    List<Map<String, Object>> findRelatedRowsForPerson(String tableName, String organizationCode, String personCode) {
        if (!SUBMISSION_RELATED_TABLES.contains(tableName)
                || organizationCode == null || organizationCode.isBlank()
                || personCode == null || personCode.isBlank()) {
            return List.of();
        }
        return jdbcTemplate.queryForList(
                "SELECT * FROM " + tableName + " WHERE dwbm = ? AND grbm = ?",
                organizationCode,
                personCode);
    }
}
