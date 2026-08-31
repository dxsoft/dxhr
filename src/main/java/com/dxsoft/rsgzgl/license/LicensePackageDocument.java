package com.dxsoft.rsgzgl.license;

import java.util.List;

public record LicensePackageDocument(
        String format,
        String issuedAt,
        String expiresAt,
        String issuer,
        LicenseSubject subject,
        List<LicenseOrganization> organizations,
        /** null = 旧包未携带本地政策，导入时仅更新签约主体字段 */
        LicenseLocalPolicy localPolicy,
        /** null = 旧包未声明，运行时回退环境变量 */
        Boolean ukeyEnabled,
        /** null = 旧包未声明，运行时回退环境变量 */
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
