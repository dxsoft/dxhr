package com.dxsoft.rsgzgl.personnel;

public record AssessmentRecord(
        Integer id,
        String organizationCode,
        String personCode,
        String year,
        String result,
        Boolean appCreated
) {
}
