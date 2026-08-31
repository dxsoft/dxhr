package com.dxsoft.rsgzgl.personnel;

public record AnnualAssessmentSummaryRecord(
        String year,
        String organizationCode,
        String organizationName,
        String result,
        Long personnelCount
) {
}
