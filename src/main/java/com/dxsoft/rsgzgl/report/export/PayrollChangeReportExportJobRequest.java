package com.dxsoft.rsgzgl.report.export;

public record PayrollChangeReportExportJobRequest(
        PayrollChangeReportExportTarget target,
        PayrollChangeReportExportRequest exportRequest) {
}
