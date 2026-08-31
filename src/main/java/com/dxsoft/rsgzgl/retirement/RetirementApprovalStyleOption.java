package com.dxsoft.rsgzgl.retirement;

public record RetirementApprovalStyleOption(
        String code,
        String label,
        String agencyTemplate,
        String institutionTemplate,
        boolean defaultSelected) {
}
