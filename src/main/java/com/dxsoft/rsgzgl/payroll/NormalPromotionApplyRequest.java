package com.dxsoft.rsgzgl.payroll;

/**
 * 正常档次/薪级晋升办理请求：沿用列表试算结果写库，办理时不再重算考核。
 */
public record NormalPromotionApplyRequest(
        String organizationCode,
        String personCode,
        String calculationPeriod,
        String promotedGradeOrLevel,
        String gradeSalaryLevel,
        Integer promotedBaseSalary,
        Integer increaseAmount,
        String baseSalarySource
) {
}
