package com.dxsoft.rsgzgl.payroll;

/**
 * 级别晋升办理请求：沿用列表试算结果写库，办理时不再重算考核。
 */
public record LevelPromotionApplyRequest(
        String organizationCode,
        String personCode,
        Boolean reformLevelRollingDue,
        String calculationPeriod,
        String promotedLevel,
        String promotedStep,
        String nextLevelAssessmentStartYear,
        String nextStepAssessmentStartYear,
        Integer promotedGradeSalary,
        Integer increaseAmount
) {
}
