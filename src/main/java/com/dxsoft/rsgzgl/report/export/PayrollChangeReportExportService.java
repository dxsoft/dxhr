package com.dxsoft.rsgzgl.report.export;

import com.dxsoft.rsgzgl.payroll.PayrollChangeComparison;
import com.dxsoft.rsgzgl.payroll.PayrollService;
import com.dxsoft.rsgzgl.printauth.PrintAuthorizationService;
import com.dxsoft.rsgzgl.report.export.PayrollChangeReportLayoutService.ApprovalSheetModel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class PayrollChangeReportExportService {

    private static final Logger log = LoggerFactory.getLogger(PayrollChangeReportExportService.class);
    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final int APPROVAL_PDF_CHUNK_SIZE = 48;

    private final PayrollService payrollService;
    private final PayrollChangeReportLayoutService layoutService;
    private final PayrollChangeApprovalHtmlRenderer approvalHtmlRenderer;
    private final PayrollChangeApprovalOpenPdfRenderer approvalOpenPdfRenderer;
    private final PayrollChangeRegisterHtmlRenderer registerHtmlRenderer;
    private final ReportPdfService pdfService;
    private final PayrollChangeExcelExportService excelExportService;
    private final PrintAuthorizationService printAuthorizationService;
    private final ReportPdfEngineProperties pdfEngineProperties;

    PayrollChangeReportExportService(
            PayrollService payrollService,
            PayrollChangeReportLayoutService layoutService,
            PayrollChangeApprovalHtmlRenderer approvalHtmlRenderer,
            PayrollChangeApprovalOpenPdfRenderer approvalOpenPdfRenderer,
            PayrollChangeRegisterHtmlRenderer registerHtmlRenderer,
            ReportPdfService pdfService,
            PayrollChangeExcelExportService excelExportService,
            PrintAuthorizationService printAuthorizationService,
            ReportPdfEngineProperties pdfEngineProperties) {
        this.payrollService = payrollService;
        this.layoutService = layoutService;
        this.approvalHtmlRenderer = approvalHtmlRenderer;
        this.approvalOpenPdfRenderer = approvalOpenPdfRenderer;
        this.registerHtmlRenderer = registerHtmlRenderer;
        this.pdfService = pdfService;
        this.excelExportService = excelExportService;
        this.printAuthorizationService = printAuthorizationService;
        this.pdfEngineProperties = pdfEngineProperties;
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
        long startedAt = System.nanoTime();
        return switch (target) {
            case APPROVAL_PDF -> {
                long loadStartedAt = System.nanoTime();
                var sheets = buildApprovalSheets(request);
                long layoutAt = System.nanoTime();
                String engine = pdfEngineProperties.normalizedEngine();
                byte[] pdf = renderApprovalPdf(sheets);
                log.info(
                        "export APPROVAL_PDF engine={} sheets={} loadLayout={}ms pdf={}ms total={}ms bytes={}",
                        engine,
                        sheets.size(),
                        (layoutAt - loadStartedAt) / 1_000_000L,
                        (System.nanoTime() - layoutAt) / 1_000_000L,
                        (System.nanoTime() - startedAt) / 1_000_000L,
                        pdf.length);
                yield artifact(pdf, target);
            }
            case APPROVAL_EXCEL -> artifact(
                    excelExportService.exportApprovals(buildApprovalSheets(request)),
                    target);
            case REGISTER_PDF -> {
                var pages = buildRegisterPages(request);
                yield artifact(pdfService.renderPdf(registerHtmlRenderer.renderDocument(pages)), target);
            }
            case REGISTER_EXCEL -> artifact(
                    excelExportService.exportRegister(buildRegisterPages(request)),
                    target);
        };
    }

    public String previewApprovalHtml(PayrollChangeReportExportRequest request) {
        List<PayrollChangeComparison> comparisons = loadReports(request, false);
        boolean unauthorized = !printAuthorizationService.isAuthorizedForReports(comparisons);
        return approvalHtmlRenderer.renderPreviewBody(
                layoutService.buildApprovalSheets(comparisons, request.reportTitle(), request.institution()),
                unauthorized);
    }

    public String previewRegisterHtml(PayrollChangeReportExportRequest request) {
        List<PayrollChangeComparison> comparisons = loadReports(request, false);
        boolean unauthorized = !printAuthorizationService.isAuthorizedForReports(comparisons);
        return registerHtmlRenderer.renderPreviewBody(
                layoutService.buildRegisterPages(comparisons, request.reportTitle(), request.institution()),
                unauthorized);
    }

    private byte[] renderApprovalPdf(List<ApprovalSheetModel> sheets) {
        if (pdfEngineProperties.useOpenPdf()) {
            return approvalOpenPdfRenderer.render(sheets);
        }
        if (sheets.size() <= APPROVAL_PDF_CHUNK_SIZE) {
            return pdfService.renderPdf(approvalHtmlRenderer.renderDocument(sheets));
        }
        List<String> htmlChunks = new ArrayList<>();
        for (int from = 0; from < sheets.size(); from += APPROVAL_PDF_CHUNK_SIZE) {
            int to = Math.min(from + APPROVAL_PDF_CHUNK_SIZE, sheets.size());
            htmlChunks.add(approvalHtmlRenderer.renderDocument(sheets.subList(from, to)));
        }
        return pdfService.renderAndMergePdfs(htmlChunks);
    }

    private List<ApprovalSheetModel> buildApprovalSheets(
            PayrollChangeReportExportRequest request) {
        List<PayrollChangeComparison> comparisons = loadReports(request, true);
        return layoutService.buildApprovalSheets(comparisons, request.reportTitle(), request.institution());
    }

    private List<PayrollChangeReportLayoutService.RegisterPageModel> buildRegisterPages(
            PayrollChangeReportExportRequest request) {
        List<PayrollChangeComparison> comparisons = loadReports(request, true);
        return layoutService.buildRegisterPages(comparisons, request.reportTitle(), request.institution());
    }

    private List<PayrollChangeComparison> loadReports(
            PayrollChangeReportExportRequest request,
            boolean requireAuthorization) {
        List<String> ids = request.payrollHistoryIds() == null
                ? List.of()
                : request.payrollHistoryIds().stream().map(String::trim).filter(id -> !id.isEmpty()).toList();
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("请至少选择一条工资变动记录");
        }
        List<PayrollChangeComparison> reports = payrollService.payrollChangeComparisons(ids);
        if (requireAuthorization) {
            printAuthorizationService.assertAllowedForReports(reports);
        }
        return reports;
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
