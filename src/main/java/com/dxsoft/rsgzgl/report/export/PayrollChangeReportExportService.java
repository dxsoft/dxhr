package com.dxsoft.rsgzgl.report.export;

import com.dxsoft.rsgzgl.payroll.PayrollChangeComparison;
import com.dxsoft.rsgzgl.payroll.PayrollService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class PayrollChangeReportExportService {

    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final PayrollService payrollService;
    private final PayrollChangeReportLayoutService layoutService;
    private final PayrollChangeApprovalHtmlRenderer approvalHtmlRenderer;
    private final PayrollChangeRegisterHtmlRenderer registerHtmlRenderer;
    private final ReportPdfService pdfService;
    private final PayrollChangeExcelExportService excelExportService;

    PayrollChangeReportExportService(
            PayrollService payrollService,
            PayrollChangeReportLayoutService layoutService,
            PayrollChangeApprovalHtmlRenderer approvalHtmlRenderer,
            PayrollChangeRegisterHtmlRenderer registerHtmlRenderer,
            ReportPdfService pdfService,
            PayrollChangeExcelExportService excelExportService) {
        this.payrollService = payrollService;
        this.layoutService = layoutService;
        this.approvalHtmlRenderer = approvalHtmlRenderer;
        this.registerHtmlRenderer = registerHtmlRenderer;
        this.pdfService = pdfService;
        this.excelExportService = excelExportService;
    }

    public ResponseEntity<byte[]> downloadApprovalPdf(PayrollChangeReportExportRequest request) {
        PayrollChangeReportArtifact artifact = exportArtifact(PayrollChangeReportExportTarget.APPROVAL_PDF, request);
        return fileResponse(artifact.bytes(), artifact.contentType(), artifact.fileName());
    }

    public ResponseEntity<byte[]> downloadApprovalExcel(PayrollChangeReportExportRequest request) {
        PayrollChangeReportArtifact artifact = exportArtifact(PayrollChangeReportExportTarget.APPROVAL_EXCEL, request);
        return fileResponse(artifact.bytes(), artifact.contentType(), artifact.fileName());
    }

    public ResponseEntity<byte[]> downloadRegisterPdf(PayrollChangeReportExportRequest request) {
        PayrollChangeReportArtifact artifact = exportArtifact(PayrollChangeReportExportTarget.REGISTER_PDF, request);
        return fileResponse(artifact.bytes(), artifact.contentType(), artifact.fileName());
    }

    public ResponseEntity<byte[]> downloadRegisterExcel(PayrollChangeReportExportRequest request) {
        PayrollChangeReportArtifact artifact = exportArtifact(PayrollChangeReportExportTarget.REGISTER_EXCEL, request);
        return fileResponse(artifact.bytes(), artifact.contentType(), artifact.fileName());
    }

    public PayrollChangeReportArtifact exportArtifact(
            PayrollChangeReportExportTarget target,
            PayrollChangeReportExportRequest request) {
        PayrollChangeReportBundle bundle = buildBundle(request);
        return switch (target) {
            case APPROVAL_PDF -> artifact(
                    pdfService.renderPdf(approvalHtmlRenderer.renderDocument(bundle.approvalSheets())),
                    target);
            case APPROVAL_EXCEL -> artifact(
                    excelExportService.exportApprovals(bundle.approvalSheets()),
                    target);
            case REGISTER_PDF -> artifact(
                    pdfService.renderPdf(registerHtmlRenderer.renderDocument(bundle.registerPages())),
                    target);
            case REGISTER_EXCEL -> artifact(
                    excelExportService.exportRegister(bundle.registerPages()),
                    target);
        };
    }

    public String previewApprovalHtml(PayrollChangeReportExportRequest request) {
        PayrollChangeReportBundle bundle = buildBundle(request);
        return approvalHtmlRenderer.renderPreviewBody(bundle.approvalSheets());
    }

    public String previewRegisterHtml(PayrollChangeReportExportRequest request) {
        PayrollChangeReportBundle bundle = buildBundle(request);
        return registerHtmlRenderer.renderPreviewBody(bundle.registerPages());
    }

    private PayrollChangeReportBundle buildBundle(PayrollChangeReportExportRequest request) {
        List<PayrollChangeComparison> comparisons = loadReports(request);
        String reportTitle = request.reportTitle();
        Boolean institutionOverride = request.institution();
        return new PayrollChangeReportBundle(
                comparisons,
                layoutService.buildApprovalSheets(comparisons, reportTitle, institutionOverride),
                layoutService.buildRegisterPages(comparisons, reportTitle, institutionOverride));
    }

    private List<PayrollChangeComparison> loadReports(PayrollChangeReportExportRequest request) {
        List<String> ids = request.payrollHistoryIds() == null
                ? List.of()
                : request.payrollHistoryIds().stream().map(String::trim).filter(id -> !id.isEmpty()).toList();
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("请至少选择一条工资变动记录");
        }
        return payrollService.payrollChangeComparisons(ids);
    }

    private PayrollChangeReportArtifact artifact(byte[] bytes, PayrollChangeReportExportTarget target) {
        return new PayrollChangeReportArtifact(bytes, target.contentType(), fileName(target.filePrefix(), target.extension()));
    }

    private ResponseEntity<byte[]> fileResponse(byte[] bytes, String contentType, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    private String fileName(String prefix, String extension) {
        return prefix + "_" + FILE_STAMP.format(LocalDateTime.now()) + "." + extension;
    }
}
