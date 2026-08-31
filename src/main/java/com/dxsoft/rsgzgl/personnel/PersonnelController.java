package com.dxsoft.rsgzgl.personnel;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/personnel")
class PersonnelController {

    private final PersonnelService personnelService;
    private final SubrecordAttachmentService subrecordAttachmentService;

    PersonnelController(PersonnelService personnelService, SubrecordAttachmentService subrecordAttachmentService) {
        this.personnelService = personnelService;
        this.subrecordAttachmentService = subrecordAttachmentService;
    }

    @GetMapping
    PageResponse<PersonnelSummary> list(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return personnelService.list(organizationCode, keyword, sort, direction, PageRequest.of(page, size));
    }

    @GetMapping("/comprehensive-queries")
    PageResponse<PersonnelComprehensiveQueryRecord> comprehensiveQueries(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String personnelCategory,
            @RequestParam(required = false) String organizationType,
            @RequestParam(required = false) String postCategory,
            @RequestParam(required = false) String educationCode,
            @RequestParam(required = false) String birthYearMonthFrom,
            @RequestParam(required = false) String birthYearMonthTo,
            @RequestParam(required = false) String workStartYearMonthFrom,
            @RequestParam(required = false) String workStartYearMonthTo,
            @RequestParam(required = false) String regularizationYearMonthFrom,
            @RequestParam(required = false) String regularizationYearMonthTo,
            @RequestParam(required = false) String positionCode,
            @RequestParam(required = false) String positionCodePrefix,
            @RequestParam(required = false) String gradeLevelFrom,
            @RequestParam(required = false) String gradeLevelTo,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return personnelService.comprehensiveQueries(
                organizationCode,
                keyword,
                gender,
                personnelCategory,
                organizationType,
                postCategory,
                educationCode,
                birthYearMonthFrom,
                birthYearMonthTo,
                workStartYearMonthFrom,
                workStartYearMonthTo,
                regularizationYearMonthFrom,
                regularizationYearMonthTo,
                positionCode,
                positionCodePrefix,
                gradeLevelFrom,
                gradeLevelTo,
                PageRequest.of(page, size));
    }

    @GetMapping("/comprehensive-query-options")
    PersonnelComprehensiveQueryOptions comprehensiveQueryOptions() {
        return personnelService.comprehensiveQueryOptions();
    }

    @GetMapping("/{uid}")
    PersonnelDetail get(@PathVariable int uid) {
        return personnelService.get(uid);
    }

    @GetMapping("/{uid}/maintenance")
    PersonnelMaintenanceRecord maintenance(@PathVariable int uid) {
        return personnelService.maintenance(uid);
    }

