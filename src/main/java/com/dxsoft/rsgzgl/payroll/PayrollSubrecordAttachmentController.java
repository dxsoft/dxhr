package com.dxsoft.rsgzgl.payroll;

import com.dxsoft.rsgzgl.personnel.SubrecordAttachmentPreview;
import com.dxsoft.rsgzgl.personnel.SubrecordAttachmentRecord;
import com.dxsoft.rsgzgl.personnel.SubrecordAttachmentResponses;
import com.dxsoft.rsgzgl.personnel.SubrecordAttachmentService;
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
@RequestMapping("/api/payroll")
class PayrollSubrecordAttachmentController {

    private final SubrecordAttachmentService subrecordAttachmentService;

    PayrollSubrecordAttachmentController(SubrecordAttachmentService subrecordAttachmentService) {
        this.subrecordAttachmentService = subrecordAttachmentService;
    }

    @GetMapping("/personnel/{uid}/histories/{id}/attachments")
    List<SubrecordAttachmentRecord> payrollHistoryAttachments(@PathVariable int uid, @PathVariable String id) {
        return subrecordAttachmentService.listPayrollAttachments(uid, id);
    }

    @PostMapping("/personnel/{uid}/histories/{id}/attachments")
    SubrecordAttachmentRecord uploadPayrollHistoryAttachment(
            @PathVariable int uid,
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        return subrecordAttachmentService.uploadPayrollAttachment(uid, id, file);
    }

    @GetMapping("/personnel/{uid}/histories/{id}/attachments/{attachmentId}/download")
    ResponseEntity<Resource> downloadPayrollHistoryAttachment(
            @PathVariable int uid,
            @PathVariable String id,
            @PathVariable long attachmentId) {
        Resource resource = subrecordAttachmentService.downloadPayrollAttachment(uid, id, attachmentId);
        String fileName = subrecordAttachmentService.downloadPayrollFileName(uid, id, attachmentId);
        return SubrecordAttachmentResponses.download(resource, fileName);
    }

    @GetMapping("/personnel/{uid}/histories/{id}/attachments/{attachmentId}/preview")
    ResponseEntity<Resource> previewPayrollHistoryAttachment(
            @PathVariable int uid,
            @PathVariable String id,
            @PathVariable long attachmentId) {
        SubrecordAttachmentPreview preview = subrecordAttachmentService.previewPayrollAttachment(uid, id, attachmentId);
        return SubrecordAttachmentResponses.preview(preview.resource(), preview.fileName(), preview.contentType());
    }

    @DeleteMapping("/personnel/{uid}/histories/{id}/attachments/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deletePayrollHistoryAttachment(
            @PathVariable int uid,
            @PathVariable String id,
            @PathVariable long attachmentId) {
        subrecordAttachmentService.deletePayrollAttachment(uid, id, attachmentId);
    }
}
