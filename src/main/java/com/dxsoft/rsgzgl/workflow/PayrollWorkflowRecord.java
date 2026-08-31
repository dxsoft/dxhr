package com.dxsoft.rsgzgl.workflow;

import java.time.LocalDateTime;

public record PayrollWorkflowRecord(
        long id,
        String workflowNo,
        int uid,
        String organizationCode,
        String personCode,
        String personName,
        String sourceType,
        Integer sourceId,
        String payrollModule,
        String expectedJslb,
        String status,
        LocalDateTime personnelApprovedAt,
        LocalDateTime payrollCompletedAt,
        String payrollHistoryId,
        String createdBy,
        String completedBy,
        String summary) {
}
