package com.dxsoft.rsgzgl.personnel;

import java.util.List;

public record BatchAssessmentPreview(
        String organizationCode,
        String organizationName,
        String year,
        int totalPersonnel,
        int enteredCount,
        int missingCount,
        List<BatchAssessmentEntryRow> rows
) {
}
