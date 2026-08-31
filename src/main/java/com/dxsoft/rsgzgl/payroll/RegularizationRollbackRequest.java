package com.dxsoft.rsgzgl.payroll;

/**
 * 转正定级还原请求：带单位/人员编码以便按索引定位。
 */
public record RegularizationRollbackRequest(
        String organizationCode,
        String personCode
) {
}
