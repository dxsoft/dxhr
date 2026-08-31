package com.dxsoft.rsgzgl.ops.license;

import java.util.List;

public record LicensePackageDocument(
        String format,
        String issuedAt,
        String expiresAt,
        String issuer,
        LicenseSubject subject,
        List<LicenseOrganization> organizations,
        LicenseLocalPolicy localPolicy,
        /** null = 旧包未声明 */
        Boolean ukeyEnabled,
        /** null = 旧包未声明 */
        Boolean ukeyRequired,
        String signature
) {
    public static final String FORMAT = "RSGZGL_LICENSE_V1";

    public LicensePackageDocument withSignature(String newSignature) {
        return new LicensePackageDocument(
                format, issuedAt, expiresAt, issuer, subject, organizations, localPolicy,
                ukeyEnabled, ukeyRequired, newSignature);
    }
}
