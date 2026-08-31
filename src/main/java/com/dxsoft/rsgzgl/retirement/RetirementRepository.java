package com.dxsoft.rsgzgl.retirement;

import com.dxsoft.rsgzgl.common.SqlText;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import com.dxsoft.rsgzgl.statistics.RetirementMonthCalculator;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
class RetirementRepository {

    private static final RowMapper<RetirementSeedRow> SEED_ROW_MAPPER = (rs, rowNum) -> new RetirementSeedRow(
            rs.getInt("uid"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("grbm")),
            SqlText.trim(rs.getString("xm")),
            SqlText.trim(rs.getString("sfzh")),
            SqlText.trim(rs.getString("xb")),
            SqlText.trim(rs.getString("csny")),
            SqlText.trim(rs.getString("dwsx")),
            SqlText.trim(rs.getString("ryfl")),
            SqlText.trim(rs.getString("gwfl")),
            SqlText.trim(rs.getString("cjgzny")),
            rs.getInt("zdgznx"),
            rs.getInt("gznx"),
            SqlText.trim(rs.getString("zgxl")),
            rs.getInt("bjglxlnx"),
            SqlText.trim(rs.getString("mz")),
            SqlText.trim(rs.getString("spdw")),
            SqlText.trim(rs.getString("xckhndzw")),
            SqlText.trim(rs.getString("xckhndjb")),
            SqlText.trim(rs.getString("dynkh")),
            SqlText.trim(rs.getString("denkh")),
            rs.getInt("tgbl"),
            SqlText.trim(rs.getString("jhlqsny")),
            rs.getInt("zdjhlnx"),
            SqlText.trim(rs.getString("zwbm2")),
            SqlText.trim(rs.getString("zwgw2")),
            SqlText.trim(rs.getString("zwgzdc2")),
            SqlText.trim(rs.getString("jbgzjb2")),
            SqlText.trim(rs.getString("djc2")),
            SqlText.trim(rs.getString("tbnd")),
            SqlText.trim(rs.getString("jbtbz")),
            rs.getInt("zwgzse2"),
            rs.getInt("jbgzse2"),
            rs.getInt("jsdjgz2"),
            rs.getInt("jsfszwtg2"),
            rs.getInt("jxjt"),
            rs.getInt("blfb2"),
            rs.getInt("dfbt2"),
            rs.getInt("gwjt2"),
            rs.getInt("fdgz2"),
            rs.getInt("jjjy2"),
            rs.getInt("sdbt"),
            rs.getInt("jzmcbt"),
            rs.getInt("zwjt"),
            rs.getInt("qtbt"),
            rs.getInt("zzbc"),
            rs.getInt("jhljt"),
            rs.getInt("hj2"));

    private final NamedParameterJdbcTemplate jdbcTemplate;

    RetirementRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Optional<String> findPositionCodeByName(String positionName) {
        if (positionName == null || positionName.isBlank()) {
            return Optional.empty();
        }
        return jdbcTemplate.query("""
                SELECT bm
                FROM dmb
                WHERE TRIM(mc) = :name
                  AND (sfsy IS NULL OR sfsy = 1)
                ORDER BY LENGTH(bm), bm
                LIMIT 1
                """, new MapSqlParameterSource("name", positionName.trim()), (rs, rowNum) ->
                SqlText.trim(rs.getString("bm"))).stream().findFirst();
    }

    Optional<int[]> findPositionLevelRange(String positionCode) {
        if (positionCode == null || positionCode.isBlank()) {
            return Optional.empty();
        }
        return jdbcTemplate.query("""
                SELECT `min`, `max`
                FROM bz06_zw_jb_xj
                WHERE zwbm = :positionCode
                LIMIT 1
                """, new MapSqlParameterSource("positionCode", positionCode.trim()), (rs, rowNum) -> new int[]{
                rs.getInt("min"),
                rs.getInt("max")
        }).stream().findFirst();
    }

    List<RetirementSeedRow> findActiveSeedCandidates(
            OrganizationScope scope,
            String organizationCode,
            String keyword,
            boolean includeDescendants) {
        if (scope.noneScope()) {
            return List.of();
        }
        String trimmedOrganization = emptyToNull(organizationCode);
        MapSqlParameterSource parameters = scopedParameters(scope, trimmedOrganization)
                .addValue("organizationCodeLike", trimmedOrganization == null ? null : trimmedOrganization + "%")
                .addValue("includeDescendants", includeDescendants && trimmedOrganization != null)
                .addValue("keyword", emptyToNull(keyword))
                .addValue("keywordLike", emptyToNull(keyword) == null ? null : "%" + keyword.trim() + "%");
        return jdbcTemplate.query("""
                SELECT p.uid, p.dwbm, dw.dwmc, p.grbm, p.xm, p.sfzh, p.xb, p.csny, p.dwsx, p.ryfl, p.gwfl,
                       p.cjgzny, p.zdgznx, COALESCE(h.gznx, p.gznx) AS gznx, p.zgxl, p.bjglxlnx, p.mz, p.spdw,
                       h.xckhndzw, h.xckhndjb, h.dynkh, h.denkh, h.tgbl, h.jhlqsny, h.zdjhlnx,
                       h.zwbm2, h.zwgw2, h.zwgzdc2, h.jbgzjb2, h.djc2, h.tbnd, h.jbtbz,
                       h.zwgzse2, h.jbgzse2, h.jsdjgz2, h.jsfszwtg2, h.jxjt, h.blfb2, h.dfbt2, h.gwjt2,
                       h.fdgz2, h.jjjy2, h.sdbt, h.jzmcbt, h.zwjt, h.qtbt, h.zzbc, h.jhljt, h.hj2
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                INNER JOIN hisbase h ON h.id = (
                    SELECT h2.id
                    FROM hisbase h2
                    WHERE h2.dwbm = p.dwbm
                      AND h2.grbm = p.grbm
                      AND (h2.sid IS NULL OR TRIM(h2.sid) = '')
                    ORDER BY COALESCE(h2.jsnf, '') DESC, COALESCE(h2.jsyf, '') DESC, h2.id DESC
                    LIMIT 1
                )
                WHERE (:allOrganizations = TRUE OR p.dwbm IN (:organizationCodes))
                  AND (
                        :organizationCode IS NULL
                     OR (:includeDescendants = TRUE AND p.dwbm LIKE :organizationCodeLike)
                     OR (:includeDescendants = FALSE AND p.dwbm = :organizationCode)
                  )
                  AND REPLACE(COALESCE(NULLIF(TRIM(p.csny), ''), '000000'), '.', '') >= '190001'
                  AND (:keyword IS NULL
                       OR p.grbm LIKE :keywordLike
                       OR p.xm LIKE :keywordLike
                       OR p.sfzh LIKE :keywordLike
                       OR h.zwbm2 LIKE :keywordLike
                       OR h.zwgw2 LIKE :keywordLike)
                ORDER BY p.dwbm, p.grbm
                """, parameters, SEED_ROW_MAPPER);
    }

