package com.dxsoft.rsgzgl.personnel;

public record AnnualAssessmentRecord(
        Integer id,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String year,
        String result
) {
}
