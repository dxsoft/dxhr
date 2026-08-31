package com.dxsoft.rsgzgl.license;

import java.util.List;

public record LicenseImportResult(
        String subjectCode,
        String subjectName,
        int organizationsUpserted,
        List<String> organizationCodes,
        String fingerprint,
        String message
) {
}
