package com.dxsoft.rsgzgl.personnel;

import java.util.List;

public record BatchAssessmentSaveResult(
        int inserted,
        int updated,
        int skipped,
        List<BatchAssessmentSaveFailure> failures
) {
}
