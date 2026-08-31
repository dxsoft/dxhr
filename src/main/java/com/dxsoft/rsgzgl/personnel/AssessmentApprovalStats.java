package com.dxsoft.rsgzgl.personnel;

import java.math.BigDecimal;
import java.util.List;

public record AssessmentApprovalStats(
        String organizationCode,
        String organizationName,
        String year,
        int participantCount,
        int excellentCount,
        BigDecimal excellentRatio,
        int pendingCount,
        int pendingExcellentCount,
        int approvedCount,
        int approvedExcellentCount,
        List<AssessmentResultCountItem> resultCounts
) {
}
