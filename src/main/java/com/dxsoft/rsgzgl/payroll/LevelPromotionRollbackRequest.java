package com.dxsoft.rsgzgl.payroll;

/**
 * 级别晋升还原请求：带单位/人员编码以便按索引定位，避免 TRIM(id) 全表扫描。
 */
public record LevelPromotionRollbackRequest(
        String organizationCode,
        String personCode
) {
}
