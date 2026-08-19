package com.dxsoft.rsgzgl.license;

import com.dxsoft.rsgzgl.common.SqlText;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class LicenseRepository {

    private final NamedParameterJdbcTemplate jdbc;

    LicenseRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    void ensureTable() {
        jdbc.getJdbcTemplate().execute("""
                CREATE TABLE IF NOT EXISTS app_license (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    format VARCHAR(40) NOT NULL,
                    subject_code VARCHAR(20) NOT NULL,
                    subject_name VARCHAR(80) NOT NULL,
                    issued_at VARCHAR(40) NULL,
                    expires_at VARCHAR(40) NULL,
                    issuer VARCHAR(80) NULL,
                    fingerprint VARCHAR(64) NOT NULL,
                    organization_count INT NOT NULL DEFAULT 0,
                    payload_json MEDIUMTEXT NOT NULL,
                    imported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
    }

    LicenseStatusRow findLatest() {
        List<LicenseStatusRow> rows = jdbc.query("""
                SELECT format, subject_code, subject_name, issued_at, expires_at, issuer,
                       fingerprint, organization_count, payload_json
                FROM app_license
                ORDER BY id DESC
                LIMIT 1
                """, new MapSqlParameterSource(), (rs, rowNum) -> new LicenseStatusRow(
                SqlText.trim(rs.getString("format")),
                SqlText.trim(rs.getString("subject_code")),
                SqlText.trim(rs.getString("subject_name")),
                SqlText.trim(rs.getString("issued_at")),
                SqlText.trim(rs.getString("expires_at")),
                SqlText.trim(rs.getString("issuer")),
                SqlText.trim(rs.getString("fingerprint")),
                rs.getInt("organization_count"),
                rs.getString("payload_json")));
        return rows.isEmpty() ? null : rows.getFirst();
    }

    void saveImported(LicensePackageDocument doc, String fingerprint, String payloadJson) {
        jdbc.update("""
                INSERT INTO app_license (
                    format, subject_code, subject_name, issued_at, expires_at, issuer,
                    fingerprint, organization_count, payload_json
                ) VALUES (
                    :format, :subjectCode, :subjectName, :issuedAt, :expiresAt, :issuer,
                    :fingerprint, :organizationCount, :payloadJson
                )
                """, new MapSqlParameterSource()
                .addValue("format", doc.format())
                .addValue("subjectCode", doc.subject().organizationCode())
                .addValue("subjectName", doc.subject().organizationName())
                .addValue("issuedAt", doc.issuedAt())
                .addValue("expiresAt", doc.expiresAt())
                .addValue("issuer", doc.issuer())
                .addValue("fingerprint", fingerprint)
                .addValue("organizationCount", doc.organizations() == null ? 0 : doc.organizations().size())
                .addValue("payloadJson", payloadJson));
    }

    String findCyxxCity() {
        List<String> rows = jdbc.query("""
                SELECT szds
                FROM cyxx
                ORDER BY ID
                LIMIT 1
                """, new MapSqlParameterSource(), (rs, rowNum) -> SqlText.trim(rs.getString("szds")));
        return rows.isEmpty() || rows.getFirst() == null ? "" : rows.getFirst().trim();
    }

    String findCyxxSupervisor() {
        List<String> rows = jdbc.query("""
                SELECT zgry
                FROM cyxx
                ORDER BY ID
                LIMIT 1
                """, new MapSqlParameterSource(), (rs, rowNum) -> SqlText.trim(rs.getString("zgry")));
        return rows.isEmpty() || rows.getFirst() == null ? "" : rows.getFirst().trim();
    }

    long countCyxx() {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM cyxx", new MapSqlParameterSource(), Long.class);
        return count == null ? 0 : count;
    }

    void upsertCyxxSubject(LicenseSubject subject, String softsn, String licenseStamp) {
        if (countCyxx() == 0) {
            jdbc.update("""
                    INSERT INTO cyxx (ID, dwbm, dwmc, dwjc, zgry, szds, shrq, softsn, sp, bz)
                    VALUES (1, :code, :name, :level, :supervisor, :city, :stamp, :softsn, 1, :title)
                    """, params(subject, softsn, licenseStamp));
            return;
        }
        jdbc.update("""
                UPDATE cyxx
                SET dwbm = :code,
                    dwmc = :name,
                    dwjc = :level,
                    zgry = :supervisor,
                    szds = :city,
                    shrq = :stamp,
                    softsn = :softsn,
                    sp = 1,
                    bz = COALESCE(NULLIF(TRIM(bz), ''), :title)
                WHERE ID = (SELECT id FROM (SELECT MIN(ID) AS id FROM cyxx) t)
                """, params(subject, softsn, licenseStamp));
    }

    boolean organizationExists(String code) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM dwbm WHERE dwbm = :code",
                new MapSqlParameterSource("code", code),
                Integer.class);
        return count != null && count > 0;
    }

    void insertOrganization(LicenseOrganization org) {
        // dwbm 大量 NOT NULL 且无默认值的遗留字段，导入时补 0/空串
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
                    :code, :name, :shortName, :category, :property, :payrollCategory, :allowanceStandard,
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
                """, orgParams(org));
    }

    void updateOrganization(LicenseOrganization org) {
        jdbc.update("""
                UPDATE dwbm
                SET dwmc = :name,
                    dwmc1 = :shortName,
                    dwbz = :category,
                    dwsx = :property,
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
                WHERE dwbm = :code
                """, orgParams(org));
    }

    String findSubjectCodeFromCyxx() {
        List<String> rows = jdbc.query("""
                SELECT dwbm FROM cyxx ORDER BY ID LIMIT 1
                """, new MapSqlParameterSource(), (rs, rowNum) -> SqlText.trim(rs.getString("dwbm")));
        return rows.isEmpty() ? null : rows.getFirst();
    }

    /**
     * 按主体编码从本地 dwbm 加载授权单位；includeSubordinates 时包含编码前缀匹配的下属单位。
     */
    List<LicenseOrganization> findOrganizationsForIssue(String rootCode, boolean includeSubordinates) {
        String code = rootCode == null ? "" : rootCode.trim();
        if (code.isEmpty()) {
            return List.of();
        }
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("code", code)
                .addValue("includeSubordinates", includeSubordinates);
        return jdbc.query("""
                SELECT dwbm, dwmc, dwmc1, dwbz, dwsx, gzczbz, jtbz,
                       bzrs, zbrs, slrs, dwjc, dfbt, jxlb, njbt, jfly, kzfgjj, kylbxf
                FROM dwbm
                WHERE TRIM(dwbm) = :code
                   OR (:includeSubordinates = TRUE
                       AND TRIM(dwbm) LIKE CONCAT(:code, '%')
                       AND CHAR_LENGTH(TRIM(dwbm)) > CHAR_LENGTH(:code))
                ORDER BY TRIM(dwbm)
                """, params, LICENSE_ORG_MAPPER);
    }

    /** 签发「全部本地单位」：作为导入初始种子，不要求编码挂在签约主体下。 */
    List<LicenseOrganization> findAllOrganizationsForIssue() {
        return jdbc.query("""
                SELECT dwbm, dwmc, dwmc1, dwbz, dwsx, gzczbz, jtbz,
                       bzrs, zbrs, slrs, dwjc, dfbt, jxlb, njbt, jfly, kzfgjj, kylbxf
                FROM dwbm
                ORDER BY TRIM(dwbm)
                """, new MapSqlParameterSource(), LICENSE_ORG_MAPPER);
    }

    private static final org.springframework.jdbc.core.RowMapper<LicenseOrganization> LICENSE_ORG_MAPPER =
            (rs, rowNum) -> new LicenseOrganization(
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
                    SqlText.trim(rs.getString("dwjc")),
                    rs.getInt("dfbt"),
                    rs.getInt("jxlb"),
                    rs.getInt("njbt"),
                    SqlText.trim(rs.getString("jfly")),
                    SqlText.trim(rs.getString("kzfgjj")),
                    SqlText.trim(rs.getString("kylbxf")));

    private MapSqlParameterSource params(LicenseSubject subject, String softsn, String stamp) {
        String title = subject.organizationName() == null ? "" : subject.organizationName();
        if (title.length() > 12) {
            title = title.substring(0, 12);
        }
        return new MapSqlParameterSource()
                .addValue("code", trimTo(subject.organizationCode(), 9))
                .addValue("name", trimTo(subject.organizationName(), 30))
                .addValue("level", trimTo(subject.organizationLevel(), 1))
                .addValue("supervisor", trimTo(subject.supervisor(), 8))
                .addValue("city", trimTo(subject.city(), 20))
                .addValue("stamp", trimTo(stamp, 80))
                .addValue("softsn", trimTo(softsn, 25))
                .addValue("title", title);
    }

    private MapSqlParameterSource orgParams(LicenseOrganization org) {
        return new MapSqlParameterSource()
                .addValue("code", trimTo(org.organizationCode(), 9))
                .addValue("name", trimTo(org.name(), 40))
                .addValue("shortName", trimTo(org.shortName(), 30))
                .addValue("category", trimTo(org.category(), 4))
                .addValue("property", trimTo(org.property(), 2))
                .addValue("payrollCategory", trimTo(org.payrollCategory(), 10))
                .addValue("allowanceStandard", trimTo(org.allowanceStandard(), 5))
                .addValue("personnelQuota", org.personnelQuota() == null ? 0 : org.personnelQuota())
                .addValue("establishmentCount", org.establishmentCount() == null ? 0 : org.establishmentCount())
                .addValue("actualCount", org.actualCount() == null ? 0 : org.actualCount())
                .addValue("organizationLevel", trimTo(org.organizationLevel(), 1))
                .addValue("performanceAllowanceEnabled",
                        org.performanceAllowanceEnabled() == null ? 0 : org.performanceAllowanceEnabled())
                .addValue("performanceCategory",
                        org.performanceCategory() == null ? 0 : org.performanceCategory())
                .addValue("yearAllowanceCategory",
                        org.yearAllowanceCategory() == null ? 0 : org.yearAllowanceCategory())
                .addValue("financeSource", trimTo(org.financeSource(), 8))
                .addValue("housingFundWithheld", trimTo(org.housingFundWithheld(), 2))
                .addValue("pensionWithheld", trimTo(org.pensionWithheld(), 2));
    }

    private static String trimTo(String value, int max) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }

    record LicenseStatusRow(
            String format,
            String subjectCode,
            String subjectName,
            String issuedAt,
            String expiresAt,
            String issuer,
            String fingerprint,
            int organizationCount,
            String payloadJson
    ) {
    }
}
