package com.dxsoft.rsgzgl.ops.license;

import java.math.BigDecimal;

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