    @GetMapping("/{uid}/field-policy")
    PersonnelFieldPolicyView fieldPolicy(@PathVariable int uid) {
        return personnelService.fieldPolicy(uid);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PersonnelMaintenanceRecord create(@RequestBody PersonnelMaintenanceRequest request) {
        return personnelService.create(request);
    }

    @PutMapping("/{uid}")
    PersonnelMaintenanceRecord update(@PathVariable int uid, @RequestBody PersonnelMaintenanceRequest request) {
        return personnelService.update(uid, request);
    }

    @PostMapping("/{uid}/approval/cancel")
    PersonnelMaintenanceRecord cancelApproval(
            @PathVariable int uid,
            @RequestBody(required = false) PersonnelApprovalCancelRequest request) {
        return personnelService.cancelApproval(uid, request);
    }

    @PostMapping("/{uid}/approval/submit")
    PersonnelMaintenanceRecord submitApproval(@PathVariable int uid) {
        return personnelService.submitApproval(uid);
    }

    @PostMapping("/{uid}/approval/approve")
    PersonnelMaintenanceRecord approvePersonnel(@PathVariable int uid) {
        return personnelService.approvePersonnel(uid);
    }

    @PostMapping("/{uid}/approval/return-to-draft")
    PersonnelMaintenanceRecord returnPersonnelToDraft(@PathVariable int uid) {
        return personnelService.returnPersonnelToDraft(uid);
    }

    @DeleteMapping("/{uid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable int uid) {
        personnelService.delete(uid);
    }

    @PostMapping("/{uid}/change")
    PersonnelChangeResult changePersonnel(@PathVariable int uid, @RequestBody PersonnelChangeRequest request) {
        return personnelService.changePersonnel(uid, request);
    }

    @GetMapping("/{uid}/positions")
    List<PositionRecord> positions(@PathVariable int uid) {
        return personnelService.positions(uid);
    }

    @PostMapping("/{uid}/positions")
    List<PositionRecord> createPosition(@PathVariable int uid, @RequestBody PositionMaintenanceRequest request) {
        return personnelService.createPosition(uid, request);
    }

    @PutMapping("/{uid}/positions/{id}")
    List<PositionRecord> updatePosition(@PathVariable int uid, @PathVariable int id, @RequestBody PositionMaintenanceRequest request) {
        return personnelService.updatePosition(uid, id, request);
    }

    @DeleteMapping("/{uid}/positions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deletePosition(@PathVariable int uid, @PathVariable int id) {
        personnelService.deletePosition(uid, id);
    }

    @GetMapping("/{uid}/positions/{id}/attachments")
    List<SubrecordAttachmentRecord> positionAttachments(@PathVariable int uid, @PathVariable int id) {
        return subrecordAttachmentService.listPositionAttachments(uid, id);
    }

    @PostMapping("/{uid}/positions/{id}/attachments")
    SubrecordAttachmentRecord uploadPositionAttachment(
            @PathVariable int uid,
            @PathVariable int id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return subrecordAttachmentService.uploadPositionAttachment(uid, id, file);
    }

    @GetMapping("/{uid}/positions/{id}/attachments/{attachmentId}/download")
    org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadPositionAttachment(
            @PathVariable int uid,
            @PathVariable int id,
            @PathVariable long attachmentId) {
        org.springframework.core.io.Resource resource = subrecordAttachmentService.downloadPositionAttachment(uid, id, attachmentId);
        String fileName = subrecordAttachmentService.downloadFileName(uid, id, attachmentId);
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        org.springframework.http.ContentDisposition.attachment()
                                .filename(fileName, java.nio.charset.StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(resource);
    }

    @GetMapping("/{uid}/positions/{id}/attachments/{attachmentId}/preview")
    org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> previewPositionAttachment(
            @PathVariable int uid,
            @PathVariable int id,
            @PathVariable long attachmentId) {
        SubrecordAttachmentPreview preview = subrecordAttachmentService.previewPositionAttachment(uid, id, attachmentId);
        return SubrecordAttachmentResponses.preview(preview.resource(), preview.fileName(), preview.contentType());
    }

    @DeleteMapping("/{uid}/positions/{id}/attachments/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deletePositionAttachment(
            @PathVariable int uid,
            @PathVariable int id,
            @PathVariable long attachmentId) {
        subrecordAttachmentService.deletePositionAttachment(uid, id, attachmentId);
    }

    @PostMapping("/{uid}/positions/{id}/approval/submit")
    List<PositionRecord> submitPosition(@PathVariable int uid, @PathVariable int id) {
        return personnelService.submitPosition(uid, id);
    }

    @PostMapping("/{uid}/positions/{id}/approval/return-to-draft")
    List<PositionRecord> returnPositionToDraft(@PathVariable int uid, @PathVariable int id) {
        return personnelService.returnPositionToDraft(uid, id);
    }

    @PostMapping("/{uid}/positions/{id}/approval/approve")
    List<PositionRecord> approvePosition(@PathVariable int uid, @PathVariable int id) {
        return personnelService.approvePosition(uid, id);
    }

    @PostMapping("/{uid}/positions/{id}/approval/cancel")
    List<PositionRecord> cancelPositionApproval(
            @PathVariable int uid,
            @PathVariable int id,
            @RequestBody(required = false) PersonnelApprovalCancelRequest request) {
        return personnelService.cancelPositionApproval(uid, id, request);
    }

    @GetMapping("/positions")
    PageResponse<PersonnelPositionHistoryRecord> positionHistories(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String positionCode,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return personnelService.positionHistories(organizationCode, keyword, positionCode, PageRequest.of(page, size));
    }

    @GetMapping("/{uid}/education")
    List<EducationRecord> education(@PathVariable int uid) {
        return personnelService.education(uid);
    }

    @PostMapping("/{uid}/education")
    List<EducationRecord> createEducation(@PathVariable int uid, @RequestBody EducationMaintenanceRequest request) {
        return personnelService.createEducation(uid, request);
    }

    @PutMapping("/{uid}/education/{id}")
    List<EducationRecord> updateEducation(@PathVariable int uid, @PathVariable int id, @RequestBody EducationMaintenanceRequest request) {
        return personnelService.updateEducation(uid, id, request);
    }

    @DeleteMapping("/{uid}/education/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteEducation(@PathVariable int uid, @PathVariable int id) {
        personnelService.deleteEducation(uid, id);
    }

    @PostMapping("/{uid}/education/{id}/approval/submit")
    List<EducationRecord> submitEducation(@PathVariable int uid, @PathVariable int id) {
        return personnelService.submitEducation(uid, id);
    }

    @PostMapping("/{uid}/education/{id}/approval/return-to-draft")
    List<EducationRecord> returnEducationToDraft(@PathVariable int uid, @PathVariable int id) {
        return personnelService.returnEducationToDraft(uid, id);
    }

    @PostMapping("/{uid}/education/{id}/approval/approve")
    List<EducationRecord> approveEducation(@PathVariable int uid, @PathVariable int id) {
        return personnelService.approveEducation(uid, id);
    }

    @PostMapping("/{uid}/education/{id}/approval/cancel")
    List<EducationRecord> cancelEducationApproval(
            @PathVariable int uid,
            @PathVariable int id,
            @RequestBody(required = false) PersonnelApprovalCancelRequest request) {
        return personnelService.cancelEducationApproval(uid, id, request);
    }

    @GetMapping("/education")
    PageResponse<PersonnelEducationHistoryRecord> educationHistories(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String educationCode,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return personnelService.educationHistories(organizationCode, keyword, educationCode, PageRequest.of(page, size));
    }

    @GetMapping("/{uid}/assessments")
    List<AssessmentRecord> assessments(@PathVariable int uid) {
        return personnelService.assessments(uid);
    }

    @GetMapping("/{uid}/assessments/missing")
    MissingAssessmentPreview missingAssessments(
            @PathVariable int uid,
            @RequestParam(required = false) String targetPeriod) {
        return personnelService.missingAssessments(uid, targetPeriod);
    }

    @GetMapping("/{uid}/related-records")
    Map<String, Object> relatedRecords(@PathVariable int uid) {
        return personnelService.relatedRecords(uid);
    }

    @PostMapping("/{uid}/assessments")
    List<AssessmentRecord> createAssessment(@PathVariable int uid, @RequestBody AssessmentMaintenanceRequest request) {
        return personnelService.createAssessment(uid, request);
    }

    @PutMapping("/{uid}/assessments/{id}")
    List<AssessmentRecord> updateAssessment(@PathVariable int uid, @PathVariable int id, @RequestBody AssessmentMaintenanceRequest request) {
        return personnelService.updateAssessment(uid, id, request);
    }

    @DeleteMapping("/{uid}/assessments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteAssessment(@PathVariable int uid, @PathVariable int id) {
        personnelService.deleteAssessment(uid, id);
    }

    @PostMapping("/{uid}/assessments/{id}/approval/submit")
    List<AssessmentRecord> submitAssessment(@PathVariable int uid, @PathVariable int id) {
        return personnelService.submitAssessment(uid, id);
    }

    @PostMapping("/{uid}/assessments/{id}/approval/return-to-draft")
    List<AssessmentRecord> returnAssessmentToDraft(@PathVariable int uid, @PathVariable int id) {
        return personnelService.returnAssessmentToDraft(uid, id);
    }

    @PostMapping("/{uid}/assessments/{id}/approval/approve")
    List<AssessmentRecord> approveAssessment(@PathVariable int uid, @PathVariable int id) {
        return personnelService.approveAssessment(uid, id);
    }

    @PostMapping("/{uid}/assessments/{id}/approval/cancel")
    List<AssessmentRecord> cancelAssessmentApproval(
            @PathVariable int uid,
            @PathVariable int id,
            @RequestBody(required = false) PersonnelApprovalCancelRequest request) {
        return personnelService.cancelAssessmentApproval(uid, id, request);
    }

    @PostMapping("/{uid}/awards")
    List<AwardRecord> createAward(@PathVariable int uid, @RequestBody AwardMaintenanceRequest request) {
        return personnelService.createAward(uid, request);
    }

    @PutMapping("/{uid}/awards/{id}")
    List<AwardRecord> updateAward(
            @PathVariable int uid,
            @PathVariable int id,
            @RequestBody AwardMaintenanceRequest request) {
        return personnelService.updateAward(uid, id, request);
    }

    @DeleteMapping("/{uid}/awards/{id}")
    void deleteAward(@PathVariable int uid, @PathVariable int id) {
        personnelService.deleteAward(uid, id);
    }

    @PostMapping("/{uid}/ranks")
    List<RankRecord> createRank(@PathVariable int uid, @RequestBody RankMaintenanceRequest request) {
        return personnelService.createRank(uid, request);
    }

    @PutMapping("/{uid}/ranks/{id}")
    List<RankRecord> updateRank(
            @PathVariable int uid,
            @PathVariable int id,
            @RequestBody RankMaintenanceRequest request) {
        return personnelService.updateRank(uid, id, request);
    }

    @DeleteMapping("/{uid}/ranks/{id}")
    void deleteRank(@PathVariable int uid, @PathVariable int id) {
        personnelService.deleteRank(uid, id);
    }

    @PostMapping("/{uid}/awards/{id}/approval/submit")
    List<AwardRecord> submitAward(@PathVariable int uid, @PathVariable int id) {
        return personnelService.submitAward(uid, id);
    }

    @PostMapping("/{uid}/awards/{id}/approval/return-to-draft")
    List<AwardRecord> returnAwardToDraft(@PathVariable int uid, @PathVariable int id) {
        return personnelService.returnAwardToDraft(uid, id);
    }

    @PostMapping("/{uid}/awards/{id}/approval/approve")
    List<AwardRecord> approveAward(@PathVariable int uid, @PathVariable int id) {
        return personnelService.approveAward(uid, id);
    }

    @PostMapping("/{uid}/awards/{id}/approval/cancel")
    List<AwardRecord> cancelAwardApproval(
            @PathVariable int uid,
            @PathVariable int id,
            @RequestBody(required = false) PersonnelApprovalCancelRequest request) {
        return personnelService.cancelAwardApproval(uid, id, request);
    }

    @PostMapping("/{uid}/ranks/{id}/approval/submit")
    List<RankRecord> submitRank(@PathVariable int uid, @PathVariable int id) {
        return personnelService.submitRank(uid, id);
    }

    @PostMapping("/{uid}/ranks/{id}/approval/return-to-draft")
    List<RankRecord> returnRankToDraft(@PathVariable int uid, @PathVariable int id) {
        return personnelService.returnRankToDraft(uid, id);
    }

    @PostMapping("/{uid}/ranks/{id}/approval/approve")
    List<RankRecord> approveRank(@PathVariable int uid, @PathVariable int id) {
        return personnelService.approveRank(uid, id);
    }

    @PostMapping("/{uid}/ranks/{id}/approval/cancel")
    List<RankRecord> cancelRankApproval(
            @PathVariable int uid,
            @PathVariable int id,
            @RequestBody(required = false) PersonnelApprovalCancelRequest request) {
        return personnelService.cancelRankApproval(uid, id, request);
    }

    @GetMapping("/assessments/batch-entry")
    BatchAssessmentPreview batchAssessmentPreview(
            @RequestParam(required = false) String organizationCode,
            @RequestParam String year,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "false") boolean includeDescendants) {
        return personnelService.batchAssessmentPreview(organizationCode, year, keyword, includeDescendants);
    }

    @PostMapping("/assessments/batch-entry")
    BatchAssessmentSaveResult saveBatchAssessments(@RequestBody BatchAssessmentSaveRequest request) {
        return personnelService.saveBatchAssessments(request);
    }

    @PostMapping("/assessments/batch-entry/submit")
    BatchAssessmentSubmitResult submitBatchAssessments(@RequestBody BatchAssessmentSubmitRequest request) {
        return personnelService.submitBatchAssessments(request);
    }

    @GetMapping("/assessments")
    PageResponse<AnnualAssessmentRecord> annualAssessments(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return personnelService.annualAssessments(organizationCode, year, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/assessment-summary")
    PageResponse<AnnualAssessmentSummaryRecord> annualAssessmentSummary(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String result,
            @RequestParam(required = false, defaultValue = "false") boolean includeDescendants,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return personnelService.annualAssessmentSummary(
                organizationCode, year, result, includeDescendants, PageRequest.of(page, size));
    }

    @GetMapping("/approval-tracking")
    PageResponse<PersonnelApprovalTrackingRecord> approvalTracking(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "false") boolean submittedByMe,
            @RequestParam(required = false) Integer approvedWithinDays,
            @RequestParam(required = false) String recordType,
            @RequestParam(required = false) String assessmentYear,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return personnelService.approvalTracking(
                organizationCode,
                keyword,
                status,
                submittedByMe,
                approvedWithinDays,
                recordType,
                assessmentYear,
                PageRequest.of(page, size));
    }

    @PostMapping("/approval-tracking/batch-approve")
    BatchApprovalResult batchApproveTrackingRecords(@RequestBody BatchApprovalRequest request) {
        return personnelService.batchApproveTrackingRecords(request);
    }

    @GetMapping("/assessments/approval-stats")
    AssessmentApprovalStats assessmentApprovalStats(
            @RequestParam(required = false) String organizationCode,
            @RequestParam String year,
            @RequestParam(required = false, defaultValue = "false") boolean includeDescendants) {
        return personnelService.assessmentApprovalStats(organizationCode, year, includeDescendants);
    }

    @GetMapping("/changed")
    PageResponse<ChangedPersonnelRecord> changedPersonnel(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return personnelService.changedPersonnel(organizationCode, period, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/changed/{uid}/detail")
    ChangedPersonnelDetail changedPersonnelDetail(@PathVariable int uid) {
        return personnelService.changedPersonnelDetail(uid);
    }

    @PostMapping("/changed/restore")
    PersonnelChangeResult restoreChangedPersonnel(@RequestBody ChangedPersonnelRestoreRequest request) {
        return personnelService.restoreChangedPersonnel(request);
    }
}
