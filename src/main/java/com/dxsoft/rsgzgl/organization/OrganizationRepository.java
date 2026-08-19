package com.dxsoft.rsgzgl.organization;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.SqlText;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class OrganizationRepository {

    private final NamedParameterJdbcTemplate jdbc;

    OrganizationRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    List<OrganizationSummary> findAll(String keyword, OrganizationScope organizationScope, PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes())
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());

        String where = " where (:allOrganizations = TRUE OR o.dwbm IN (:organizationCodes))";
        if (keyword != null && !keyword.isBlank()) {
            where += " and (o.dwbm like :keyword or o.dwmc like :keyword or o.dwmc1 like :keyword)";
            parameters.addValue("keyword", "%" + keyword.trim() + "%");
        }

        return jdbc.query(
                """
                select o.id, o.dwbm, o.dwmc, o.dwmc1, o.dwsx, o.dwbz, o.bzrs,
                       (select count(*) from dryjbxx p where p.dwbm = o.dwbm) as active_personnel_count
                from dwbm o
                """ + where + """

                order by o.dwbm
                limit :limit offset :offset
                """,
                parameters,
                (rs, rowNum) -> new OrganizationSummary(
                        rs.getInt("id"),
                        SqlText.trim(rs.getString("dwbm")),
                        SqlText.trim(rs.getString("dwmc")),
                        SqlText.trim(rs.getString("dwmc1")),
                        SqlText.trim(rs.getString("dwsx")),
                        SqlText.trim(rs.getString("dwbz")),
                        rs.getInt("bzrs"),
                        rs.getInt("active_personnel_count")));
    }

    List<OrganizationMaintenanceRecord> findMaintenanceRecords(
            String keyword,
            OrganizationScope organizationScope,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource parameters = scopedParameters(keyword, organizationScope)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());

        return jdbc.query(
                """
                select o.id, o.dwbm, o.dwmc, o.dwmc1, o.dwbz, o.dwsx, o.gzczbz, o.jtbz,
                       o.bzrs, o.zbrs, o.slrs, o.dwjc, o.dfbt, o.jxlb, o.njbt,
                       o.jfly, o.kzfgjj, o.kylbxf,
                       (select count(*) from dryjbxx p where p.dwbm = o.dwbm) as active_personnel_count
                from dwbm o
                """ + maintenanceWhere(keyword) + """

                order by o.dwbm
                limit :limit offset :offset
                """,
                parameters,
                (rs, rowNum) -> mapMaintenanceRecord(rs));
    }

    List<OrganizationTreeNode> findTree(OrganizationScope organizationScope, String keyword) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource parameters = scopedParameters(keyword, organizationScope);
        return jdbc.query("""
                select o.dwbm, o.dwmc, o.dwmc1
                from dwbm o
                """ + maintenanceWhere(keyword) + """

                order by o.dwbm
                """, parameters, (rs, rowNum) -> {
            String code = SqlText.trim(rs.getString("dwbm"));
            return new OrganizationTreeNode(
                    code,
                    SqlText.trim(rs.getString("dwmc")),
                    SqlText.trim(rs.getString("dwmc1")),
                    parentOrganizationCode(code));
        });
    }

    long count(String keyword, OrganizationScope organizationScope) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes());
        String where = " where (:allOrganizations = TRUE OR dwbm IN (:organizationCodes))";
        if (keyword != null && !keyword.isBlank()) {
            where += " and (dwbm like :keyword or dwmc like :keyword or dwmc1 like :keyword)";
            parameters.addValue("keyword", "%" + keyword.trim() + "%");
        }

        Long count = jdbc.queryForObject("select count(*) from dwbm" + where, parameters, Long.class);
        return count == null ? 0 : count;
    }

    long countMaintenanceRecords(String keyword, OrganizationScope organizationScope) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbc.queryForObject(
                "select count(*) from dwbm o" + maintenanceWhere(keyword),
                scopedParameters(keyword, organizationScope),
                Long.class);
        return count == null ? 0 : count;
    }

    private MapSqlParameterSource scopedParameters(String keyword, OrganizationScope organizationScope) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes());
        if (keyword != null && !keyword.isBlank()) {
            parameters.addValue("keyword", "%" + keyword.trim() + "%");
        }
        return parameters;
    }

    private String maintenanceWhere(String keyword) {
        String where = " where (:allOrganizations = TRUE OR o.dwbm IN (:organizationCodes))";
        if (keyword != null && !keyword.isBlank()) {
            where += " and (o.dwbm like :keyword or o.dwmc like :keyword or o.dwmc1 like :keyword or o.gzczbz like :keyword)";
        }
        return where;
    }

    private String parentOrganizationCode(String code) {
        if (code == null || code.length() <= 3) {
            return null;
        }
        for (int length : List.of(7, 5, 3)) {
            if (code.length() > length) {
                return code.substring(0, length);
            }
        }
        return null;
    }

    OrganizationMaintenanceRecord findMaintenanceRecordById(int id) {
        List<OrganizationMaintenanceRecord> rows = jdbc.query("""
                select o.id, o.dwbm, o.dwmc, o.dwmc1, o.dwbz, o.dwsx, o.gzczbz, o.jtbz,
                       o.bzrs, o.zbrs, o.slrs, o.dwjc, o.dfbt, o.jxlb, o.njbt,
                       o.jfly, o.kzfgjj, o.kylbxf,
                       (select count(*) from dryjbxx p where p.dwbm = o.dwbm) as active_personnel_count
                from dwbm o
                where o.id = :id
                limit 1
                """, new MapSqlParameterSource("id", id), (rs, rowNum) -> mapMaintenanceRecord(rs));
        return rows.isEmpty() ? null : rows.getFirst();
    }

    OrganizationMaintenanceRecord findMaintenanceRecordByCode(String organizationCode) {
        List<OrganizationMaintenanceRecord> rows = jdbc.query("""
                select o.id, o.dwbm, o.dwmc, o.dwmc1, o.dwbz, o.dwsx, o.gzczbz, o.jtbz,
                       o.bzrs, o.zbrs, o.slrs, o.dwjc, o.dfbt, o.jxlb, o.njbt,
                       o.jfly, o.kzfgjj, o.kylbxf,
                       (select count(*) from dryjbxx p where p.dwbm = o.dwbm) as active_personnel_count
                from dwbm o
                where o.dwbm = :organizationCode
                limit 1
                """, new MapSqlParameterSource("organizationCode", organizationCode),
                (rs, rowNum) -> mapMaintenanceRecord(rs));
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private OrganizationMaintenanceRecord mapMaintenanceRecord(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new OrganizationMaintenanceRecord(
                rs.getInt("id"),
                SqlText.trim(rs.getString("dwbm")),
                SqlText.trim(rs.getString("dwmc")),
                SqlText.trim(rs.getString("dwmc1")),
                SqlText.trim(rs.getString("dwsx")),
                SqlText.trim(rs.getString("dwbz")),
                SqlText.trim(rs.getString("gzczbz")),
                SqlText.trim(rs.getString("jtbz")),
                rs.getInt("bzrs"),
                rs.getInt("zbrs"),
                rs.getInt("slrs"),
                rs.getInt("active_personnel_count"),
                SqlText.trim(rs.getString("dwjc")),
                rs.getInt("dfbt"),
                rs.getInt("jxlb"),
                rs.getInt("njbt"),
                SqlText.trim(rs.getString("jfly")),
                SqlText.trim(rs.getString("kzfgjj")),
                SqlText.trim(rs.getString("kylbxf")));
    }

    void updateMaintenanceRecord(int id, OrganizationMaintenanceRequest request) {
        jdbc.update("""
                update dwbm
                set dwmc = :name,
                    dwmc1 = :shortName,
                    dwsx = :property,
                    dwbz = :category,
                    gzczbz = :payrollCategory,
                    jtbz = :allowanceStandard,
                    bzrs = :personnelQuota,
                    zbrs = :establishmentCount,
                    slrs = :actualCount,
                    dwjc = :organizationLevel,
                    dfbt = :performanceAllowanceEnabled,
                    jxlb = :performanceCategory,
                    njbt = :yearAllowanceCategory,
                    jfly = :financeSource,
                    kzfgjj = :housingFundWithheld,
                    kylbxf = :pensionWithheld
                where id = :id
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("name", request.name())
                .addValue("shortName", request.shortName())
                .addValue("property", request.property())
                .addValue("category", request.category())
                .addValue("payrollCategory", request.payrollCategory())
                .addValue("allowanceStandard", request.allowanceStandard())
                .addValue("personnelQuota", request.personnelQuota() == null ? 0 : request.personnelQuota())
                .addValue("establishmentCount", request.establishmentCount() == null ? 0 : request.establishmentCount())
                .addValue("actualCount", request.actualCount() == null ? 0 : request.actualCount())
                .addValue("organizationLevel", request.organizationLevel())
                .addValue("performanceAllowanceEnabled", request.performanceAllowanceEnabled() == null ? 0 : request.performanceAllowanceEnabled())
                .addValue("performanceCategory", request.performanceCategory() == null ? 0 : request.performanceCategory())
                .addValue("yearAllowanceCategory", request.yearAllowanceCategory() == null ? 0 : request.yearAllowanceCategory())
                .addValue("financeSource", request.financeSource())
                .addValue("housingFundWithheld", request.housingFundWithheld())
                .addValue("pensionWithheld", request.pensionWithheld()));
    }

    int insertOrganization(OrganizationCreateRequest request) {
        jdbc.update("""
                INSERT INTO dwbm (
                    dwbm, dwmc, dwmc1, dwbz, dwsx, gzczbz, jtbz,
                    bzrs, zbrs, slrs, dwjc,
                    gjzcrsxd, zjzcrsxd, cjzcrsxd, bz, yhzh,
                    csbz, tf, tfyf, kmbm, czbz, kzfgjj, kylbxf,
                    ltrs, jkjs, jfly, dfbt, jb, xtlb, jglb, zgbm,
                    tby, tbm, tbd, frzsh, jxlb, njbt, dwcc, sshy, sfqyhgl, jxbl,
                    a0760, a0770, a0780, a0790, a07a0, a07b0, a07c0,
                    a1002, a1003, a1004, a1005, a1006, a1007, a1008, a1009, a1010, a1011, a1012, a1013,
                    a0801, a0802, a0803, a0804, a0805,
                    nzj2010, nzj2011, nzj2012, nzj2013, gqbz
                ) VALUES (
                    :organizationCode, :name, :shortName, :category, :property, :payrollCategory, :allowanceStandard,
                    :personnelQuota, :establishmentCount, :actualCount, :organizationLevel,
                    0, 0, 0, '', '',
                    '', '', '', '', '', :housingFundWithheld, :pensionWithheld,
                    0, 0, :financeSource, :performanceAllowanceEnabled, '', '', '', '',
                    '', '', '', '', :performanceCategory, :yearAllowanceCategory, 0, '', 0, '',
                    0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0
                )
                """, new MapSqlParameterSource()
                .addValue("organizationCode", request.organizationCode())
                .addValue("name", nullToEmpty(request.name()))
                .addValue("shortName", nullToEmpty(request.shortName()))
                .addValue("category", nullToEmpty(request.category()))
                .addValue("property", nullToEmpty(request.property()))
                .addValue("payrollCategory", nullToEmpty(request.payrollCategory()))
                .addValue("allowanceStandard", nullToEmpty(request.allowanceStandard()))
                .addValue("personnelQuota", nullToZero(request.personnelQuota()))
                .addValue("establishmentCount", nullToZero(request.establishmentCount()))
                .addValue("actualCount", nullToZero(request.actualCount()))
                .addValue("organizationLevel", nullToEmpty(request.organizationLevel()))
                .addValue("performanceAllowanceEnabled", nullToZero(request.performanceAllowanceEnabled()))
                .addValue("performanceCategory", nullToZero(request.performanceCategory()))
                .addValue("yearAllowanceCategory", nullToZero(request.yearAllowanceCategory()))
                .addValue("financeSource", nullToEmpty(request.financeSource()))
                .addValue("housingFundWithheld", nullToEmpty(request.housingFundWithheld()))
                .addValue("pensionWithheld", nullToEmpty(request.pensionWithheld())));
        Integer id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Integer.class);
        return id == null ? 0 : id;
    }

    boolean existsByOrganizationCode(String organizationCode) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM dwbm WHERE dwbm = :organizationCode",
                new MapSqlParameterSource("organizationCode", organizationCode),
                Integer.class);
        return count != null && count > 0;
    }

    boolean hasChildOrganizations(String organizationCode) {
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM dwbm
                WHERE dwbm LIKE :prefix
                  AND CHAR_LENGTH(TRIM(dwbm)) > :codeLength
                """, new MapSqlParameterSource()
                .addValue("prefix", organizationCode + "%")
                .addValue("codeLength", organizationCode.length()), Integer.class);
        return count != null && count > 0;
    }

    boolean hasPersonnel(String organizationCode) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM dryjbxx WHERE dwbm = :organizationCode",
                new MapSqlParameterSource("organizationCode", organizationCode),
                Integer.class);
        return count != null && count > 0;
    }

    void deleteById(int id) {
        jdbc.update("DELETE FROM dwbm WHERE id = :id", new MapSqlParameterSource("id", id));
    }

    String suggestNextRootCode() {
        String maxCode = jdbc.query("""
                SELECT MAX(TRIM(dwbm)) AS maxbm
                FROM dwbm
                WHERE CHAR_LENGTH(TRIM(dwbm)) = 3
                """, new MapSqlParameterSource(), rs -> rs.next() ? SqlText.trim(rs.getString("maxbm")) : null);
        if (maxCode == null || maxCode.isBlank()) {
            return "001";
        }
        int next = Integer.parseInt(maxCode) + 1;
        return String.format("%03d", next);
    }

    String suggestNextChildCode(String parentCode) {
        int childLength = parentCode.length() + 2;
        String maxCode = jdbc.query("""
                SELECT MAX(TRIM(dwbm)) AS maxbm
                FROM dwbm
                WHERE LEFT(TRIM(dwbm), :parentLength) = :parentCode
                  AND CHAR_LENGTH(TRIM(dwbm)) = :childLength
                """, new MapSqlParameterSource()
                .addValue("parentLength", parentCode.length())
                .addValue("parentCode", parentCode)
                .addValue("childLength", childLength), rs -> rs.next() ? SqlText.trim(rs.getString("maxbm")) : null);
        if (maxCode == null || maxCode.isBlank()) {
            return parentCode + "01";
        }
        String suffix = maxCode.substring(parentCode.length());
        int next = Integer.parseInt(suffix) + 1;
        return parentCode + String.format("%0" + (childLength - parentCode.length()) + "d", next);
    }

    List<OrganizationFieldOption> findPropertyOptions() {
        return jdbc.query("""
                SELECT TRIM(bm) AS bm, TRIM(mc) AS mc
                FROM dmb
                WHERE bm LIKE '008%'
                  AND CHAR_LENGTH(TRIM(bm)) > 3
                  AND COALESCE(sfsy, 1) = 1
                ORDER BY bm
                """, new MapSqlParameterSource(), (rs, rowNum) -> {
            String code = SqlText.trim(rs.getString("bm"));
            String value = code != null && code.startsWith("008") ? code.substring(3) : code;
            String name = SqlText.trim(rs.getString("mc"));
            String label = (value == null || value.isBlank())
                    ? name
                    : (name == null || name.isBlank() ? value : value + " " + name);
            return new OrganizationFieldOption(value, label);
        });
    }

    List<String> findDistinctValues(String column) {
        String safeColumn = switch (column) {
            case "dwsx", "dwbz", "dwjc", "gzczbz", "jtbz", "jfly", "kzfgjj", "kylbxf" -> column;
            default -> throw new IllegalArgumentException("Unsupported column: " + column);
        };
        return jdbc.query("""
                SELECT DISTINCT TRIM(%s) AS value
                FROM dwbm
                WHERE %s IS NOT NULL AND TRIM(%s) <> ''
                ORDER BY value
                """.formatted(safeColumn, safeColumn, safeColumn),
                new MapSqlParameterSource(),
                (rs, rowNum) -> SqlText.trim(rs.getString("value")));
    }

    List<Integer> findDistinctIntegers(String column) {
        String safeColumn = switch (column) {
            case "dfbt", "jxlb", "njbt" -> column;
            default -> throw new IllegalArgumentException("Unsupported column: " + column);
        };
        return jdbc.query("""
                SELECT DISTINCT %s AS value
                FROM dwbm
                WHERE %s IS NOT NULL
                ORDER BY value
                """.formatted(safeColumn, safeColumn),
                new MapSqlParameterSource(),
                (rs, rowNum) -> rs.getInt("value"));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }
}