    Optional<RetirementSeedRow> findActiveSeedByUid(int uid) {
        return jdbcTemplate.query("""
                SELECT p.uid, p.dwbm, dw.dwmc, p.grbm, p.xm, p.sfzh, p.xb, p.csny, p.dwsx, p.ryfl, p.gwfl,
                       p.cjgzny, p.zdgznx, COALESCE(h.gznx, p.gznx) AS gznx, p.zgxl, p.bjglxlnx, p.mz, p.spdw,
                       h.xckhndzw, h.xckhndjb, h.dynkh, h.denkh, h.tgbl, h.jhlqsny, h.zdjhlnx,
                       h.zwbm2, h.zwgw2, h.zwgzdc2, h.jbgzjb2, h.djc2, h.tbnd, h.jbtbz,
                       h.zwgzse2, h.jbgzse2, h.jsdjgz2, h.jsfszwtg2, h.jxjt, h.blfb2, h.dfbt2, h.gwjt2,
                       h.fdgz2, h.jjjy2, h.sdbt, h.jzmcbt, h.zwjt, h.qtbt, h.zzbc, h.jhljt, h.hj2
                FROM dryjbxx p
                LEFT JOIN dwbm dw ON dw.dwbm = p.dwbm
                INNER JOIN hisbase h ON h.id = (
                    SELECT h2.id
                    FROM hisbase h2
                    WHERE h2.dwbm = p.dwbm
                      AND h2.grbm = p.grbm
                      AND (h2.sid IS NULL OR TRIM(h2.sid) = '')
                    ORDER BY COALESCE(h2.jsnf, '') DESC, COALESCE(h2.jsyf, '') DESC, h2.id DESC
                    LIMIT 1
                )
                WHERE p.uid = :uid
                """, new MapSqlParameterSource("uid", uid), SEED_ROW_MAPPER).stream().findFirst();
    }

