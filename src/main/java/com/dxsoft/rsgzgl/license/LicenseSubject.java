package com.dxsoft.rsgzgl.license;

public record LicenseSubject(
        String organizationCode,
        String organizationName,
        String organizationLevel,
        String city,
        String supervisor
) {
}
