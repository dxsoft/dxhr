package com.dxsoft.rsgzgl.report.export;

import java.util.List;

public record PayrollChangeReportExportRequest(
        List<String> payrollHistoryIds,
        String reportTitle,
        Boolean institution) {
}
