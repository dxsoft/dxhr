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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class DataExchangeRepository {

    private static final List<String> RELATED_TABLES = List.of(
            "hisbase",
            "dryzwbh",
            "dxl",
            "dndkh",
            "jx",
            "dtgxx",
            "tgqgz2006",
            "hjxx");

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
                SELECT r.dwbm, d.dwmc, r.grbm, r.xm, r.sfzh, r.xb, r.csny,
                       r.ryfl, r.dwsx, r.gwfl, r.cjgzny, r.zzny, r.gznx,
                       r.xlbm, r.zgxl, r.zwjb, r.zjbm, r.xrzw,
                       (SELECT z.srny FROM dryzwbh z WHERE z.dwbm = r.dwbm AND z.grbm = r.grbm ORDER BY z.srny DESC, z.id DESC LIMIT 1) AS rzny,
                       r.mz, r.zzmm, r.dah,
                       h.zwbm2 AS gw, h.zwgw2 AS zw, h.jbgzjb2 AS jb, h.zwgzdc2 AS dc
                FROM dryjbxx r
                LEFT JOIN dwbm d ON r.dwbm = d.dwbm
                LEFT JOIN hisbase h ON h.dwbm = r.dwbm AND h.grbm = r.grbm AND (h.sid IS NULL OR TRIM(h.sid) = '')
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
                LEFT JOIN hisbase h ON h.dwbm = r.dwbm AND h.grbm = r.grbm AND (h.sid IS NULL OR TRIM(h.sid) = '')
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
                SELECT r.dwbm, d.dwmc, r.grbm, r.xm, r.sfzh, r.xb, r.csny,
                       r.ryfl, r.dwsx, r.gwfl, r.cjgzny, r.zzny, r.gznx,
                       r.xlbm, r.zgxl, r.zwjb, r.zjbm, r.xrzw,
                       (SELECT z.srny FROM dryzwbh z WHERE z.dwbm = r.dwbm AND z.grbm = r.grbm ORDER BY z.srny DESC, z.id DESC LIMIT 1) AS rzny,
                       r.mz, r.zzmm, r.dah,
                       h.zwbm2 AS gw, h.zwgw2 AS zw, h.jbgzjb2 AS jb, h.zwgzdc2 AS dc
                FROM dryjbxx r
                LEFT JOIN dwbm d ON r.dwbm = d.dwbm
                LEFT JOIN hisbase h ON h.dwbm = r.dwbm AND h.grbm = r.grbm AND (h.sid IS NULL OR TRIM(h.sid) = '')
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
                    SELECT r.dwbm, d.dwmc, r.grbm, r.xm, r.sfzh, r.xb, r.csny,
                           r.ryfl, r.dwsx, r.gwfl, r.cjgzny, r.zzny, r.gznx,
                           r.xlbm, r.zgxl, r.zwjb, r.zjbm, r.xrzw,
                           (SELECT z.srny FROM dryzwbh z WHERE z.dwbm = r.dwbm AND z.grbm = r.grbm ORDER BY z.srny DESC, z.id DESC LIMIT 1) AS rzny,
                           r.mz, r.zzmm, r.dah,
                           h.zwbm2 AS gw, h.zwgw2 AS zw, h.jbgzjb2 AS jb, h.zwgzdc2 AS dc
                    FROM dryjbxx r
                    LEFT JOIN dwbm d ON r.dwbm = d.dwbm
                    LEFT JOIN hisbase h ON h.dwbm = r.dwbm AND h.grbm = r.grbm AND (h.sid IS NULL OR TRIM(h.sid) = '')
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
        for (String table : RELATED_TABLES) {
            List<Map<String, Object>> rows = new ArrayList<>();
            for (PersonnelExportRecord person : personnelRows) {
                rows.addAll(jdbcTemplate.queryForList(
                        "SELECT * FROM " + table + " WHERE dwbm = ? AND grbm = ?",
                        person.organizationCode(),
                        person.personCode()));
            }
            tables.add(new DataExchangeService.ExchangeTable(table, rows));
        }
        return tables;
    }

    int replaceReceivedPersonnel(List<PersonnelExportRecord> rows, List<DataExchangeService.ExchangeTable> relatedTables) {
        int count = 0;
        for (PersonnelExportRecord row : rows) {
            deletePersonRelatedRows(row.organizationCode(), row.personCode());
            jdbcTemplate.update("DELETE FROM dryjbxx WHERE dwbm = ? AND grbm = ?", row.organizationCode(), row.personCode());
            insertPersonnel(row, row.organizationCode(), row.personCode());
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
            insertPersonnel(row, targetOrganizationCode, personCode);
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
            List<DataExchangeService.ExchangeTable> relatedTables,
            String sourceOrganizationCode,
            String sourcePersonCode,
            String targetOrganizationCode,
            String targetPersonCode,
            boolean appendMode) {
        if (relatedTables == null || relatedTables.isEmpty()) {
            return;
        }
        for (DataExchangeService.ExchangeTable table : relatedTables) {
            if (!RELATED_TABLES.contains(table.tableName()) || table.rows() == null) {
                continue;
            }
            for (Map<String, Object> sourceRow : table.rows()) {
                if (!matchesPerson(sourceRow, sourceOrganizationCode, sourcePersonCode)) {
                    continue;
                }
                insertGenericRelatedRow(table.tableName(), sourceRow, targetOrganizationCode, targetPersonCode, appendMode);
            }
        }
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
                value = findPersonUid(targetOrganizationCode, targetPersonCode);
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
}
