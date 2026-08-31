package com.dxsoft.rsgzgl.systemconfig;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.SqlText;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class SystemConfigRepository {

    private static final RowMapper<LocalPolicyConfig> LOCAL_POLICY_MAPPER = (rs, rowNum) -> new LocalPolicyConfig(
            rs.getInt("ID"),
            SqlText.trim(rs.getString("dwbm")),
            SqlText.trim(rs.getString("dwmc")),
            SqlText.trim(rs.getString("dwjc")),
            SqlText.trim(rs.getString("zgry")),
            SqlText.trim(rs.getString("szds")),
            rs.getObject("zzrs", Integer.class),
            SqlText.trim(rs.getString("skbz")),
            SqlText.trim(rs.getString("shrq")),
            SqlText.trim(rs.getString("bz")),
            SqlText.trim(rs.getString("blxs")),
            SqlText.trim(rs.getString("swyz")),
            SqlText.trim(rs.getString("jzmcbtmc")),
            SqlText.trim(rs.getString("sdbtmc")),
            SqlText.trim(rs.getString("spfs")),
            SqlText.trim(rs.getString("dwsplb")),
            rs.getBigDecimal("jqdm"),
            SqlText.trim(rs.getString("ltjddc")),
            rs.getBigDecimal("jxgz"),
            rs.getBigDecimal("jjjy"),
            rs.getBigDecimal("fdgz"),
            rs.getBigDecimal("pgbc"),
            SqlText.trim(rs.getString("softsn")),
            rs.getBoolean("sp"),
            SqlText.trim(rs.getString("path_bak")),
            SqlText.trim(rs.getString("zwbhhjsdj")),
            SqlText.trim(rs.getString("cdchjsdj")),
            rs.getBigDecimal("autobak"),
            rs.getBigDecimal("ask"),
            rs.getBigDecimal("chkupdate"));

    private static final RowMapper<SystemOptionConfig> SYSTEM_OPTION_MAPPER = (rs, rowNum) -> new SystemOptionConfig(
            SqlText.trim(rs.getString("qydrstg")),
            SqlText.trim(rs.getString("tgdcxlgl")),
            SqlText.trim(rs.getString("xsws")),
            SqlText.trim(rs.getString("jwbz")),
            SqlText.trim(rs.getString("tgjjjy")),
            SqlText.trim(rs.getString("fdgz")));

    private final NamedParameterJdbcTemplate jdbcTemplate;

    SystemConfigRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    List<LocalPolicyConfig> findLocalPolicies(String keyword, PageRequest pageRequest) {
        MapSqlParameterSource parameters = parameters(keyword)
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());
        return jdbcTemplate.query("""
                SELECT ID, dwbm, dwmc, dwjc, zgry, szds, zzrs, skbz, shrq, bz,
                       blxs, swyz, jzmcbtmc, sdbtmc, spfs, dwsplb, jqdm, ltjddc,
                       jxgz, jjjy, fdgz, pgbc, softsn, sp, path_bak,
                       zwbhhjsdj, cdchjsdj, autobak, ask, chkupdate
                FROM cyxx
                WHERE (:keyword IS NULL OR dwbm LIKE :keywordLike OR dwmc LIKE :keywordLike OR szds LIKE :keywordLike)
                ORDER BY ID
                LIMIT :limit OFFSET :offset
                """, parameters, LOCAL_POLICY_MAPPER);
    }

    long countLocalPolicies(String keyword) {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM cyxx
                WHERE (:keyword IS NULL OR dwbm LIKE :keywordLike OR dwmc LIKE :keywordLike OR szds LIKE :keywordLike)
                """, parameters(keyword), Long.class);
        return count == null ? 0 : count;
    }

    List<SystemOptionConfig> findSystemOptions() {
        return jdbcTemplate.query("""
                SELECT qydrstg, tgdcxlgl, xsws, jwbz, tgjjjy, fdgz
                FROM xtcs
                """, SYSTEM_OPTION_MAPPER);
    }

    void updateSystemOptions(SystemOptionUpdateRequest request) {
        jdbcTemplate.update("""
                UPDATE xtcs
                SET qydrstg = :enterpriseTransferRaise,
                    tgdcxlgl = :gradeStepEducationLink,
                    xsws = :decimalPlaces,
                    jwbz = :policeRankAllowance,
                    tgjjjy = :reformBonusBalance,
                    fdgz = :floatingSalary
                """, new MapSqlParameterSource()
                .addValue("enterpriseTransferRaise", request.enterpriseTransferRaise())
                .addValue("gradeStepEducationLink", request.gradeStepEducationLink())
                .addValue("decimalPlaces", request.decimalPlaces())
                .addValue("policeRankAllowance", request.policeRankAllowance())
                .addValue("reformBonusBalance", request.reformBonusBalance())
                .addValue("floatingSalary", request.floatingSalary()));
    }

    boolean localPolicyExists(int id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cyxx WHERE ID = :id",
                new MapSqlParameterSource("id", id),
                Integer.class);
        return count != null && count > 0;
    }

    void updateLocalPolicy(int id, LocalPolicyUpdateRequest request) {
        jdbcTemplate.update("""
                UPDATE cyxx
                SET dwbm = :organizationCode,
                    dwmc = :organizationName,
                    dwjc = :organizationLevel,
                    zgry = :supervisor,
                    szds = :city,
                    zzrs = :activeStaffFlag,
                    skbz = :approvalFlag,
                    shrq = :approvedAt,
                    bz = :payrollTitle,
                    blxs = :roundingMode,
                    swyz = :roundToInteger,
                    jzmcbtmc = :policeAllowanceCaption,
                    sdbtmc = :subsidyCaption,
                    spfs = :approvalMode,
                    dwsplb = :unitApprovalCategory,
                    jqdm = :policeRankStartLevel,
                    ltjddc = :retiredGradeStep,
                    jxgz = :internSalaryMode,
                    jjjy = :bonusBalanceMode,
                    fdgz = :floatingSalaryMode,
                    pgbc = :payGradeRetentionMode,
                    softsn = :softwareSerialNumber,
                    sp = :approvalEnabled,
                    path_bak = :backupPath,
                    zwbhhjsdj = :positionChangeIncludeTechnicalGrade,
                    cdchjsdj = :rankChangeIncludeTechnicalGrade,
                    autobak = :autoBackup,
                    ask = :confirmBeforeAction,
                    chkupdate = :checkUpdate
                WHERE ID = :id
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("organizationCode", request.organizationCode())
                .addValue("organizationName", request.organizationName())
                .addValue("organizationLevel", request.organizationLevel())
                .addValue("supervisor", request.supervisor())
                .addValue("city", request.city())
                .addValue("activeStaffFlag", request.activeStaffFlag())
                .addValue("approvalFlag", request.approvalFlag())
                .addValue("approvedAt", request.approvedAt())
                .addValue("payrollTitle", request.payrollTitle())
                .addValue("roundingMode", request.roundingMode())
                .addValue("roundToInteger", request.roundToInteger())
                .addValue("policeAllowanceCaption", request.policeAllowanceCaption())
                .addValue("subsidyCaption", request.subsidyCaption())
                .addValue("approvalMode", request.approvalMode())
                .addValue("unitApprovalCategory", request.unitApprovalCategory())
                .addValue("policeRankStartLevel", request.policeRankStartLevel())
                .addValue("retiredGradeStep", request.retiredGradeStep())
                .addValue("internSalaryMode", request.internSalaryMode())
                .addValue("bonusBalanceMode", request.bonusBalanceMode())
                .addValue("floatingSalaryMode", request.floatingSalaryMode())
                .addValue("payGradeRetentionMode", request.payGradeRetentionMode())
                .addValue("softwareSerialNumber", request.softwareSerialNumber())
                .addValue("approvalEnabled", request.approvalEnabled() != null && request.approvalEnabled())
                .addValue("backupPath", request.backupPath())
                .addValue("positionChangeIncludeTechnicalGrade", request.positionChangeIncludeTechnicalGrade())
                .addValue("rankChangeIncludeTechnicalGrade", request.rankChangeIncludeTechnicalGrade())
                .addValue("autoBackup", request.autoBackup())
                .addValue("confirmBeforeAction", request.confirmBeforeAction())
                .addValue("checkUpdate", request.checkUpdate()));
    }

    LocalPolicyConfig findLocalPolicyById(int id) {
        List<LocalPolicyConfig> rows = jdbcTemplate.query("""
                SELECT ID, dwbm, dwmc, dwjc, zgry, szds, zzrs, skbz, shrq, bz,
                       blxs, swyz, jzmcbtmc, sdbtmc, spfs, dwsplb, jqdm, ltjddc,
                       jxgz, jjjy, fdgz, pgbc, softsn, sp, path_bak,
                       zwbhhjsdj, cdchjsdj, autobak, ask, chkupdate
                FROM cyxx
                WHERE ID = :id
                LIMIT 1
                """, new MapSqlParameterSource("id", id), LOCAL_POLICY_MAPPER);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private MapSqlParameterSource parameters(String keyword) {
        String trimmedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return new MapSqlParameterSource()
                .addValue("keyword", trimmedKeyword)
                .addValue("keywordLike", trimmedKeyword == null ? null : "%" + trimmedKeyword + "%");
    }
}
