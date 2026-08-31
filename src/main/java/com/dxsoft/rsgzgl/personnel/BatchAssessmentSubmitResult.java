package com.dxsoft.rsgzgl.personnel;

import java.util.List;

public record BatchAssessmentSubmitResult(
        int submitted,
        int skipped,
        List<BatchAssessmentSubmitFailure> failures
) {
}
