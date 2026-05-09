package com.dxsoft.rsgzgl.personnel;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.SensitiveData;
import com.dxsoft.rsgzgl.common.SqlText;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class PersonnelRepository {

    private static final RowMapper<PersonnelSummary> SUMMARY_MAPPER = (rs, rowNum) -> new PersonnelSummary(
            rs.getInt("uid"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SensitiveData.maskIdCard(rs.getString("sfzh")),
            SqlText.trim(rs.getString("xb")),
            SqlText.trim(rs.getString("csny")),
            SqlText.trim(rs.getString("ryfl")),
            SqlText.trim(rs.getString("dwsx")),
            SqlText.trim(rs.getString("gwfl")),
            SqlText.trim(rs.getString("xrzw")),
            SqlText.trim(rs.getString("zjbm"))
    );

    private static final RowMapper<PersonnelDetail> DETAIL_MAPPER = (rs, rowNum) -> new PersonnelDetail(
            rs.getInt("uid"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SensitiveData.maskIdCard(rs.getString("sfzh")),
            SqlText.trim(rs.getString("xb")),
            SqlText.trim(rs.getString("csny")),
            SqlText.trim(rs.getString("ryfl")),
            SqlText.trim(rs.getString("dwsx")),
            SqlText.trim(rs.getString("gwfl")),
            SqlText.trim(rs.getString("cjgzny")),
            SqlText.trim(rs.getString("zzny")),
            SqlText.trim(rs.getString("zwjb")),
            SqlText.trim(rs.getString("zjbm")),
            SqlText.trim(rs.getString("xrzw")),
            SqlText.trim(rs.getString("srny")),
            rs.getInt("gznx"),
            SqlText.trim(rs.getString("zgxl")),
            SqlText.trim(rs.getString("txsj"))
    );

    private static final RowMapper<PersonnelMaintenanceRecord> MAINTENANCE_MAPPER = (rs, rowNum) -> new PersonnelMaintenanceRecord(
            rs.getInt("uid"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("sfzh")),
            SqlText.trim(rs.getString("xb")),
            SqlText.trim(rs.getString("csny")),
            SqlText.trim(rs.getString("ryfl")),
            SqlText.trim(rs.getString("dwsx")),
            SqlText.trim(rs.getString("gwfl")),
            SqlText.trim(rs.getString("cjgzny")),
            SqlText.trim(rs.getString("zzny")),
            rs.getInt("gznx"),
            SqlText.trim(rs.getString("xlbm")),
            SqlText.trim(rs.getString("zgxl")),
            SqlText.trim(rs.getString("zwjb")),
            SqlText.trim(rs.getString("zjbm")),
            SqlText.trim(rs.getString("xrzw")),
            SqlText.trim(rs.getString("srny")),
            SqlText.trim(rs.getString("mz")),
            SqlText.trim(rs.getString("zzmm")),
            SqlText.trim(rs.getString("dah")));

    private static final RowMapper<PositionRecord> POSITION_MAPPER = (rs, rowNum) -> new PositionRecord(
            rs.getInt("id"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xrzwbm")),
            SqlText.trim(rs.getString("xrzw")),
            SqlText.trim(rs.getString("zwjb")),
            SqlText.trim(rs.getString("zjbm")),
            SqlText.trim(rs.getString("zwbm")),
            SqlText.trim(rs.getString("xzzw")),
            SqlText.trim(rs.getString("zwlb")),
            SqlText.trim(rs.getString("srny")),
            rs.getInt("kjnx"),
            SqlText.trim(rs.getString("xrzwbz")),
            SqlText.trim(rs.getString("jsbz"))
    );

    private static final RowMapper<PersonnelPositionHistoryRecord> POSITION_HISTORY_MAPPER = (rs, rowNum) -> new PersonnelPositionHistoryRecord(
            rs.getInt("id"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("xrzwbm")),
            SqlText.trim(rs.getString("xrzw")),
            SqlText.trim(rs.getString("zwjb")),
            SqlText.trim(rs.getString("zjbm")),
            SqlText.trim(rs.getString("zwbm")),
            SqlText.trim(rs.getString("xzzw")),
            SqlText.trim(rs.getString("zwlb")),
            SqlText.trim(rs.getString("srny")),
            rs.getInt("kjnx"),
            SqlText.trim(rs.getString("xrzwbz")),
            SqlText.trim(rs.getString("jsbz"))
    );

    private static final RowMapper<EducationRecord> EDUCATION_MAPPER = (rs, rowNum) -> new EducationRecord(
            rs.getInt("id"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xlbm")),
            SqlText.trim(rs.getString("xl")),
            SqlText.trim(rs.getString("byyx")),
            SqlText.trim(rs.getString("rxsj")),
            SqlText.trim(rs.getString("bysj")),
            rs.getInt("xz"),
            SqlText.trim(rs.getString("xllb")),
            SqlText.trim(rs.getString("bz"))
    );

    private static final RowMapper<PersonnelEducationHistoryRecord> EDUCATION_HISTORY_MAPPER = (rs, rowNum) -> new PersonnelEducationHistoryRecord(
            rs.getInt("id"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("xlbm")),
            SqlText.trim(rs.getString("xl")),
            SqlText.trim(rs.getString("byyx")),
            SqlText.trim(rs.getString("rxsj")),
            SqlText.trim(rs.getString("bysj")),
            rs.getInt("xz"),
            SqlText.trim(rs.getString("xllb")),
            SqlText.trim(rs.getString("bz"))
    );

    private static final RowMapper<AssessmentRecord> ASSESSMENT_MAPPER = (rs, rowNum) -> new AssessmentRecord(
            rs.getInt("id"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("khnd")),
            SqlText.trim(rs.getString("khjg"))
    );

    private static final RowMapper<AnnualAssessmentRecord> ANNUAL_ASSESSMENT_MAPPER = (rs, rowNum) -> new AnnualAssessmentRecord(
            rs.getInt("id"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("khnd")),
            SqlText.trim(rs.getString("khjg"))
    );

    private static final RowMapper<AnnualAssessmentSummaryRecord> ANNUAL_ASSESSMENT_SUMMARY_MAPPER = (rs, rowNum) -> new AnnualAssessmentSummaryRecord(
            SqlText.trim(rs.getString("khnd")),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("khjg")),
            rs.getLong("personnel_count")
    );

    private static final RowMapper<ChangedPersonnelRecord> CHANGED_PERSONNEL_MAPPER = (rs, rowNum) -> new ChangedPersonnelRecord(
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SensitiveData.maskIdCard(rs.getString("sfzh")),
            SqlText.trim(rs.getString("xb")),
            SqlText.trim(rs.getString("csny")),
            SqlText.trim(rs.getString("ryfl")),
            SqlText.trim(rs.getString("dwsx")),
            SqlText.trim(rs.getString("gwfl")),
            SqlText.trim(rs.getString("jsnf")),
            SqlText.trim(rs.getString("jsyf")),
            SqlText.trim(rs.getString("jslb")),
            null,
            null,
            null,
            SqlText.trim(rs.getString("zwbm2")),
            SqlText.trim(rs.getString("zwgw2")),
            rs.getBigDecimal("hj2"),
            SqlText.trim(rs.getString("tbnd")),
            SqlText.trim(rs.getString("jbtbz")),
            SqlText.trim(rs.getString("bz"))
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    PersonnelRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    List<PersonnelSummary> findAll(OrganizationScope organizationScope, String keyword, PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource params = parameters(organizationScope, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT p.uid, p.dwbm, dw.dwmc, p.grbm, p.xm, p.sfzh, p.xb, p.csny,
                       p.ryfl, p.dwsx, p.gwfl, p.xrzw, p.zjbm
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:keyword IS NULL OR p.xm LIKE :keywordLike OR p.grbm LIKE :keywordLike OR p.sfzh LIKE :keywordLike)
                ORDER BY p.dwbm, p.grbm
                LIMIT :limit OFFSET :offset
                """, params, SUMMARY_MAPPER);
    }

    long countAll(OrganizationScope organizationScope, String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dryjbxx p
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (:keyword IS NULL OR p.xm LIKE :keywordLike OR p.grbm LIKE :keywordLike OR p.sfzh LIKE :keywordLike)
                """, parameters(organizationScope, keyword), Long.class);
        return count == null ? 0 : count;
    }

    Optional<PersonnelDetail> findByUid(int uid) {
        return jdbcTemplate.query("""
                SELECT p.*, dw.dwmc
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE p.uid = :uid
                """, new MapSqlParameterSource("uid", uid), DETAIL_MAPPER).stream().findFirst();
    }

    Optional<PersonnelMaintenanceRecord> findMaintenanceByUid(int uid) {
        return jdbcTemplate.query("""
                SELECT p.*, dw.dwmc
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                WHERE p.uid = :uid
                """, new MapSqlParameterSource("uid", uid), MAINTENANCE_MAPPER).stream().findFirst();
    }

    int createPersonnel(PersonnelMaintenanceRequest request) {
        MapSqlParameterSource params = maintenanceParameters(request);
        jdbcTemplate.update("""
                INSERT INTO dryjbxx (
                    dwbm, grbm, xm, sfzh, xb, csny, ryfl, dwsx, gwfl, cjgzny, zzny, jrny, jrfs,
                    zdgznx, gznx, jhlqsny, zdjhlnx, xlbm, zgxl, bjglxlnx, tc, txsj, bgdwjc,
                    zwjb, zjbm, xrzw, srny, tgbl, jtbl, fddc, khqk, dynkh, denkh, bbz, bh,
                    gryhzh, spdw, mz, zzmm, fdgd, fdsj, jzgb, ydwzw, yzwrzsj, dah, sfjzgb, yctxsj
                ) VALUES (
                    :organizationCode, :personCode, :name, :idCard, :gender, :birthYearMonth, :personnelCategory, :organizationType, :postCategory,
                    :workStartYearMonth, :regularizationYearMonth, '', '', 0, :salaryYears, '', 0, :educationCode, :highestEducation, 0, '', '',
                    '', :currentPositionLevel, :currentRankCode, :currentPosition, :currentPositionStartYearMonth, 0, '', '', '', '', '', '', '',
                    '', '', :ethnicity, :politicalStatus, '', '', '', '', '', :archiveNumber, '', 0
                )
                """, params);
        Integer uid = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", new MapSqlParameterSource(), Integer.class);
        return uid == null ? 0 : uid;
    }

    void updatePersonnel(int uid, PersonnelMaintenanceRequest request) {
        MapSqlParameterSource params = maintenanceParameters(request).addValue("uid", uid);
        jdbcTemplate.update("""
                UPDATE dryjbxx
                SET dwbm = :organizationCode,
                    grbm = :personCode,
                    xm = :name,
                    sfzh = :idCard,
                    xb = :gender,
                    csny = :birthYearMonth,
                    ryfl = :personnelCategory,
                    dwsx = :organizationType,
                    gwfl = :postCategory,
                    cjgzny = :workStartYearMonth,
                    zzny = :regularizationYearMonth,
                    gznx = :salaryYears,
                    xlbm = :educationCode,
                    zgxl = :highestEducation,
                    zwjb = :currentPositionLevel,
                    zjbm = :currentRankCode,
                    xrzw = :currentPosition,
                    srny = :currentPositionStartYearMonth,
                    mz = :ethnicity,
                    zzmm = :politicalStatus,
                    dah = :archiveNumber
                WHERE uid = :uid
                """, params);
    }

    void deletePersonnel(int uid) {
        jdbcTemplate.update("DELETE FROM dryjbxx WHERE uid = :uid", new MapSqlParameterSource("uid", uid));
    }

    Optional<PersonKey> findKeyByUid(int uid) {
        return jdbcTemplate.query("""
                SELECT uid, dwbm, grbm
                FROM dryjbxx
                WHERE uid = :uid
                """, new MapSqlParameterSource("uid", uid), (rs, rowNum) -> new PersonKey(
                SqlText.trim(rs.getString("dwbm")),
                SqlText.trim(rs.getString("grbm"))
        )).stream().findFirst();
    }

    List<PositionRecord> findPositions(PersonKey key) {
        return jdbcTemplate.query("""
                SELECT id, dwbm, grbm, xrzwbm, xrzw, zwjb, zjbm, zwbm, xzzw, zwlb, srny, kjnx, xrzwbz, jsbz
                FROM dryzwbh
                WHERE dwbm = :dwbm AND grbm = :grbm
                ORDER BY srny DESC, id DESC
                """, keyParameters(key), POSITION_MAPPER);
    }

    List<PersonnelPositionHistoryRecord> findPositionHistories(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource params = personnelHistoryParameters(organizationScope, organizationCode, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT z.id, z.dwbm, dw.dwmc, z.grbm, p.xm, z.xrzwbm, z.xrzw, z.zwjb, z.zjbm,
                       z.zwbm, z.xzzw, z.zwlb, z.srny, z.kjnx, z.xrzwbz, z.jsbz
                FROM dryzwbh z
                LEFT JOIN dryjbxx p ON p.dwbm = z.dwbm AND p.grbm = z.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = z.dwbm
                WHERE (:allOrganizations = TRUE OR z.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR z.dwbm = :organizationCode)
                  AND (:keyword IS NULL OR z.grbm LIKE :keywordLike OR p.xm LIKE :keywordLike
                       OR z.xrzw LIKE :keywordLike OR z.xzzw LIKE :keywordLike OR z.zwbm = :keyword)
                ORDER BY z.dwbm, z.grbm, z.srny DESC, z.id DESC
                LIMIT :limit OFFSET :offset
                """, params, POSITION_HISTORY_MAPPER);
    }

    long countPositionHistories(OrganizationScope organizationScope, String organizationCode, String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dryzwbh z
                LEFT JOIN dryjbxx p ON p.dwbm = z.dwbm AND p.grbm = z.grbm
                WHERE (:allOrganizations = TRUE OR z.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR z.dwbm = :organizationCode)
                  AND (:keyword IS NULL OR z.grbm LIKE :keywordLike OR p.xm LIKE :keywordLike
                       OR z.xrzw LIKE :keywordLike OR z.xzzw LIKE :keywordLike OR z.zwbm = :keyword)
                """, personnelHistoryParameters(organizationScope, organizationCode, keyword), Long.class);
        return count == null ? 0 : count;
    }

    List<EducationRecord> findEducation(PersonKey key) {
        return jdbcTemplate.query("""
                SELECT id, dwbm, grbm, xlbm, xl, byyx, rxsj, bysj, xz, xllb, bz
                FROM dxl
                WHERE dwbm = :dwbm AND grbm = :grbm
                ORDER BY bysj DESC, xlbm
                """, keyParameters(key), EDUCATION_MAPPER);
    }

    List<PersonnelEducationHistoryRecord> findEducationHistories(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource params = personnelHistoryParameters(organizationScope, organizationCode, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT e.id, e.dwbm, dw.dwmc, e.grbm, p.xm, e.xlbm, e.xl, e.byyx,
                       e.rxsj, e.bysj, e.xz, e.xllb, e.bz
                FROM dxl e
                LEFT JOIN dryjbxx p ON p.dwbm = e.dwbm AND p.grbm = e.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = e.dwbm
                WHERE (:allOrganizations = TRUE OR e.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR e.dwbm = :organizationCode)
                  AND (:keyword IS NULL OR e.grbm LIKE :keywordLike OR p.xm LIKE :keywordLike
                       OR e.xlbm = :keyword OR e.xl LIKE :keywordLike OR e.byyx LIKE :keywordLike OR e.xllb LIKE :keywordLike)
                ORDER BY e.dwbm, e.grbm, e.bysj DESC, e.xlbm, e.id DESC
                LIMIT :limit OFFSET :offset
                """, params, EDUCATION_HISTORY_MAPPER);
    }

    long countEducationHistories(OrganizationScope organizationScope, String organizationCode, String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dxl e
                LEFT JOIN dryjbxx p ON p.dwbm = e.dwbm AND p.grbm = e.grbm
                WHERE (:allOrganizations = TRUE OR e.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR e.dwbm = :organizationCode)
                  AND (:keyword IS NULL OR e.grbm LIKE :keywordLike OR p.xm LIKE :keywordLike
                       OR e.xlbm = :keyword OR e.xl LIKE :keywordLike OR e.byyx LIKE :keywordLike OR e.xllb LIKE :keywordLike)
                """, personnelHistoryParameters(organizationScope, organizationCode, keyword), Long.class);
        return count == null ? 0 : count;
    }

    List<ChangedPersonnelRecord> findChangedPersonnel(
            OrganizationScope organizationScope,
            String organizationCode,
            String period,
            String keyword,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource params = changedPersonnelParameters(organizationScope, organizationCode, period, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT b.dwbm, dw.dwmc, b.grbm, b.xm, b.sfzh, b.xb, b.csny, b.ryfl, b.dwsx, b.gwfl,
                       h.jsnf, h.jsyf, h.jslb, h.zwbm2, h.zwgw2, h.hj2, h.tbnd, h.jbtbz, b.bz
                FROM dryjbxxb b
                LEFT JOIN dwbm dw ON dw.dwbm = b.dwbm
                LEFT JOIN hisbaseb h ON h.dwbm = b.dwbm AND h.grbm = b.grbm AND (h.sid IS NULL OR TRIM(h.sid) = '')
                WHERE (:allOrganizations = TRUE OR b.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR b.dwbm = :organizationCode)
                  AND (:period IS NULL OR CONCAT(h.jsnf, h.jsyf) = :period)
                  AND (:keyword IS NULL OR b.grbm LIKE :keywordLike OR b.xm LIKE :keywordLike
                       OR b.sfzh LIKE :keywordLike OR h.jslb LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                ORDER BY b.dwbm, b.grbm, h.jsnf DESC, h.jsyf DESC, h.jslb
                LIMIT :limit OFFSET :offset
                """, params, CHANGED_PERSONNEL_MAPPER);
    }

    long countChangedPersonnel(OrganizationScope organizationScope, String organizationCode, String period, String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dryjbxxb b
                LEFT JOIN hisbaseb h ON h.dwbm = b.dwbm AND h.grbm = b.grbm AND (h.sid IS NULL OR TRIM(h.sid) = '')
                WHERE (:allOrganizations = TRUE OR b.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR b.dwbm = :organizationCode)
                  AND (:period IS NULL OR CONCAT(h.jsnf, h.jsyf) = :period)
                  AND (:keyword IS NULL OR b.grbm LIKE :keywordLike OR b.xm LIKE :keywordLike
                       OR b.sfzh LIKE :keywordLike OR h.jslb LIKE :keywordLike OR h.zwgw2 LIKE :keywordLike)
                """, changedPersonnelParameters(organizationScope, organizationCode, period, keyword), Long.class);
        return count == null ? 0 : count;
    }

    List<AssessmentRecord> findAssessments(PersonKey key) {
        return jdbcTemplate.query("""
                SELECT id, dwbm, grbm, khnd, khjg
                FROM dndkh
                WHERE dwbm = :dwbm AND grbm = :grbm
                ORDER BY khnd DESC
                """, keyParameters(key), ASSESSMENT_MAPPER);
    }

    List<AnnualAssessmentRecord> findAnnualAssessments(
            OrganizationScope organizationScope,
            String organizationCode,
            String year,
            String keyword,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource params = assessmentParameters(organizationScope, organizationCode, year, keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT a.id, a.dwbm, dw.dwmc, a.grbm, p.xm, a.khnd, a.khjg
                FROM dndkh a
                LEFT JOIN dryjbxx p ON p.dwbm = a.dwbm AND p.grbm = a.grbm
                LEFT JOIN dwbm dw ON dw.dwbm = a.dwbm
                WHERE (:allOrganizations = TRUE OR a.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR a.dwbm = :organizationCode)
                  AND (:year IS NULL OR a.khnd = :year)
                  AND (:keyword IS NULL OR a.grbm LIKE :keywordLike OR p.xm LIKE :keywordLike OR a.khjg LIKE :keywordLike)
                ORDER BY a.khnd DESC, a.dwbm, a.grbm
                LIMIT :limit OFFSET :offset
                """, params, ANNUAL_ASSESSMENT_MAPPER);
    }

    long countAnnualAssessments(
            OrganizationScope organizationScope,
            String organizationCode,
            String year,
            String keyword) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dndkh a
                LEFT JOIN dryjbxx p ON p.dwbm = a.dwbm AND p.grbm = a.grbm
                WHERE (:allOrganizations = TRUE OR a.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR a.dwbm = :organizationCode)
                  AND (:year IS NULL OR a.khnd = :year)
                  AND (:keyword IS NULL OR a.grbm LIKE :keywordLike OR p.xm LIKE :keywordLike OR a.khjg LIKE :keywordLike)
                """, assessmentParameters(organizationScope, organizationCode, year, keyword), Long.class);
        return count == null ? 0 : count;
    }

    List<AnnualAssessmentSummaryRecord> findAnnualAssessmentSummary(
            OrganizationScope organizationScope,
            String organizationCode,
            String year,
            String result,
            PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource params = assessmentSummaryParameters(organizationScope, organizationCode, year, result)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT a.khnd, a.dwbm, dw.dwmc, a.khjg, COUNT(*) AS personnel_count
                FROM dndkh a
                LEFT JOIN dwbm dw ON dw.dwbm = a.dwbm
                WHERE (:allOrganizations = TRUE OR a.dwbm IN (:organizationCodes))
                  AND (:organizationCode IS NULL OR a.dwbm = :organizationCode)
                  AND (:year IS NULL OR a.khnd = :year)
                  AND (:result IS NULL OR a.khjg = :result)
                GROUP BY a.khnd, a.dwbm, dw.dwmc, a.khjg
                ORDER BY a.khnd DESC, a.dwbm, a.khjg
                LIMIT :limit OFFSET :offset
                """, params, ANNUAL_ASSESSMENT_SUMMARY_MAPPER);
    }

    long countAnnualAssessmentSummary(
            OrganizationScope organizationScope,
            String organizationCode,
            String year,
            String result) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM (
                    SELECT 1
                    FROM dndkh a
                    WHERE (:allOrganizations = TRUE OR a.dwbm IN (:organizationCodes))
                      AND (:organizationCode IS NULL OR a.dwbm = :organizationCode)
                      AND (:year IS NULL OR a.khnd = :year)
                      AND (:result IS NULL OR a.khjg = :result)
                    GROUP BY a.khnd, a.dwbm, a.khjg
                ) grouped_assessment
                """, assessmentSummaryParameters(organizationScope, organizationCode, year, result), Long.class);
        return count == null ? 0 : count;
    }

    private MapSqlParameterSource parameters(OrganizationScope organizationScope, String keyword) {
        String trimmedKeyword = SqlText.trim(keyword);
        return new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes())
                .addValue("keyword", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : "%" + trimmedKeyword + "%");
    }

    private MapSqlParameterSource maintenanceParameters(PersonnelMaintenanceRequest request) {
        return new MapSqlParameterSource()
                .addValue("organizationCode", valueOrBlank(request.organizationCode()))
                .addValue("personCode", valueOrBlank(request.personCode()))
                .addValue("name", valueOrBlank(request.name()))
                .addValue("idCard", valueOrBlank(request.idCard()))
                .addValue("gender", valueOrBlank(request.gender()))
                .addValue("birthYearMonth", valueOrBlank(request.birthYearMonth()))
                .addValue("personnelCategory", valueOrBlank(request.personnelCategory()))
                .addValue("organizationType", valueOrBlank(request.organizationType()))
                .addValue("postCategory", valueOrBlank(request.postCategory()))
                .addValue("workStartYearMonth", valueOrBlank(request.workStartYearMonth()))
                .addValue("regularizationYearMonth", valueOrBlank(request.regularizationYearMonth()))
                .addValue("salaryYears", request.salaryYears() == null ? 0 : request.salaryYears())
                .addValue("educationCode", valueOrBlank(request.educationCode()))
                .addValue("highestEducation", valueOrBlank(request.highestEducation()))
                .addValue("currentPositionLevel", valueOrBlank(request.currentPositionLevel()))
                .addValue("currentRankCode", valueOrBlank(request.currentRankCode()))
                .addValue("currentPosition", valueOrBlank(request.currentPosition()))
                .addValue("currentPositionStartYearMonth", valueOrBlank(request.currentPositionStartYearMonth()))
                .addValue("ethnicity", valueOrBlank(request.ethnicity()))
                .addValue("politicalStatus", valueOrBlank(request.politicalStatus()))
                .addValue("archiveNumber", valueOrBlank(request.archiveNumber()));
    }

    private String valueOrBlank(String value) {
        String trimmed = SqlText.trim(value);
        return trimmed == null ? "" : trimmed;
    }

    private MapSqlParameterSource keyParameters(PersonKey key) {
        return new MapSqlParameterSource()
                .addValue("dwbm", key.organizationCode())
                .addValue("grbm", key.personCode());
    }

    private MapSqlParameterSource assessmentParameters(
            OrganizationScope organizationScope,
            String organizationCode,
            String year,
            String keyword) {
        String trimmedKeyword = SqlText.trim(keyword);
        return new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes())
                .addValue("organizationCode", emptyToNull(organizationCode))
                .addValue("year", emptyToNull(year))
                .addValue("keyword", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : "%" + trimmedKeyword + "%");
    }

    private MapSqlParameterSource assessmentSummaryParameters(
            OrganizationScope organizationScope,
            String organizationCode,
            String year,
            String result) {
        return new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes())
                .addValue("organizationCode", emptyToNull(organizationCode))
                .addValue("year", emptyToNull(year))
                .addValue("result", emptyToNull(result));
    }

    private MapSqlParameterSource personnelHistoryParameters(
            OrganizationScope organizationScope,
            String organizationCode,
            String keyword) {
        String trimmedKeyword = SqlText.trim(keyword);
        return new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes())
                .addValue("organizationCode", emptyToNull(organizationCode))
                .addValue("keyword", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : "%" + trimmedKeyword + "%");
    }

    private MapSqlParameterSource changedPersonnelParameters(
            OrganizationScope organizationScope,
            String organizationCode,
            String period,
            String keyword) {
        String trimmedKeyword = SqlText.trim(keyword);
        return new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes())
                .addValue("organizationCode", emptyToNull(organizationCode))
                .addValue("period", emptyToNull(period))
                .addValue("keyword", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null || trimmedKeyword.isEmpty() ? null : "%" + trimmedKeyword + "%");
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
