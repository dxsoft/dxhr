package com.dxsoft.rsgzgl.personnel;

public record BatchAssessmentEntryRow(
        Integer uid,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String personnelCategory,
        String organizationType,
        String currentPosition,
        String year,
        Integer assessmentId,
        String result,
        String approvalStatus,
        String defaultResult
) {
}
