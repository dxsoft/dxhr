package com.dxsoft.rsgzgl.retirement;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/retirement")
class RetirementController {

    private final RetirementService retirementService;

    RetirementController(RetirementService retirementService) {
        this.retirementService = retirementService;
    }

    @GetMapping("/approval-report/styles")
    java.util.List<RetirementApprovalStyleOption> approvalStyles() {
        return retirementService.approvalStyles();
    }

    @GetMapping("/approval-report/resolve-template")
    Map<String, String> resolveTemplate(
            @RequestParam(required = false) String style,
            @RequestParam(required = false) String organizationNature) {
        RetirementApprovalStyle resolved = retirementService.resolveStyle(style);
        String template = retirementService.resolveTemplateName(resolved, organizationNature);
        return Map.of(
                "style", resolved.code(),
                "styleLabel", resolved.label(),
                "template", template,
                "organizationNature", organizationNature == null ? "" : organizationNature);
    }

    @GetMapping("/processing/candidates")
    PageResponse<RetirementProcessingCandidate> processingCandidates(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String referencePeriod,
            @RequestParam(defaultValue = "false") boolean includeDescendants,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return retirementService.processingCandidates(
                organizationCode, keyword, referencePeriod, includeDescendants, PageRequest.of(page, size));
    }

    @GetMapping("/processing/{uid}/preview")
    RetirementProcessingPreview processingPreview(
            @PathVariable int uid,
            @RequestParam(required = false) String retirementDate,
            @RequestParam(required = false) String retirementCategory,
            @RequestParam(required = false) String retirementReason) {
        return retirementService.processingPreview(uid, retirementDate, retirementCategory, retirementReason);
    }

    @PostMapping("/processing/{uid}/apply")
    RetirementProcessingApplyResult applyProcessing(
            @PathVariable int uid,
            @RequestBody(required = false) RetirementProcessingApplyRequest request) {
        return retirementService.applyProcessing(uid, request);
    }

    @GetMapping("/retirees")
    PageResponse<RetirementRetireeRecord> retirees(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean includeDescendants,
            @RequestParam(defaultValue = "true") boolean pendingOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return retirementService.retirees(
                organizationCode, keyword, includeDescendants, pendingOnly, PageRequest.of(page, size));
    }

    @GetMapping("/retirees/{id}")
    RetirementRetireeDetail retireeDetail(@PathVariable int id) {
        return retirementService.retireeDetail(id);
    }

    @PutMapping("/retirees/{id}")
    RetirementRetireeDetail updateRetiree(
            @PathVariable int id,
            @RequestBody RetirementRetireeUpdateRequest request) {
        return retirementService.updateRetiree(id, request);
    }

    @PostMapping("/retirees/{id}/approve")
    RetirementRetireeDetail approveRetiree(@PathVariable int id) {
        return retirementService.approveRetiree(id);
    }

    @PostMapping("/retirees/{id}/cancel-approval")
    RetirementRetireeDetail cancelRetireeApproval(@PathVariable int id) {
        return retirementService.cancelRetireeApproval(id);
    }

    @GetMapping("/position-level-range")
    RetirementPositionLevelRange positionLevelRange(@RequestParam String positionCode) {
        return retirementService.positionLevelRange(positionCode);
    }

    @GetMapping("/approval-report/candidates")
    PageResponse<RetirementRetireeRecord> approvalReportCandidates(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return retirementService.approvalReportCandidates(
                organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/ratio-standards")
    java.util.List<RetirementRatioStandard> ratioStandards() {
        return retirementService.ratioStandards();
    }

    @PostMapping("/approval-report/pdf")
    ResponseEntity<byte[]> exportApprovalReportPdf(@RequestBody RetirementApprovalExportRequest request) {
        return retirementService.exportApprovalReportPdf(request);
    }

    @PostMapping("/approval-report/preview")
    Map<String, String> previewApprovalReport(@RequestBody RetirementApprovalExportRequest request) {
        return Map.of("html", retirementService.previewApprovalReportHtml(request));
    }
}
