package com.dxsoft.rsgzgl.personnel;

public record BatchAssessmentSubmitFailure(
        String personCode,
        String name,
        String message
) {
}
