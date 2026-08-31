package com.dxsoft.rsgzgl.retirement;

public record RetirementProcessingApplyRequest(
        String retirementDate,
        String retirementCategory,
        String retirementReason,
        String remark) {
}
