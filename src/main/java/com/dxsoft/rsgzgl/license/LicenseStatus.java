package com.dxsoft.rsgzgl.license;

public record LicenseStatus(
        boolean authorized,
        String subjectCode,
        String subjectName,
        String issuedAt,
        String expiresAt,
        String issuer,
        int organizationCount,
        String fingerprint,
        String message,
        Boolean ukeyEnabled,
        Boolean ukeyRequired
) {
}
