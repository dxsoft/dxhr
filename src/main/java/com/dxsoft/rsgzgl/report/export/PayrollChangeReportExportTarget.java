package com.dxsoft.rsgzgl.report.export;

public enum PayrollChangeReportExportTarget {
    APPROVAL_PDF("payroll_change_approval", "pdf", "application/pdf"),
    APPROVAL_EXCEL("payroll_change_approval", "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    REGISTER_PDF("payroll_change_register", "pdf", "application/pdf"),
    REGISTER_EXCEL("payroll_change_register", "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final String filePrefix;
    private final String extension;
    private final String contentType;

    PayrollChangeReportExportTarget(String filePrefix, String extension, String contentType) {
        this.filePrefix = filePrefix;
        this.extension = extension;
        this.contentType = contentType;
    }

    public String filePrefix() {
        return filePrefix;
    }

    public String extension() {
        return extension;
    }

    public String contentType() {
        return contentType;
    }
}
