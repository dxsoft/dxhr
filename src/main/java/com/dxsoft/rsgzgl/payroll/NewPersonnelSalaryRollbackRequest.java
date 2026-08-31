package com.dxsoft.rsgzgl.payroll;

/**
 * 新进定资还原请求：带单位/人员编码以便按索引定位，避免 TRIM(id) 全表扫描。
 */
public record NewPersonnelSalaryRollbackRequest(
        String organizationCode,
        String personCode
) {
}
