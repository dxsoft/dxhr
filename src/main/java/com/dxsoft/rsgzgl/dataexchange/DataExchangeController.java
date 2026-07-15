package com.dxsoft.rsgzgl.dataexchange;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data-exchange")
class DataExchangeController {

    private final DataExchangeService dataExchangeService;

    DataExchangeController(DataExchangeService dataExchangeService) {
        this.dataExchangeService = dataExchangeService;
    }

    @GetMapping("/personnel")
    PageResponse<PersonnelExportRecord> exportPersonnel(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return dataExchangeService.exportPersonnel(organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/personnel/download")
    ResponseEntity<byte[]> downloadPersonnelCsv(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword) {
        return dataExchangeService.downloadPersonnelCsv(organizationCode, keyword);
    }

    @PostMapping("/dispatch/preview")
    DataExchangeService.PersonnelExchangePackage previewDispatchPackage(@RequestBody PersonnelDispatchRequest request) {
        return dataExchangeService.buildPersonnelPackage(request);
    }

    @PostMapping("/dispatch/personnel")
    ResponseEntity<byte[]> dispatchPersonnelPackage(@RequestBody PersonnelDispatchRequest request) {
        return dataExchangeService.dispatchPersonnelPackage(request);
    }

    @PostMapping("/submission/preview")
    PayrollSubmissionPackage previewSubmissionPackage(@RequestBody PersonnelDispatchRequest request) {
        return dataExchangeService.buildSubmissionPackage(request);
    }

    @PostMapping("/submission/export")
    ResponseEntity<byte[]> exportSubmissionPackage(@RequestBody PersonnelDispatchRequest request) {
        return dataExchangeService.dispatchSubmissionPackage(request);
    }

    @PostMapping("/submission/review/preview")
    SubmissionReviewPreviewResponse previewSubmissionReview(@RequestBody SubmissionReviewRequest request) {
        return dataExchangeService.previewSubmissionReview(request);
    }

    @PostMapping("/submission/review/apply")
    SubmissionReviewApplyResponse applySubmissionReview(@RequestBody SubmissionReviewRequest request) {
        return dataExchangeService.applySubmissionReview(request);
    }

    @PostMapping("/approval/preview")
    PayrollSubmissionPackage previewApprovalPackage(@RequestBody PersonnelDispatchRequest request) {
        return dataExchangeService.buildApprovalPackage(request);
    }

    @PostMapping("/approval/export")
    ResponseEntity<byte[]> exportApprovalPackage(@RequestBody PersonnelDispatchRequest request) {
        return dataExchangeService.dispatchApprovalPackage(request);
    }

    @PostMapping("/approval/receive/preview")
    SubmissionReviewPreviewResponse previewApprovalReceive(@RequestBody ApprovalReceiveRequest request) {
        return dataExchangeService.previewApprovalReceive(request);
    }

    @PostMapping("/approval/receive/apply")
    SubmissionReviewApplyResponse applyApprovalReceive(@RequestBody ApprovalReceiveRequest request) {
        return dataExchangeService.applyApprovalReceive(request);
    }

    @PostMapping("/receive/preview")
    ReceivePreviewResponse previewReceive(@RequestBody ReceiveRequest request) {
        return dataExchangeService.previewReceive(request);
    }

    @PostMapping("/receive/apply")
    ReceiveApplyResponse applyReceive(@RequestBody ReceiveRequest request) {
        return dataExchangeService.applyReceive(request);
    }

    @GetMapping("/annual-report")
    PageResponse<AnnualReportRecord> exportAnnualReport(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return dataExchangeService.exportAnnualReport(organizationCode, period, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/annual-report/download")
    ResponseEntity<byte[]> downloadAnnualReportCsv(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String keyword) {
        return dataExchangeService.downloadAnnualReportCsv(organizationCode, period, keyword);
    }

    @GetMapping("/annual-report/excel")
    ResponseEntity<byte[]> downloadAnnualReportExcel(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String keyword) {
        return dataExchangeService.downloadAnnualReportExcel(organizationCode, period, keyword);
    }

    record PersonnelDispatchRequest(
            List<String> organizationCodes,
            boolean includeDescendants,
            String keyword,
            List<PersonKey> selectedPersonnel) {
    }

    record PersonKey(String organizationCode, String personCode) {
    }

    record ReceiveRequest(
            String packageJson,
            String mode,
            String targetOrganizationCode,
            List<PersonKey> selectedPersonnel,
            Boolean dryRun) {
    }

    record ReceivePreviewResponse(
            int totalRecords,
            List<PersonnelExportRecord> rows,
            List<ReceivePreviewRow> previewRows,
            ReceiveSummary summary,
            List<String> sampleErrors,
            String message) {
    }

    record ReceivePreviewRow(
            String organizationCode,
            String personCode,
            String name,
            String action,
            boolean targetOrganizationExists,
            String targetOrganizationCode,
            String targetPersonCode,
            List<TableCount> relatedCounts) {
    }

    record TableCount(String tableName, int count) {
    }

    record ReceiveSummary(
            int totalRecords,
            int newRecords,
            int replaceRecords,
            int appendRecords,
            List<TableCount> relatedCounts) {
    }

    record CodeMapping(String sourceOrganizationCode, String sourcePersonCode, String targetOrganizationCode, String targetPersonCode, String name) {
    }

    record ReceiveApplyResponse(
            int receivedRecords,
            int newRecords,
            int replacedRecords,
            int appendedRecords,
            List<CodeMapping> codeMappings,
            ReceiveSummary relatedSummary,
            String message) {
    }

    record SubmissionReviewRequest(
            String packageJson,
            String decision,
            List<PersonKey> selectedPersonnel,
            Boolean dryRun) {
    }

    record SubmissionReviewPreviewRow(
            String organizationCode,
            String organizationName,
            String personCode,
            String name,
            String changeType,
            String calculationPeriod,
            Integer totalAmount,
            String approvalStatus,
            String submissionStatus,
            int payrollRecordCount,
            boolean organizationExists,
            boolean personExists,
            String action) {
    }

    record SubmissionReviewSummary(
            int totalRecords,
            int newRecords,
            int replaceRecords,
            int payrollRecords) {
    }

    record SubmissionReviewPreviewResponse(
            int totalRecords,
            List<SubmissionReviewPreviewRow> previewRows,
            SubmissionReviewSummary summary,
            String message) {
    }

    record SubmissionReviewApplyResponse(
            int processedRecords,
            SubmissionReviewSummary summary,
            String message) {
    }

    record ApprovalReceiveRequest(
            String packageJson,
            List<PersonKey> selectedPersonnel,
            Boolean dryRun) {
    }
}
