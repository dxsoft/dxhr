package com.dxsoft.rsgzgl.personnel;

public record BatchAssessmentSaveFailure(
        String personCode,
        String name,
        String message
) {
}