    boolean existsRetireeByInterfaceKey(String sourceOrganizationCode, String sourcePersonCode) {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM ryjbxxb
                WHERE jkdwbm = :organizationCode AND jkgrbm = :personCode
                """, new MapSqlParameterSource()
                .addValue("organizationCode", sourceOrganizationCode)
                .addValue("personCode", sourcePersonCode), Long.class);
        return count != null && count > 0;
    }

    String allocateRetireePersonCode(String organizationCode) {
        String maxCode = jdbcTemplate.query("""
                SELECT COALESCE(MAX(grbm), '') AS max_code
                FROM ryjbxxb
                WHERE dwbm = :organizationCode
                """, new MapSqlParameterSource("organizationCode", organizationCode),
                (rs, rowNum) -> SqlText.trim(rs.getString("max_code"))).stream()
                .findFirst()
                .map(code -> code == null ? "" : code)
                .orElse("");
        int next = 1;
        if (maxCode != null && !maxCode.isBlank()) {
            try {
                next = Integer.parseInt(maxCode.replaceAll("\\D", "")) + 1;
            } catch (NumberFormatException ignored) {
                next = 1;
            }
        }
        return String.format("%05d", next);
    }

    int lookupConversionRatio(String postCategory, int salaryYears, String retirementCategory) {
        if ("离休".equals(trim(retirementCategory))) {
            return 100;
        }
        String bandCategory = resolveRatioCategory(postCategory, retirementCategory);
        if (bandCategory.isBlank()) {
            return 0;
        }
        return jdbcTemplate.query("""
                SELECT a1, a2, a3, a4, a5, a6, a7, a8, a9, a10
                FROM zsbl06
                WHERE lb = :category
                LIMIT 1
                """, new MapSqlParameterSource("category", bandCategory), rs -> {
            if (!rs.next()) {
                return 0;
            }
            for (int i = 1; i <= 9; i += 2) {
                String range = SqlText.trim(rs.getString("a" + i));
                int rate = rs.getInt("a" + (i + 1));
                int upper = parseYearBandUpper(range);
                if (salaryYears <= upper) {
                    return rate;
                }
            }
            return 0;
        });
    }

    int insertRetiree(RetirementSeedInsert insert) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update("""
                INSERT INTO ryjbxxb (
                    dwbm, grbm, xm, sfzh, xb, csny, dwsx, ryfl, gwfl, cjgzny, zdgznx, gznx, zgxl, bjglxlnx,
                    tc, txsj, xckhndzw, xckhndjb, dynkh, denkh, bbz, tgbl, mz, spdw, ltyy,
                    jsqtbny, jshtbny, jbtbz, jbtbz1, jkdwbm, jkgrbm, zjbl, lczjldxf, zzbc, jhl, jhljt,
                    zwbm1, zwgw1, zwgzdc1, zwgzse1, jbgzjb1, djc1, jbgzse1, jsdjgz1, jsfszwtg1, jxjt1,
                    blfb1, dfbt1, gwjt1, fdgz1, jjjy1, sdbt1, jzmcbt1, zwjt1, qtbt1, hj1,
                    zwbm2, zwgw2, zwgzdc2, zwgzse2, jbgzjb2, djc2, jbgzse2, jsdjgz2, jsfszwtg2, jxjt,
                    blfb2, dfbt2, gwjt2, fdgz2, jjjy2, sdbt, jzmcbt, zwjt, qtbt, jbldxf, hj2
                ) VALUES (
                    :organizationCode, :retireePersonCode, :name, :idCard, :gender, :birthYearMonth, :organizationType,
                    :personnelCategory, :postCategory, :workStartYearMonth, :interruptedYears, :salaryYears, :education,
                    :educationYears, :retirementCategory, :retirementDate, :stepAssessmentYear, :levelAssessmentYear,
                    :interruptedNote, :interruptedMonths, :approvalStatus, :raisePercentage, :nation, :approvalOrganization,
                    :retirementReason, :standardYearMonth, :standardYearMonth, :allowanceStandard, :allowanceStandard,
                    :sourceOrganizationCode, :sourcePersonCode, 0, 0, :retainedSpecial, :teachingYears, :teachingAllowance,
                    :positionCode, :positionName, :gradeStep, :positionSalary, :gradeLevel, :gradeStepExtra, :gradeSalary,
                    :technicalSalary, :teachingRaise, :rankAllowance, :beforeRetainedAllowance, :beforeLocalAllowance, :beforePostAllowance,
                    :beforeFloatingSalary, :beforeBonusBalance, :beforeLivingAllowance, :beforeSpecialPostAllowance, :beforePositionAllowance, :beforeOtherAllowance,
                    :beforeTotal, :positionCode, :positionName, :gradeStep, :positionSalary, :gradeLevel, :gradeStepExtra,
                    :gradeSalary, :technicalSalary, :teachingRaise, :rankAllowance, :retainedAllowance, :localAllowance,
                    :postAllowance, :floatingSalary, :bonusBalance, :livingAllowance, :specialPostAllowance, :positionAllowance,
                    :otherAllowance, :convertedBase, :estimatedTotal
                )
                """, insert.parameters(), keyHolder, new String[]{"id"});
        Number key = keyHolder.getKey();
        return key == null ? 0 : key.intValue();
    }

    long countRetirees(
            OrganizationScope scope,
            String organizationCode,
            String keyword,
            boolean postReformOnly,
            boolean includeDescendants,
            boolean pendingOnly) {
        if (scope.noneScope()) {
            return 0L;
        }
        Long count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM ryjbxxb r
                WHERE """ + retireeWhereClause(postReformOnly),
                retireeParameters(scope, organizationCode, keyword, includeDescendants, pendingOnly),
                Long.class);
        return count == null ? 0L : count;
    }

    List<RetirementRetireeRecord> findRetirees(
            OrganizationScope scope,
            String organizationCode,
            String keyword,
            boolean postReformOnly,
            boolean includeDescendants,
            boolean pendingOnly,
            int limit,
            int offset) {
        if (scope.noneScope()) {
            return List.of();
        }
        return jdbcTemplate.query("""
                SELECT r.id, r.dwbm, dw.dwmc, r.grbm, r.xm, r.xb, r.sfzh, r.tc, r.txsj, r.gwfl,
                       COALESCE(NULLIF(TRIM(r.zwbm2), ''), NULLIF(TRIM(r.zwbm1), '')) AS position_code,
                       COALESCE(NULLIF(TRIM(r.zwgw2), ''), NULLIF(TRIM(r.zwgw1), '')) AS position_name,
                       r.gznx, COALESCE(r.hj2, r.hj1, 0) AS total_amount, r.bbz, r.jkdwbm, r.jkgrbm
                FROM ryjbxxb r
                LEFT JOIN dwbm dw ON dw.dwbm = r.dwbm
                WHERE """ + retireeWhereClause(postReformOnly) + """
                ORDER BY r.dwbm, r.txsj DESC, r.grbm
                LIMIT :limit OFFSET :offset
                """, retireeParameters(scope, organizationCode, keyword, includeDescendants, pendingOnly)
                .addValue("limit", Math.max(limit, 1))
                .addValue("offset", Math.max(offset, 0)),
                (rs, rowNum) -> new RetirementRetireeRecord(
                        rs.getInt("id"),
                        SqlText.trim(rs.getString("dwbm")),
                        SqlText.trim(rs.getString("dwmc")),
                        SqlText.trim(rs.getString("grbm")),
                        SqlText.trim(rs.getString("xm")),
                        SqlText.trim(rs.getString("xb")),
                        SqlText.trim(rs.getString("sfzh")),
                        SqlText.trim(rs.getString("tc")),
                        RetirementMonthCalculator.formatYearMonth(SqlText.trim(rs.getString("txsj"))),
                        SqlText.trim(rs.getString("gwfl")),
                        SqlText.trim(rs.getString("position_code")),
                        SqlText.trim(rs.getString("position_name")),
                        rs.getInt("gznx"),
                        rs.getInt("total_amount"),
                        SqlText.trim(rs.getString("bbz")),
                        SqlText.trim(rs.getString("jkdwbm")),
                        SqlText.trim(rs.getString("jkgrbm"))));
    }

    Optional<RetirementRetireeDetailRow> findRetireeDetailById(OrganizationScope scope, int id) {
        if (scope.noneScope()) {
            return Optional.empty();
        }
        return jdbcTemplate.query("""
                SELECT r.id, r.dwbm, dw.dwmc, r.grbm, r.xm, r.xb, r.sfzh, r.mz, r.csny, r.cjgzny,
                       r.zdgznx, r.gznx, r.zgxl, r.tc, r.txsj, r.ltyy, r.gwfl,
                       COALESCE(NULLIF(TRIM(r.zwbm2), ''), NULLIF(TRIM(r.zwbm1), '')) AS position_code,
                       COALESCE(NULLIF(TRIM(r.zwgw2), ''), NULLIF(TRIM(r.zwgw1), '')) AS position_name,
                       COALESCE(NULLIF(TRIM(r.jbgzjb2), ''), NULLIF(TRIM(r.jbgzjb1), '')) AS grade_level,
                       COALESCE(NULLIF(TRIM(r.zwgzdc2), ''), NULLIF(TRIM(r.zwgzdc1), '')) AS grade_step,
                       r.xckhndzw, r.jshtbny, r.jbtbz, r.spdw, r.bbz, r.pzwh, r.dynkh, r.denkh,
                       r.zjblyy, r.tgbl, r.zjbl, COALESCE(r.jhl, 0) AS teaching_years,
                       COALESCE(r.zwgzse1, 0) AS position_salary,
                       COALESCE(r.jbgzse1, 0) AS grade_salary,
                       COALESCE(r.jsdjgz1, 0) AS technical_salary,
                       COALESCE(r.jsfszwtg1, r.jsfszwtg2, 0) AS teaching_raise,
                       COALESCE(r.jxjt, 0) AS rank_allowance,
                       COALESCE(r.zwgzse1, 0) AS before_position_salary,
                       COALESCE(r.jbgzse1, 0) AS before_grade_salary,
                       COALESCE(r.jsdjgz1, 0) AS before_technical_salary,
                       COALESCE(r.jsfszwtg1, 0) AS before_teaching_raise,
                       COALESCE(r.jxjt, 0) AS before_rank_allowance,
                       COALESCE(r.jbldxf, 0) AS basic_fee, COALESCE(r.lczjldxf, 0) AS cumulative_increase,
                       COALESCE(r.hj2, 0) AS total_amount, COALESCE(r.hj1, 0) AS before_total,
                       COALESCE(r.blfb1, 0) AS before_retained_allowance,
                       COALESCE(r.dfbt1, 0) AS before_local_allowance,
                       COALESCE(r.gwjt1, 0) AS before_post_allowance,
                       COALESCE(r.fdgz1, 0) AS before_floating_salary,
                       COALESCE(r.jjjy1, 0) AS before_bonus_balance,
                       COALESCE(r.sdbt1, 0) AS before_living_allowance,
                       COALESCE(r.jzmcbt1, 0) AS before_special_post_allowance,
                       COALESCE(r.zwjt1, 0) AS before_position_allowance,
                       COALESCE(r.qtbt1, 0) AS before_other_allowance,
                       COALESCE(r.blfb2, 0) AS retained_allowance, COALESCE(r.dfbt2, 0) AS local_allowance,
                       COALESCE(r.gwjt2, 0) AS post_allowance, COALESCE(r.fdgz2, 0) AS floating_salary,
                       COALESCE(r.jjjy2, 0) AS bonus_balance,
                       COALESCE(r.sdbt, 0) AS living_allowance, COALESCE(r.jzmcbt, 0) AS special_post_allowance,
                       COALESCE(r.zwjt, 0) AS position_allowance, COALESCE(r.qtbt, 0) AS other_allowance,
                       r.gryhzh, r.jkdwbm, r.jkgrbm
                FROM ryjbxxb r
                LEFT JOIN dwbm dw ON dw.dwbm = r.dwbm
                WHERE r.id = :id
                  AND (:allOrganizations = TRUE OR r.dwbm IN (:organizationCodes))
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("allOrganizations", scope.all())
                .addValue("organizationCodes",
                        scope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : scope.organizationCodes()),
                (rs, rowNum) -> new RetirementRetireeDetailRow(
                        rs.getInt("id"),
                        SqlText.trim(rs.getString("dwbm")),
                        SqlText.trim(rs.getString("dwmc")),
                        SqlText.trim(rs.getString("grbm")),
                        SqlText.trim(rs.getString("xm")),
                        SqlText.trim(rs.getString("xb")),
                        SqlText.trim(rs.getString("sfzh")),
                        SqlText.trim(rs.getString("mz")),
                        SqlText.trim(rs.getString("csny")),
                        SqlText.trim(rs.getString("cjgzny")),
                        rs.getInt("zdgznx"),
                        rs.getInt("gznx"),
                        SqlText.trim(rs.getString("zgxl")),
                        SqlText.trim(rs.getString("tc")),
                        SqlText.trim(rs.getString("txsj")),
                        SqlText.trim(rs.getString("ltyy")),
                        SqlText.trim(rs.getString("gwfl")),
                        SqlText.trim(rs.getString("position_code")),
                        SqlText.trim(rs.getString("position_name")),
                        SqlText.trim(rs.getString("grade_level")),
                        SqlText.trim(rs.getString("grade_step")),
                        SqlText.trim(rs.getString("xckhndzw")),
                        SqlText.trim(rs.getString("jshtbny")),
                        SqlText.trim(rs.getString("jbtbz")),
                        SqlText.trim(rs.getString("spdw")),
                        SqlText.trim(rs.getString("bbz")),
                        SqlText.trim(rs.getString("pzwh")),
                        SqlText.trim(rs.getString("dynkh")),
                        SqlText.trim(rs.getString("denkh")),
                        SqlText.trim(rs.getString("zjblyy")),
                        rs.getInt("tgbl"),
                        rs.getInt("teaching_years"),
                        rs.getInt("zjbl"),
                        rs.getInt("position_salary"),
                        rs.getInt("grade_salary"),
                        rs.getInt("technical_salary"),
                        rs.getInt("teaching_raise"),
                        rs.getInt("rank_allowance"),
                        rs.getInt("before_position_salary"),
                        rs.getInt("before_grade_salary"),
                        rs.getInt("before_technical_salary"),
                        rs.getInt("before_teaching_raise"),
                        rs.getInt("before_rank_allowance"),
                        rs.getInt("basic_fee"),
                        rs.getInt("cumulative_increase"),
                        rs.getInt("total_amount"),
                        rs.getInt("before_total"),
                        rs.getInt("before_retained_allowance"),
                        rs.getInt("before_local_allowance"),
                        rs.getInt("before_post_allowance"),
                        rs.getInt("before_floating_salary"),
                        rs.getInt("before_bonus_balance"),
                        rs.getInt("before_living_allowance"),
                        rs.getInt("before_special_post_allowance"),
                        rs.getInt("before_position_allowance"),
                        rs.getInt("before_other_allowance"),
                        rs.getInt("retained_allowance"),
                        rs.getInt("local_allowance"),
                        rs.getInt("post_allowance"),
                        rs.getInt("floating_salary"),
                        rs.getInt("bonus_balance"),
                        rs.getInt("living_allowance"),
                        rs.getInt("special_post_allowance"),
                        rs.getInt("position_allowance"),
                        rs.getInt("other_allowance"),
                        SqlText.trim(rs.getString("gryhzh")),
                        SqlText.trim(rs.getString("jkdwbm")),
                        SqlText.trim(rs.getString("jkgrbm")))).stream().findFirst();
    }

    void updateRetiree(int id, MapSqlParameterSource parameters) {
        parameters.addValue("id", id);
        int updated = jdbcTemplate.update("""
                UPDATE ryjbxxb
                SET xm = :name,
                    xb = :gender,
                    sfzh = :idCard,
                    mz = :nation,
                    csny = :birthYearMonth,
                    cjgzny = :workStartYearMonth,
                    zdgznx = :interruptedYears,
                    gznx = :salaryYears,
                    zgxl = :education,
                    tc = :retirementCategory,
                    txsj = :retirementDate,
                    ltyy = :retirementReason,
                    gwfl = :postCategory,
                    zwbm2 = :positionCode,
                    zwgw2 = :positionName,
                    jbgzjb2 = :gradeLevel,
                    zwgzdc2 = :gradeStep,
                    xckhndzw = :assessmentStartYear,
                    jshtbny = :salaryStandardYearMonth,
                    jbtbz = :allowanceStandardYearMonth,
                    spdw = :approvalOrganization,
                    tgbl = :teachingRaisePercentage,
                    jhl = :teachingYears,
                    zjbl = :increaseRatio,
                    zjblyy = :increaseReason,
                    pzwh = :approvalDocumentNumber,
                    dynkh = :interruptedNote,
                    denkh = :interruptedMonths,
                    gryhzh = :bankAccount,
                    zwgzse1 = :positionSalary,
                    jbgzse1 = :gradeSalary,
                    jsdjgz1 = :technicalSalary,
                    jsfszwtg1 = :teachingRaiseAmount,
                    jxjt = :rankAllowance,
                    jbldxf = :basicRetirementFee,
                    blfb2 = :retainedAllowance,
                    dfbt2 = :localAllowance,
                    gwjt2 = :postAllowance,
                    fdgz2 = :floatingSalary,
                    jjjy2 = :bonusBalance,
                    sdbt = :livingAllowance,
                    jzmcbt = :specialPostAllowance,
                    zwjt = :positionAllowance,
                    qtbt = :otherAllowance,
                    hj2 = :totalAmount
                WHERE id = :id
                """, parameters);
        if (updated == 0) {
            throw new IllegalArgumentException("未找到离退休人员记录：" + id);
        }
    }

    void updateRetireeApprovalStatus(int id, String approvalStatus) {
        int updated = jdbcTemplate.update("""
                UPDATE ryjbxxb
                SET bbz = :approvalStatus
                WHERE id = :id
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("approvalStatus", approvalStatus));
        if (updated == 0) {
            throw new IllegalArgumentException("未找到离退休人员记录：" + id);
        }
    }

    List<RetirementRatioStandard> findRatioStandards() {
        return jdbcTemplate.query("""
                SELECT lb, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10
                FROM zsbl06
                ORDER BY CASE lb
                    WHEN '行政管理人员' THEN 1
                    WHEN '其它' THEN 2
                    WHEN '退职' THEN 3
                    ELSE 9
                END, lb
                """, (rs, rowNum) -> new RetirementRatioStandard(
                SqlText.trim(rs.getString("lb")),
                SqlText.trim(rs.getString("a1")),
                rs.getInt("a2"),
                SqlText.trim(rs.getString("a3")),
                rs.getInt("a4"),
                SqlText.trim(rs.getString("a5")),
                rs.getInt("a6"),
                SqlText.trim(rs.getString("a7")),
                rs.getInt("a8"),
                SqlText.trim(rs.getString("a9")),
                rs.getInt("a10")));
    }

    int organizationEducationCategory(String organizationCode) {
        Integer value = jdbcTemplate.query("""
                SELECT jxlb
                FROM dwbm
                WHERE dwbm = :organizationCode
                LIMIT 1
                """, new MapSqlParameterSource("organizationCode", organizationCode),
                rs -> rs.next() ? rs.getObject(1, Integer.class) : null);
        return value == null ? 0 : value;
    }

    Optional<RetirementAllowanceStandardRow> findRetirementAllowanceStandard(
            String standardYearMonth,
            String positionCode,
            String retirementCategory,
            int educationCategory) {
        int leaveType = resolveLeaveType(retirementCategory);
        if (leaveType <= 0 || emptyToNull(standardYearMonth) == null || emptyToNull(positionCode) == null) {
            return Optional.empty();
        }
        return jdbcTemplate.query("""
                SELECT blfb2, zwjt, dfbt2, gwjt2, jzmcbt, sdbt, qtbt
                FROM jbtbz06
                WHERE tbnd = :standardYearMonth
                  AND bm = :positionCode
                  AND ltlb = :leaveType
                  AND jxlb = :educationCategory
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", standardYearMonth.trim())
                .addValue("positionCode", positionCode.trim())
                .addValue("leaveType", leaveType)
                .addValue("educationCategory", educationCategory), ALLOWANCE_ROW_MAPPER).stream().findFirst()
                .or(() -> jdbcTemplate.query("""
                        SELECT blfb2, zwjt, dfbt2, gwjt2, jzmcbt, sdbt, qtbt
                        FROM jbtbz06
                        WHERE tbnd = :standardYearMonth
                          AND bm = :positionCode
                          AND ltlb = :leaveType
                        ORDER BY jxlb
                        LIMIT 1
                        """, new MapSqlParameterSource()
                        .addValue("standardYearMonth", standardYearMonth.trim())
                        .addValue("positionCode", positionCode.trim())
                        .addValue("leaveType", leaveType), ALLOWANCE_ROW_MAPPER).stream().findFirst());
    }

    Optional<RetirementAllowanceStandardRow> findActiveAllowanceStandard(
            String standardYearMonth,
            String positionCode,
            int educationCategory) {
        if (emptyToNull(standardYearMonth) == null || emptyToNull(positionCode) == null) {
            return Optional.empty();
        }
        return jdbcTemplate.query("""
                SELECT blfb2, zwjt, dfbt2, gwjt2, jzmcbt, sdbt, qtbt
                FROM zzjbtbz06
                WHERE tbnd = :standardYearMonth
                  AND bm = :positionCode
                  AND jxlb = :educationCategory
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("standardYearMonth", standardYearMonth.trim())
                .addValue("positionCode", positionCode.trim())
                .addValue("educationCategory", educationCategory), ALLOWANCE_ROW_MAPPER).stream().findFirst()
                .or(() -> jdbcTemplate.query("""
                        SELECT blfb2, zwjt, dfbt2, gwjt2, jzmcbt, sdbt, qtbt
                        FROM zzjbtbz06
                        WHERE tbnd = :standardYearMonth
                          AND bm = :positionCode
                        ORDER BY jxlb
                        LIMIT 1
                        """, new MapSqlParameterSource()
                        .addValue("standardYearMonth", standardYearMonth.trim())
                        .addValue("positionCode", positionCode.trim()), ALLOWANCE_ROW_MAPPER).stream().findFirst());
    }

    private int resolveLeaveType(String retirementCategory) {
        String value = trim(retirementCategory);
        if ("离休".equals(value)) {
            return 1;
        }
        if ("退职".equals(value)) {
            return 3;
        }
        if ("退休".equals(value) || value.isBlank()) {
            return 2;
        }
        return 2;
    }

    private static final RowMapper<RetirementAllowanceStandardRow> ALLOWANCE_ROW_MAPPER = (rs, rowNum) ->
            new RetirementAllowanceStandardRow(
                    rs.getInt("blfb2"),
                    rs.getInt("zwjt"),
                    rs.getInt("dfbt2"),
                    rs.getInt("gwjt2"),
                    rs.getInt("jzmcbt"),
                    rs.getInt("sdbt"),
                    rs.getInt("qtbt"));

    List<RetirementApprovalDetailRow> findApprovalDetailsByIds(OrganizationScope scope, List<Integer> ids) {
        if (scope.noneScope() || ids == null || ids.isEmpty()) {
            return List.of();
        }
        return jdbcTemplate.query("""
                SELECT r.id, r.dwbm, dw.dwmc, r.grbm, r.xm, r.xb, r.sfzh, r.mz, r.csny, r.cjgzny, r.gznx, r.zgxl,
                       r.tc, r.txsj, r.ltyy, r.gwfl, r.bbz, r.zjbl, r.dynkh, r.denkh,
                       COALESCE(NULLIF(TRIM(r.zwbm2), ''), NULLIF(TRIM(r.zwbm1), '')) AS position_code,
                       COALESCE(NULLIF(TRIM(r.zwgw2), ''), NULLIF(TRIM(r.zwgw1), '')) AS position_name,
                       NULLIF(TRIM(r.zwgw1), '') AS before_position_name,
                       COALESCE(NULLIF(TRIM(r.jbgzjb2), ''), NULLIF(TRIM(r.jbgzjb1), '')) AS grade_level,
                       COALESCE(NULLIF(TRIM(r.zwgzdc2), ''), NULLIF(TRIM(r.zwgzdc1), '')) AS grade_step,
                       COALESCE(r.zwgzse1, 0) AS before_position_salary,
                       COALESCE(r.jbgzse1, 0) AS before_grade_salary,
                       COALESCE(r.jsdjgz1, 0) AS before_technical_salary,
                       COALESCE(r.jsfszwtg1, 0) AS before_teaching_raise,
                       COALESCE(r.jxjt1, 0) AS before_rank_allowance,
                       COALESCE(r.blfb1, 0) AS before_retained_allowance,
                       COALESCE(r.jjjy1, 0) AS before_bonus_balance,
                       COALESCE(r.zwjt1, 0) AS before_job_allowance,
                       COALESCE(r.dfbt1, 0) AS before_local_allowance,
                       COALESCE(r.gwjt1, 0) AS before_post_allowance,
                       COALESCE(r.fdgz1, 0) + COALESCE(r.sdbt1, 0)
                         + COALESCE(r.jzmcbt1, 0) + COALESCE(r.qtbt1, 0) AS before_other,
                       COALESCE(r.hj1, 0) AS before_total,
                       COALESCE(r.zwgzse2, 0) AS after_position_salary,
                       COALESCE(r.jbgzse2, 0) AS after_grade_salary,
                       COALESCE(r.jsdjgz2, 0) AS after_technical_salary,
                       COALESCE(r.jsfszwtg2, 0) AS after_teaching_raise,
                       COALESCE(r.jxjt, 0) AS after_rank_allowance,
                       COALESCE(r.blfb2, 0) AS after_retained_allowance,
                       COALESCE(r.dfbt2, 0) AS after_local_allowance,
                       COALESCE(r.gwjt2, 0) AS after_post_allowance,
                       COALESCE(r.jbldxf, 0) AS after_converted_base,
                       COALESCE(r.fdgz2, 0) + COALESCE(r.jjjy2, 0) + COALESCE(r.sdbt, 0)
                         + COALESCE(r.jzmcbt, 0) + COALESCE(r.zwjt, 0) + COALESCE(r.qtbt, 0) AS after_other,
                       COALESCE(r.hj2, 0) AS after_total,
                       COALESCE(r.jhljt, 0) AS teaching_age_allowance,
                       COALESCE(r.sdbt, 0) AS living_allowance,
                       COALESCE(r.lczjldxf, 0) AS cumulative_increase
                FROM ryjbxxb r
                LEFT JOIN dwbm dw ON dw.dwbm = r.dwbm
                WHERE (:allOrganizations = TRUE OR r.dwbm IN (:organizationCodes))
                  AND r.id IN (:ids)
                ORDER BY r.dwbm, r.grbm
                """, new MapSqlParameterSource()
                .addValue("allOrganizations", scope.all())
                .addValue("organizationCodes", scope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : scope.organizationCodes())
                .addValue("ids", ids),
                (rs, rowNum) -> new RetirementApprovalDetailRow(
                        rs.getInt("id"),
                        SqlText.trim(rs.getString("dwbm")),
                        SqlText.trim(rs.getString("dwmc")),
                        SqlText.trim(rs.getString("grbm")),
                        SqlText.trim(rs.getString("xm")),
                        SqlText.trim(rs.getString("xb")),
                        SqlText.trim(rs.getString("sfzh")),
                        SqlText.trim(rs.getString("mz")),
                        SqlText.trim(rs.getString("csny")),
                        SqlText.trim(rs.getString("cjgzny")),
                        rs.getInt("gznx"),
                        SqlText.trim(rs.getString("zgxl")),
                        SqlText.trim(rs.getString("tc")),
                        SqlText.trim(rs.getString("txsj")),
                        SqlText.trim(rs.getString("ltyy")),
                        SqlText.trim(rs.getString("gwfl")),
                        SqlText.trim(rs.getString("bbz")),
                        SqlText.trim(rs.getString("dynkh")),
                        SqlText.trim(rs.getString("denkh")),
                        rs.getInt("zjbl"),
                        SqlText.trim(rs.getString("position_code")),
                        SqlText.trim(rs.getString("position_name")),
                        SqlText.trim(rs.getString("before_position_name")),
                        SqlText.trim(rs.getString("grade_level")),
                        SqlText.trim(rs.getString("grade_step")),
                        rs.getInt("before_position_salary"),
                        rs.getInt("before_grade_salary"),
                        rs.getInt("before_technical_salary"),
                        rs.getInt("before_teaching_raise"),
                        rs.getInt("before_rank_allowance"),
                        rs.getInt("before_retained_allowance"),
                        rs.getInt("before_bonus_balance"),
                        rs.getInt("before_job_allowance"),
                        rs.getInt("before_local_allowance"),
                        rs.getInt("before_post_allowance"),
                        rs.getInt("before_other"),
                        rs.getInt("before_total"),
                        rs.getInt("after_position_salary"),
                        rs.getInt("after_grade_salary"),
                        rs.getInt("after_technical_salary"),
                        rs.getInt("after_teaching_raise"),
                        rs.getInt("after_rank_allowance"),
                        rs.getInt("after_retained_allowance"),
                        rs.getInt("after_local_allowance"),
                        rs.getInt("after_post_allowance"),
                        rs.getInt("after_converted_base"),
                        rs.getInt("after_other"),
                        rs.getInt("after_total"),
                        rs.getInt("teaching_age_allowance"),
                        rs.getInt("living_allowance"),
                        rs.getInt("cumulative_increase")));
    }

    private String retireeWhereClause(boolean postReformOnly) {
        return """
                (:allOrganizations = TRUE OR r.dwbm IN (:organizationCodes))
                  AND (
                        :organizationCode IS NULL
                     OR (:includeDescendants = TRUE AND r.dwbm LIKE :organizationCodeLike)
                     OR (:includeDescendants = FALSE AND r.dwbm = :organizationCode)
                  )
                  AND (:pendingOnly = FALSE OR TRIM(COALESCE(r.bbz, '')) = '待办退休')
                  AND (:keyword IS NULL
                       OR r.grbm LIKE :keywordLike
                       OR r.xm LIKE :keywordLike
                       OR r.sfzh LIKE :keywordLike
                       OR r.zwbm2 LIKE :keywordLike
                       OR r.zwgw2 LIKE :keywordLike
                       OR r.jkgrbm LIKE :keywordLike)
                """ + (postReformOnly
                ? " AND REPLACE(COALESCE(NULLIF(TRIM(r.txsj), ''), '000000'), '.', '') > '200607'\n"
                : "");
    }

    private MapSqlParameterSource retireeParameters(
            OrganizationScope scope,
            String organizationCode,
            String keyword,
            boolean includeDescendants,
            boolean pendingOnly) {
        String trimmedOrganization = emptyToNull(organizationCode);
        return scopedParameters(scope, trimmedOrganization)
                .addValue("organizationCodeLike", trimmedOrganization == null ? null : trimmedOrganization + "%")
                .addValue("includeDescendants", includeDescendants && trimmedOrganization != null)
                .addValue("pendingOnly", pendingOnly)
                .addValue("keyword", emptyToNull(keyword))
                .addValue("keywordLike", emptyToNull(keyword) == null ? null : "%" + keyword.trim() + "%");
    }

    private String resolveRatioCategory(String postCategory, String retirementCategory) {
        if ("退职".equals(trim(retirementCategory))) {
            return "退职";
        }
        if ("行政管理人员".equals(trim(postCategory))) {
            return "行政管理人员";
        }
        return "其它";
    }

    private int parseYearBandUpper(String range) {
        if (range == null || range.isBlank()) {
            return 99;
        }
        int dash = range.indexOf('-');
        String upper = dash >= 0 ? range.substring(dash + 1) : range;
        try {
            int value = Integer.parseInt(upper.replaceAll("\\D", ""));
            return value <= 0 ? 99 : value;
        } catch (NumberFormatException ex) {
            return 99;
        }
    }

    private MapSqlParameterSource scopedParameters(OrganizationScope scope, String organizationCode) {
        return new MapSqlParameterSource()
                .addValue("allOrganizations", scope.all())
                .addValue("organizationCodes", scope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : scope.organizationCodes())
                .addValue("organizationCode", organizationCode);
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    record RetirementSeedRow(
            int uid,
            String organizationCode,
            String organizationName,
            String personCode,
            String name,
            String idCard,
            String gender,
            String birthYearMonth,
            String organizationType,
            String personnelCategory,
            String postCategory,
            String workStartYearMonth,
            int interruptedYears,
            int salaryYears,
            String education,
            int educationYears,
            String nation,
            String approvalOrganization,
            String stepAssessmentYear,
            String levelAssessmentYear,
            String interruptedNote,
            String interruptedMonths,
            int raisePercentage,
            String teachingStartYearMonth,
            int teachingInterruptedYears,
            String positionCode,
            String positionName,
            String gradeStep,
            String gradeLevel,
            String gradeStepExtra,
            String salaryStandardYearMonth,
            String allowanceStandardYearMonth,
            int positionSalary,
            int gradeSalary,
            int technicalSalary,
            int teachingRaise,
            int rankAllowance,
            int retainedAllowance,
            int localAllowance,
            int postAllowance,
            int floatingSalary,
            int bonusBalance,
            int livingAllowance,
            int specialPostAllowance,
            int positionAllowance,
            int otherAllowance,
            int retainedSpecial,
            int teachingAllowance,
            int currentTotal) {
    }

    record RetirementSeedInsert(MapSqlParameterSource parameters) {
    }

    record RetirementAllowanceStandardRow(
            int retainedAllowance,
            int positionAllowance,
            int localAllowance,
            int postAllowance,
            int specialPostAllowance,
            int livingAllowance,
            int otherAllowance) {
    }

    record RetirementRetireeDetailRow(
            int id,
            String organizationCode,
            String organizationName,
            String personCode,
            String name,
            String gender,
            String idCard,
            String nation,
            String birthYearMonth,
            String workStartYearMonth,
            int interruptedYears,
            int salaryYears,
            String education,
            String retirementCategory,
            String retirementDate,
            String retirementReason,
            String postCategory,
            String positionCode,
            String positionName,
            String gradeLevel,
            String gradeStep,
            String assessmentStartYear,
            String salaryStandardYearMonth,
            String allowanceStandardYearMonth,
            String approvalOrganization,
            String approvalStatus,
            String approvalDocumentNumber,
            String interruptedNote,
            String interruptedMonths,
            String increaseReason,
            int teachingRaisePercentage,
            int teachingYears,
            int increaseRatio,
            int positionSalary,
            int gradeSalary,
            int technicalSalary,
            int teachingRaise,
            int rankAllowance,
            int beforePositionSalary,
            int beforeGradeSalary,
            int beforeTechnicalSalary,
            int beforeTeachingRaise,
            int beforeRankAllowance,
            int basicRetirementFee,
            int cumulativeIncrease,
            int totalAmount,
            int beforeTotal,
            int beforeRetainedAllowance,
            int beforeLocalAllowance,
            int beforePostAllowance,
            int beforeFloatingSalary,
            int beforeBonusBalance,
            int beforeLivingAllowance,
            int beforeSpecialPostAllowance,
            int beforePositionAllowance,
            int beforeOtherAllowance,
            int retainedAllowance,
            int localAllowance,
            int postAllowance,
            int floatingSalary,
            int bonusBalance,
            int livingAllowance,
            int specialPostAllowance,
            int positionAllowance,
            int otherAllowance,
            String bankAccount,
            String sourceOrganizationCode,
            String sourcePersonCode) {
    }

    record RetirementApprovalDetailRow(
            int id,
            String organizationCode,
            String organizationName,
            String personCode,
            String name,
            String gender,
            String idCard,
            String nation,
            String birthYearMonth,
            String workStartYearMonth,
            int salaryYears,
            String education,
            String retirementCategory,
            String retirementDate,
            String retirementReason,
            String postCategory,
            String approvalStatus,
            String interruptedNote,
            String interruptedMonths,
            int increaseRatio,
            String positionCode,
            String positionName,
            String beforePositionName,
            String gradeLevel,
            String gradeStep,
            int beforePositionSalary,
            int beforeGradeSalary,
            int beforeTechnicalSalary,
            int beforeTeachingRaise,
            int beforeRankAllowance,
            int beforeRetainedAllowance,
            int beforeBonusBalance,
            int beforeJobAllowance,
            int beforeLocalAllowance,
            int beforePostAllowance,
            int beforeOther,
            int beforeTotal,
            int afterPositionSalary,
            int afterGradeSalary,
            int afterTechnicalSalary,
            int afterTeachingRaise,
            int afterRankAllowance,
            int afterRetainedAllowance,
            int afterLocalAllowance,
            int afterPostAllowance,
            int afterConvertedBase,
            int afterOther,
            int afterTotal,
            int teachingAgeAllowance,
            int livingAllowance,
            int cumulativeIncrease) {
    }
}
