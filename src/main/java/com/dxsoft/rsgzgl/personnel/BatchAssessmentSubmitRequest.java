package com.dxsoft.rsgzgl.personnel;

import java.util.List;

public record BatchAssessmentSubmitRequest(
        String organizationCode,
        String year,
        Boolean includeDescendants,
        List<BatchAssessmentSubmitItem> records
) {
}
