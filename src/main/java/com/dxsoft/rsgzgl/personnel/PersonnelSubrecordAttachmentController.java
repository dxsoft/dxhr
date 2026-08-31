package com.dxsoft.rsgzgl.personnel;

import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/personnel")
class PersonnelSubrecordAttachmentController {

    private final SubrecordAttachmentService subrecordAttachmentService;

    PersonnelSubrecordAttachmentController(SubrecordAttachmentService subrecordAttachmentService) {
        this.subrecordAttachmentService = subrecordAttachmentService;
    }

    @GetMapping("/{uid}/basic-attachments")
    List<SubrecordAttachmentRecord> basicAttachments(@PathVariable int uid) {
        return subrecordAttachmentService.listMainAttachments(uid);
    }

    @PostMapping("/{uid}/basic-attachments")
    SubrecordAttachmentRecord uploadBasicAttachment(@PathVariable int uid, @RequestParam("file") MultipartFile file) {
        return subrecordAttachmentService.uploadMainAttachment(uid, file);
    }

    @GetMapping("/{uid}/basic-attachments/{attachmentId}/download")
    ResponseEntity<Resource> downloadBasicAttachment(@PathVariable int uid, @PathVariable long attachmentId) {
        Resource resource = subrecordAttachmentService.downloadMainAttachment(uid, attachmentId);
        String fileName = subrecordAttachmentService.downloadMainFileName(uid, attachmentId);
        return SubrecordAttachmentResponses.download(resource, fileName);
    }

    @GetMapping("/{uid}/basic-attachments/{attachmentId}/preview")
    ResponseEntity<Resource> previewBasicAttachment(@PathVariable int uid, @PathVariable long attachmentId) {
        SubrecordAttachmentPreview preview = subrecordAttachmentService.previewMainAttachment(uid, attachmentId);
        return SubrecordAttachmentResponses.preview(preview.resource(), preview.fileName(), preview.contentType());
    }

    @DeleteMapping("/{uid}/basic-attachments/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteBasicAttachment(@PathVariable int uid, @PathVariable long attachmentId) {
        subrecordAttachmentService.deleteMainAttachment(uid, attachmentId);
    }

    @GetMapping("/{uid}/education/{id}/attachments")
    List<SubrecordAttachmentRecord> educationAttachments(@PathVariable int uid, @PathVariable int id) {
        return subrecordAttachmentService.listEducationAttachments(uid, id);
    }

    @PostMapping("/{uid}/education/{id}/attachments")
    SubrecordAttachmentRecord uploadEducationAttachment(
            @PathVariable int uid,
            @PathVariable int id,
            @RequestParam("file") MultipartFile file) {
        return subrecordAttachmentService.uploadEducationAttachment(uid, id, file);
    }

    @GetMapping("/{uid}/education/{id}/attachments/{attachmentId}/download")
    ResponseEntity<Resource> downloadEducationAttachment(
            @PathVariable int uid,
            @PathVariable int id,
            @PathVariable long attachmentId) {
        Resource resource = subrecordAttachmentService.downloadEducationAttachment(uid, id, attachmentId);
        String fileName = subrecordAttachmentService.downloadEducationFileName(uid, id, attachmentId);
        return SubrecordAttachmentResponses.download(resource, fileName);
    }

    @GetMapping("/{uid}/education/{id}/attachments/{attachmentId}/preview")
    ResponseEntity<Resource> previewEducationAttachment(
            @PathVariable int uid,
            @PathVariable int id,
            @PathVariable long attachmentId) {
        SubrecordAttachmentPreview preview = subrecordAttachmentService.previewEducationAttachment(uid, id, attachmentId);
        return SubrecordAttachmentResponses.preview(preview.resource(), preview.fileName(), preview.contentType());
    }

    @DeleteMapping("/{uid}/education/{id}/attachments/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteEducationAttachment(@PathVariable int uid, @PathVariable int id, @PathVariable long attachmentId) {
        subrecordAttachmentService.deleteEducationAttachment(uid, id, attachmentId);
    }

    @GetMapping("/{uid}/assessments/{id}/attachments")
    List<SubrecordAttachmentRecord> assessmentAttachments(@PathVariable int uid, @PathVariable int id) {
        return subrecordAttachmentService.listAssessmentAttachments(uid, id);
    }

    @PostMapping("/{uid}/assessments/{id}/attachments")
    SubrecordAttachmentRecord uploadAssessmentAttachment(
            @PathVariable int uid,
            @PathVariable int id,
            @RequestParam("file") MultipartFile file) {
        return subrecordAttachmentService.uploadAssessmentAttachment(uid, id, file);
    }

    @GetMapping("/{uid}/assessments/{id}/attachments/{attachmentId}/download")
    ResponseEntity<Resource> downloadAssessmentAttachment(
            @PathVariable int uid,
            @PathVariable int id,
            @PathVariable long attachmentId) {
        Resource resource = subrecordAttachmentService.downloadAssessmentAttachment(uid, id, attachmentId);
        String fileName = subrecordAttachmentService.downloadAssessmentFileName(uid, id, attachmentId);
        return SubrecordAttachmentResponses.download(resource, fileName);
    }

    @GetMapping("/{uid}/assessments/{id}/attachments/{attachmentId}/preview")
    ResponseEntity<Resource> previewAssessmentAttachment(
            @PathVariable int uid,
            @PathVariable int id,
            @PathVariable long attachmentId) {
        SubrecordAttachmentPreview preview = subrecordAttachmentService.previewAssessmentAttachment(uid, id, attachmentId);
        return SubrecordAttachmentResponses.preview(preview.resource(), preview.fileName(), preview.contentType());
    }

