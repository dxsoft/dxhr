package com.dxsoft.rsgzgl.report.export;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReportPdfEngineProperties {

    private final String approvalPdfEngine;

    public ReportPdfEngineProperties(
            @Value("${rsgzgl.report.approval-pdf-engine:openpdf}") String approvalPdfEngine) {
        this.approvalPdfEngine = approvalPdfEngine;
    }

    public boolean useOpenPdf() {
        String value = approvalPdfEngine == null ? "" : approvalPdfEngine.trim();
        return !"html".equalsIgnoreCase(value);
    }

    public String normalizedEngine() {
        return useOpenPdf() ? "openpdf" : "html";
    }
}
