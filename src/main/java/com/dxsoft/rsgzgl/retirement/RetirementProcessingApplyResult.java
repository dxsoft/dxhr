package com.dxsoft.rsgzgl.retirement;

public record RetirementProcessingApplyResult(
        Integer uid,
        String organizationCode,
        String sourcePersonCode,
        String retireePersonCode,
        String name,
        String retirementDate,
        Integer retireeId,
        Integer estimatedTotal,
        String message) {
}
