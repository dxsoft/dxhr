package com.dxsoft.rsgzgl.license;

import java.math.BigDecimal;

/**
 * 本地工资政策参数（cyxx 中除签约主体身份外的配置项），随授权包签发与导入。
 */
public record LicenseLocalPolicy(
        Integer activeStaffFlag,
        String approvalFlag,
        String payrollTitle,
        String roundingMode,
        String roundToInteger,
        String policeAllowanceCaption,
        String subsidyCaption,
        String approvalMode,
        String unitApprovalCategory,
        BigDecimal policeRankStartLevel,
        String retiredGradeStep,
        BigDecimal internSalaryMode,
        BigDecimal bonusBalanceMode,
        BigDecimal floatingSalaryMode,
        BigDecimal payGradeRetentionMode,
        String backupPath,
        String positionChangeIncludeTechnicalGrade,
        String rankChangeIncludeTechnicalGrade,
        BigDecimal autoBackup,
        BigDecimal confirmBeforeAction,
        BigDecimal checkUpdate
) {
}
