package com.dxsoft.rsgzgl.payroll;

/**
 * 转正定级办理请求：沿用列表试算结果写库，办理时不再重算学历/标准。
 */
public record RegularizationApplyRequest(
        String organizationCode,
        String personCode,
        String calculationPeriod,
        String regularPositionCode,
        String regularPositionName,
        String regularLevel,
        String regularStep,
        Integer regularPositionSalary,
        Integer regularBaseSalary,
        Integer increaseAmount
) {
}
