package com.dxsoft.rsgzgl.personnel;

import java.time.LocalDateTime;

public record EducationRecord(
        Integer id,
        String organizationCode,
        String personCode,
        String educationCode,
        String educationName,
        String school,
        String enrollmentDate,
        String graduationDate,
        Integer studyYears,
        String educationType,
        String remark,
        String approvalStatus,
        Boolean appCreated,
        String submittedBy,
        LocalDateTime submittedAt,
        String approvedBy,
        LocalDateTime approvedAt,
        int attachmentCount
) {
}
