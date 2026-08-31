package com.dxsoft.rsgzgl.report;

public record ReportTypeOption(
        String code,
        String name,
        String title,
        String fileName,
        String reportType,
        String category,
        String printCategory,
        String defaultText
) {
}
