package com.dxsoft.rsgzgl.report;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.payroll.PayrollChangeComparison;
import com.dxsoft.rsgzgl.personnel.PersonnelInformationCollectionReport;
import com.dxsoft.rsgzgl.report.export.PayrollChangeReportAsyncExportService;
import com.dxsoft.rsgzgl.report.export.PayrollChangeReportExportJobRequest;
import com.dxsoft.rsgzgl.report.export.PayrollChangeReportExportJobView;
import com.dxsoft.rsgzgl.report.export.PayrollChangeReportExportRequest;
import com.dxsoft.rsgzgl.report.export.PayrollChangeReportExportService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
class ReportController {

    private final ReportService reportService;
    private final PayrollChangeReportExportService payrollChangeReportExportService;
    private final PayrollChangeReportAsyncExportService payrollChangeReportAsyncExportService;

    ReportController(
            ReportService reportService,
            PayrollChangeReportExportService payrollChangeReportExportService,
            PayrollChangeReportAsyncExportService payrollChangeReportAsyncExportService) {
        this.reportService = reportService;
        this.payrollChangeReportExportService = payrollChangeReportExportService;
        this.payrollChangeReportAsyncExportService = payrollChangeReportAsyncExportService;
    }

    @GetMapping("/types")
    PageResponse<ReportTypeOption> reportTypes(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return reportService.reportTypes(category, reportType, PageRequest.of(page, size));
    }

    @GetMapping("/payroll-change-candidates")
    PageResponse<PayrollChangeRegisterRow> payrollChangeCandidates(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String reportTypeCode,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return reportService.payrollChangeCandidates(organizationCode, reportTypeCode, year, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/payroll-change-register")
    PageResponse<PayrollChangeRegisterRow> payrollChangeRegister(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return reportService.payrollChangeRegister(organizationCode, period, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/payroll-change-approval")
    PayrollChangeComparison payrollChangeApproval(@RequestParam String payrollHistoryId) {
        return reportService.payrollChangeApproval(payrollHistoryId);
    }

    @PostMapping("/payroll-change-approvals")
    List<PayrollChangeComparison> payrollChangeApprovals(@RequestBody List<String> payrollHistoryIds) {
        return reportService.payrollChangeApprovals(payrollHistoryIds);
    }

    @PostMapping("/payroll-change-approvals/pdf")
    ResponseEntity<byte[]> payrollChangeApprovalsPdf(@RequestBody PayrollChangeReportExportRequest request) {
        return payrollChangeReportExportService.downloadApprovalPdf(request);
    }

    @PostMapping("/payroll-change-approvals/excel")
    ResponseEntity<byte[]> payrollChangeApprovalsExcel(@RequestBody PayrollChangeReportExportRequest request) {
        return payrollChangeReportExportService.downloadApprovalExcel(request);
    }

    @PostMapping("/payroll-change-register/pdf")
    ResponseEntity<byte[]> payrollChangeRegisterPdf(@RequestBody PayrollChangeReportExportRequest request) {
        return payrollChangeReportExportService.downloadRegisterPdf(request);
    }

    @PostMapping("/payroll-change-register/excel")
    ResponseEntity<byte[]> payrollChangeRegisterExcel(@RequestBody PayrollChangeReportExportRequest request) {
        return payrollChangeReportExportService.downloadRegisterExcel(request);
    }

    @PostMapping(value = "/payroll-change-approvals/preview", produces = "text/html;charset=UTF-8")
    String payrollChangeApprovalsPreview(@RequestBody PayrollChangeReportExportRequest request) {
        return payrollChangeReportExportService.previewApprovalHtml(request);
    }

    @PostMapping(value = "/payroll-change-register/preview", produces = "text/html;charset=UTF-8")
    String payrollChangeRegisterPreview(@RequestBody PayrollChangeReportExportRequest request) {
        return payrollChangeReportExportService.previewRegisterHtml(request);
    }

    @PostMapping("/payroll-change-export-jobs")
    PayrollChangeReportExportJobView submitPayrollChangeExportJob(@RequestBody PayrollChangeReportExportJobRequest request) {
        return payrollChangeReportAsyncExportService.submit(request);
    }

    @GetMapping("/payroll-change-export-jobs/{jobId}")
    PayrollChangeReportExportJobView payrollChangeExportJob(
            @PathVariable String jobId,
            @RequestParam String accessToken) {
        return payrollChangeReportAsyncExportService.getJob(jobId, accessToken);
    }

    @GetMapping("/payroll-change-export-jobs/{jobId}/download")
    ResponseEntity<byte[]> downloadPayrollChangeExportJob(
            @PathVariable String jobId,
            @RequestParam String accessToken) {
        return payrollChangeReportAsyncExportService.downloadJob(jobId, accessToken);
    }

    @GetMapping("/wage-reform-2006-public-notice")
    PageResponse<WageReform2006PublicNoticeRow> wageReform2006PublicNotice(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return reportService.wageReform2006PublicNoticeRows(organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/personnel-report-candidates")
    PageResponse<PersonnelReportCandidateRow> personnelReportCandidates(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return reportService.personnelReportCandidates(organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/personnel-information-collection")
    PersonnelInformationCollectionReport personnelInformationCollection(@RequestParam int uid) {
        return reportService.personnelInformationCollection(uid);
    }

    @GetMapping("/personnel-information-registration")
    PersonnelInformationCollectionReport personnelInformationRegistration(@RequestParam int uid) {
        return reportService.personnelInformationCollection(uid);
    }
}