    @DeleteMapping("/{uid}/assessments/{id}/attachments/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteAssessmentAttachment(@PathVariable int uid, @PathVariable int id, @PathVariable long attachmentId) {
        subrecordAttachmentService.deleteAssessmentAttachment(uid, id, attachmentId);
    }

    @GetMapping("/{uid}/awards/{id}/attachments")
    List<SubrecordAttachmentRecord> awardAttachments(@PathVariable int uid, @PathVariable int id) {
        return subrecordAttachmentService.listAwardAttachments(uid, id);
    }

    @PostMapping("/{uid}/awards/{id}/attachments")
    SubrecordAttachmentRecord uploadAwardAttachment(
            @PathVariable int uid,
            @PathVariable int id,
            @RequestParam("file") MultipartFile file) {
        return subrecordAttachmentService.uploadAwardAttachment(uid, id, file);
    }

    @GetMapping("/{uid}/awards/{id}/attachments/{attachmentId}/download")
    ResponseEntity<Resource> downloadAwardAttachment(
            @PathVariable int uid,
            @PathVariable int id,
            @PathVariable long attachmentId) {
        Resource resource = subrecordAttachmentService.downloadAwardAttachment(uid, id, attachmentId);
        String fileName = subrecordAttachmentService.downloadAwardFileName(uid, id, attachmentId);
        return SubrecordAttachmentResponses.download(resource, fileName);
    }

    @GetMapping("/{uid}/awards/{id}/attachments/{attachmentId}/preview")
    ResponseEntity<Resource> previewAwardAttachment(
            @PathVariable int uid,
            @PathVariable int id,
            @PathVariable long attachmentId) {
        SubrecordAttachmentPreview preview = subrecordAttachmentService.previewAwardAttachment(uid, id, attachmentId);
        return SubrecordAttachmentResponses.preview(preview.resource(), preview.fileName(), preview.contentType());
    }

    @DeleteMapping("/{uid}/awards/{id}/attachments/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteAwardAttachment(@PathVariable int uid, @PathVariable int id, @PathVariable long attachmentId) {
        subrecordAttachmentService.deleteAwardAttachment(uid, id, attachmentId);
    }

    @GetMapping("/{uid}/ranks/{id}/attachments")
    List<SubrecordAttachmentRecord> rankAttachments(@PathVariable int uid, @PathVariable int id) {
        return subrecordAttachmentService.listRankAttachments(uid, id);
    }

    @PostMapping("/{uid}/ranks/{id}/attachments")
    SubrecordAttachmentRecord uploadRankAttachment(
            @PathVariable int uid,
            @PathVariable int id,
            @RequestParam("file") MultipartFile file) {
        return subrecordAttachmentService.uploadRankAttachment(uid, id, file);
    }

    @GetMapping("/{uid}/ranks/{id}/attachments/{attachmentId}/download")
    ResponseEntity<Resource> downloadRankAttachment(
            @PathVariable int uid,
            @PathVariable int id,
            @PathVariable long attachmentId) {
        Resource resource = subrecordAttachmentService.downloadRankAttachment(uid, id, attachmentId);
        String fileName = subrecordAttachmentService.downloadRankFileName(uid, id, attachmentId);
        return SubrecordAttachmentResponses.download(resource, fileName);
    }

    @GetMapping("/{uid}/ranks/{id}/attachments/{attachmentId}/preview")
    ResponseEntity<Resource> previewRankAttachment(
            @PathVariable int uid,
            @PathVariable int id,
            @PathVariable long attachmentId) {
        SubrecordAttachmentPreview preview = subrecordAttachmentService.previewRankAttachment(uid, id, attachmentId);
        return SubrecordAttachmentResponses.preview(preview.resource(), preview.fileName(), preview.contentType());
    }

    @DeleteMapping("/{uid}/ranks/{id}/attachments/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteRankAttachment(@PathVariable int uid, @PathVariable int id, @PathVariable long attachmentId) {
        subrecordAttachmentService.deleteRankAttachment(uid, id, attachmentId);
    }

    @GetMapping("/{uid}/transfers/{id}/attachments")
    List<SubrecordAttachmentRecord> transferAttachments(@PathVariable int uid, @PathVariable long id) {
        return subrecordAttachmentService.listTransferAttachments(uid, id);
    }

    @PostMapping("/{uid}/transfers/{id}/attachments")
    SubrecordAttachmentRecord uploadTransferAttachment(
            @PathVariable int uid,
            @PathVariable long id,
            @RequestParam("file") MultipartFile file) {
        return subrecordAttachmentService.uploadTransferAttachment(uid, id, file);
    }

    @GetMapping("/{uid}/transfers/{id}/attachments/{attachmentId}/download")
    ResponseEntity<Resource> downloadTransferAttachment(
            @PathVariable int uid,
            @PathVariable long id,
            @PathVariable long attachmentId) {
        Resource resource = subrecordAttachmentService.downloadTransferAttachment(uid, id, attachmentId);
        String fileName = subrecordAttachmentService.downloadTransferFileName(uid, id, attachmentId);
        return SubrecordAttachmentResponses.download(resource, fileName);
    }

    @GetMapping("/{uid}/transfers/{id}/attachments/{attachmentId}/preview")
    ResponseEntity<Resource> previewTransferAttachment(
            @PathVariable int uid,
            @PathVariable long id,
            @PathVariable long attachmentId) {
        SubrecordAttachmentPreview preview = subrecordAttachmentService.previewTransferAttachment(uid, id, attachmentId);
        return SubrecordAttachmentResponses.preview(preview.resource(), preview.fileName(), preview.contentType());
    }

    @DeleteMapping("/{uid}/transfers/{id}/attachments/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteTransferAttachment(@PathVariable int uid, @PathVariable long id, @PathVariable long attachmentId) {
        subrecordAttachmentService.deleteTransferAttachment(uid, id, attachmentId);
    }
}
