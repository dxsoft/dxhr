package com.dxsoft.rsgzgl.personnel;

import java.util.List;

public record MissingAssessmentPreview(
        List<String> years,
        String defaultResult,
        String startYear,
        String targetYear
) {
}
