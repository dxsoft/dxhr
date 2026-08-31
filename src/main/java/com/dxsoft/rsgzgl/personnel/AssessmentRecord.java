package com.dxsoft.rsgzgl.personnel;

import java.time.LocalDateTime;

public record AssessmentRecord(
        Integer id,
        String organizationCode,
        String personCode,
        String year,
        String result,
        String approvalStatus,
        Boolean appCreated,
        String submittedBy,
        LocalDateTime submittedAt,
        String approvedBy,
        LocalDateTime approvedAt,
        int attachmentCount
) {
}
