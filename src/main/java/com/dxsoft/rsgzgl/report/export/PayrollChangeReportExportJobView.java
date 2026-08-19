package com.dxsoft.rsgzgl.report.export;

import java.time.Instant;

public record PayrollChangeReportExportJobView(
        String jobId,
        String accessToken,
        PayrollChangeReportExportTarget target,
        PayrollChangeReportExportJobStatus status,
        String fileName,
        String contentType,
        int recordCount,
        String errorMessage,
        Instant createdAt,
        Instant completedAt) {
}
