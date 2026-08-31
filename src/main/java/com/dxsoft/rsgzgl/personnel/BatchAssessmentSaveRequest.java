package com.dxsoft.rsgzgl.personnel;

import java.util.List;

public record BatchAssessmentSaveRequest(
        String organizationCode,
        String year,
        String defaultResult,
        Boolean includeDescendants,
        List<BatchAssessmentRecordItem> records
) {
}
