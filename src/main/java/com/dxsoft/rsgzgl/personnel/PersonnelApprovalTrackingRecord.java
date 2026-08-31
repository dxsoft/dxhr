package com.dxsoft.rsgzgl.personnel;

import java.time.LocalDateTime;

public record PersonnelApprovalTrackingRecord(
        int uid,
        String recordType,
        int recordId,
        String organizationCode,
        String organizationName,
        String personCode,
        String personName,
        String summary,
        String positionName,
        String effectiveYearMonth,
        int attachmentCount,
        String approvalStatus,
        String auditTargetType,
        String auditTargetId,
        String submittedBy,
        LocalDateTime submittedAt,
        String approvedBy,
        LocalDateTime approvedAt) {
}
