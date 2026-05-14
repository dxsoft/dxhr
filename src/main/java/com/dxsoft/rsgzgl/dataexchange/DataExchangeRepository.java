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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class DataExchangeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AccessControlService accessControlService;

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
                SELECT r.dwbm, d.dwmc, r.grbm, r.xm, r.sfzh, r.xb, r.csn_yf,
                       r.rylb, r.dwsx, r.gwfl, r.cjgz_yf, r.zz_yf, r.gznx,
                       r.xl, r.zgxl, r.dqzwjb, r.zjbm, r.dqzw, r.rzny,
                       r.mz, r.zzmm, r.dah,
                       h.gw, h.zw, h.jb, h.dc
                FROM dryjbxx r
                LEFT JOIN dwbm d ON r.dwbm = d.dwbm
                LEFT JOIN hisbase h ON r.uid = h.uid AND h.dq = '是'
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
                rs.getString("csn_yf"),
                rs.getString("rylb"),
                rs.getString("dwsx"),
                rs.getString("gwfl"),
                rs.getString("cjgz_yf"),
                rs.getString("zz_yf"),
                rs.getObject("gznx", Integer.class),
                rs.getString("xl"),
                rs.getString("zgxl"),
                rs.getString("dqzwjb"),
                rs.getString("zjbm"),
                rs.getString("dqzw"),
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
            conditions.add("h.ny = ?");
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
                LEFT JOIN dryjbxx r ON h.uid = r.uid
                LEFT JOIN dwbm d ON h.dwbm = d.dwbm
                %s
                """.formatted(whereClause);

        long total = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

        String querySql = """
                SELECT h.dwbm, d.dwmc, h.grbm, h.xm, r.sfzh, r.xb, r.csn_yf, r.rylb,
                       h.gw, h.zw, h.jb, h.dc, h.ny, h.bdlb,
                       h.zwgz, h.jbgz, h.jsdjgz, h.jxgz, h.blfb, h.jxjt, h.njbt,
                       h.jhljt, h.tggz, h.fdgz, h.jjjy, h.pgbc, h.hj
                FROM hisbase h
                LEFT JOIN dryjbxx r ON h.uid = r.uid
                LEFT JOIN dwbm d ON h.dwbm = d.dwbm
                %s
                ORDER BY h.dwbm, h.grbm, h.ny
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
                rs.getString("csn_yf"),
                rs.getString("rylb"),
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
                SELECT r.dwbm, d.dwmc, r.grbm, r.xm, r.sfzh, r.xb, r.csn_yf,
                       r.rylb, r.dwsx, r.gwfl, r.cjgz_yf, r.zz_yf, r.gznx,
                       r.xl, r.zgxl, r.dqzwjb, r.zjbm, r.dqzw, r.rzny,
                       r.mz, r.zzmm, r.dah,
                       h.gw, h.zw, h.jb, h.dc
                FROM dryjbxx r
                LEFT JOIN dwbm d ON r.dwbm = d.dwbm
                LEFT JOIN hisbase h ON r.uid = h.uid AND h.dq = '是'
                %s
                ORDER BY r.dwbm, r.grbm
                """.formatted(whereClause);

        return jdbcTemplate.query(querySql, this::mapPersonnelExport, params.toArray());
    }

    List<PersonnelExportRecord> exportPersonnelPackageByOrganizations(List<String> organizationCodes, boolean includeDescendants) {
        List<String> resolvedCodes = resolveOrganizationCodes(organizationCodes, includeDescendants);
        if (resolvedCodes.isEmpty()) {
            return List.of();
        }
        String placeholders = resolvedCodes.stream().map(code -> "?").collect(Collectors.joining(", "));
        String querySql = """
                SELECT r.dwbm, d.dwmc, r.grbm, r.xm, r.sfzh, r.xb, r.csn_yf,
                       r.rylb, r.dwsx, r.gwfl, r.cjgz_yf, r.zz_yf, r.gznx,
                       r.xl, r.zgxl, r.dqzwjb, r.zjbm, r.dqzw, r.rzny,
                       r.mz, r.zzmm, r.dah,
                       h.gw, h.zw, h.jb, h.dc
                FROM dryjbxx r
                LEFT JOIN dwbm d ON r.dwbm = d.dwbm
                LEFT JOIN hisbase h ON r.uid = h.uid AND h.dq = '是'
                WHERE r.dwbm IN (%s)
                ORDER BY r.dwbm, r.grbm
                """.formatted(placeholders);
        return jdbcTemplate.query(querySql, this::mapPersonnelExport, resolvedCodes.toArray());
    }

    List<PersonnelExportRecord> exportSelectedPersonnel(List<DataExchangeController.PersonKey> selectedPersonnel) {
        if (selectedPersonnel == null || selectedPersonnel.isEmpty()) {
            return List.of();
        }
        List<PersonnelExportRecord> rows = new ArrayList<>();
        for (DataExchangeController.PersonKey key : selectedPersonnel) {
            rows.addAll(jdbcTemplate.query("""
                    SELECT r.dwbm, d.dwmc, r.grbm, r.xm, r.sfzh, r.xb, r.csn_yf,
                           r.rylb, r.dwsx, r.gwfl, r.cjgz_yf, r.zz_yf, r.gznx,
                           r.xl, r.zgxl, r.dqzwjb, r.zjbm, r.dqzw, r.rzny,
                           r.mz, r.zzmm, r.dah,
                           h.gw, h.zw, h.jb, h.dc
                    FROM dryjbxx r
                    LEFT JOIN dwbm d ON r.dwbm = d.dwbm
                    LEFT JOIN hisbase h ON r.uid = h.uid AND h.dq = '是'
                    WHERE r.dwbm = ? AND r.grbm = ?
                    """, this::mapPersonnelExport, key.organizationCode(), key.personCode()));
        }
        return rows;
    }

    int replaceReceivedPersonnel(List<PersonnelExportRecord> rows) {
        int count = 0;
        for (PersonnelExportRecord row : rows) {
            jdbcTemplate.update("DELETE FROM dryjbxx WHERE dwbm = ? AND grbm = ?", row.organizationCode(), row.personCode());
            insertPersonnel(row, row.organizationCode(), row.personCode());
            count++;
        }
        return count;
    }

    int appendReceivedPersonnel(List<PersonnelExportRecord> rows, String targetOrganizationCode) {
        int count = 0;
        int nextCode = nextPersonCode(targetOrganizationCode);
        for (PersonnelExportRecord row : rows) {
            String personCode = "%05d".formatted(nextCode++);
            insertPersonnel(row, targetOrganizationCode, personCode);
            count++;
        }
        return count;
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

    private void insertPersonnel(PersonnelExportRecord row, String organizationCode, String personCode) {
        jdbcTemplate.update("""
                INSERT INTO dryjbxx (
                    dwbm, grbm, xm, sfzh, xb, csn_yf, rylb, dwsx, gwfl,
                    cjgz_yf, zz_yf, gznx, xl, zgxl, dqzwjb, zjbm, dqzw,
                    rzny, mz, zzmm, dah
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                organizationCode,
                personCode,
                row.name(),
                row.idCard(),
                row.gender(),
                row.birthYearMonth(),
                row.personnelCategory(),
                row.organizationType(),
                row.postCategory(),
                row.workStart(),
                row.regularization(),
                row.salaryYears(),
                row.educationCode(),
                row.highestEducation(),
                row.positionLevel(),
                row.rankCode(),
                row.currentPosition(),
                row.positionStart(),
                row.ethnicity(),
                row.politicalStatus(),
                row.archiveNumber());
    }
}
