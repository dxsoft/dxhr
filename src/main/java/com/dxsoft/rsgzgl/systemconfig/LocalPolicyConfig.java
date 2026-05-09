package com.dxsoft.rsgzgl.systemconfig;

import java.math.BigDecimal;

public record LocalPolicyConfig(
        Integer id,
        String organizationCode,
        String organizationName,
        String organizationLevel,
        String supervisor,
        String city,
        Integer activeStaffFlag,
        String approvalFlag,
        String approvedAt,
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
        String softwareSerialNumber,
        Boolean approvalEnabled,
        String backupPath,
        String positionChangeIncludeTechnicalGrade,
        String rankChangeIncludeTechnicalGrade,
        BigDecimal autoBackup,
        BigDecimal confirmBeforeAction,
        BigDecimal checkUpdate
) {
}
