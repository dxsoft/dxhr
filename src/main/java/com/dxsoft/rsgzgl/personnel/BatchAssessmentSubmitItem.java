package com.dxsoft.rsgzgl.personnel;

public record BatchAssessmentSubmitItem(
        Integer uid,
        Integer assessmentId,
        String organizationCode,
        String personCode
) {
}
