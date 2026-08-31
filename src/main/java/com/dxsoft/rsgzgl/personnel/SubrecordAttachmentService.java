package com.dxsoft.rsgzgl.personnel;

import com.dxsoft.rsgzgl.common.NotFoundException;

import com.dxsoft.rsgzgl.payroll.PayrollRepository;

import com.dxsoft.rsgzgl.security.AccessControlService;

import com.dxsoft.rsgzgl.security.PersonnelFeaturePermissions;

import java.io.IOException;

import java.util.List;

import org.springframework.core.io.Resource;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

@Service

public class SubrecordAttachmentService {

    static final String POSITION_TABLE = "dryzwbh";

    private final SubrecordAttachmentRepository attachmentRepository;

    private final SubrecordAttachmentStorage attachmentStorage;

    private final PersonnelRepository personnelRepository;

    private final PayrollRepository payrollRepository;

    private final PersonnelSubrecordEditPolicy personnelSubrecordEditPolicy;

    private final AccessControlService accessControlService;

    public SubrecordAttachmentService(

            SubrecordAttachmentRepository attachmentRepository,

            SubrecordAttachmentStorage attachmentStorage,

            PersonnelRepository personnelRepository,

            PayrollRepository payrollRepository,

            PersonnelSubrecordEditPolicy personnelSubrecordEditPolicy,

            AccessControlService accessControlService) {

        this.attachmentRepository = attachmentRepository;

        this.attachmentStorage = attachmentStorage;

        this.personnelRepository = personnelRepository;

        this.payrollRepository = payrollRepository;

        this.personnelSubrecordEditPolicy = personnelSubrecordEditPolicy;

        this.accessControlService = accessControlService;

    }

    public List<SubrecordAttachmentRecord> listMainAttachments(int uid) {

        AttachmentAccess access = resolveAccess(uid, SubrecordAttachmentTarget.MAIN, uid, "", false, false);

        return attachmentRepository.findByKey(access.key());

    }

    public SubrecordAttachmentRecord uploadMainAttachment(int uid, MultipartFile file) {

        return upload(uid, SubrecordAttachmentTarget.MAIN, uid, "", file, false);

    }

    public Resource downloadMainAttachment(int uid, long attachmentId) {

        return download(uid, SubrecordAttachmentTarget.MAIN, uid, "", attachmentId, false);

    }

    public SubrecordAttachmentPreview previewMainAttachment(int uid, long attachmentId) {

        return preview(uid, SubrecordAttachmentTarget.MAIN, uid, "", attachmentId, false);

    }

    public String downloadMainFileName(int uid, long attachmentId) {

        return fileName(uid, SubrecordAttachmentTarget.MAIN, uid, "", attachmentId, false);

    }

    public String previewMainContentType(int uid, long attachmentId) {

        return previewContentType(uid, SubrecordAttachmentTarget.MAIN, uid, "", attachmentId, false);

    }

    public void deleteMainAttachment(int uid, long attachmentId) {

        delete(uid, SubrecordAttachmentTarget.MAIN, uid, "", attachmentId, false);

    }

    public List<SubrecordAttachmentRecord> listEducationAttachments(int uid, int educationId) {

        AttachmentAccess access = resolveAccess(uid, SubrecordAttachmentTarget.EDUCATION, educationId, "", false, false);

        return attachmentRepository.findByKey(access.key());

    }

    public SubrecordAttachmentRecord uploadEducationAttachment(int uid, int educationId, MultipartFile file) {

        return upload(uid, SubrecordAttachmentTarget.EDUCATION, educationId, "", file, false);

    }

    public Resource downloadEducationAttachment(int uid, int educationId, long attachmentId) {

        return download(uid, SubrecordAttachmentTarget.EDUCATION, educationId, "", attachmentId, false);

    }

    public SubrecordAttachmentPreview previewEducationAttachment(int uid, int educationId, long attachmentId) {

        return preview(uid, SubrecordAttachmentTarget.EDUCATION, educationId, "", attachmentId, false);

    }

    public String downloadEducationFileName(int uid, int educationId, long attachmentId) {

        return fileName(uid, SubrecordAttachmentTarget.EDUCATION, educationId, "", attachmentId, false);

    }

    public String previewEducationContentType(int uid, int educationId, long attachmentId) {

        return previewContentType(uid, SubrecordAttachmentTarget.EDUCATION, educationId, "", attachmentId, false);

    }

    public void deleteEducationAttachment(int uid, int educationId, long attachmentId) {

        delete(uid, SubrecordAttachmentTarget.EDUCATION, educationId, "", attachmentId, false);

    }

    public List<SubrecordAttachmentRecord> listPositionAttachments(int uid, int positionId) {

        AttachmentAccess access = resolveAccess(uid, SubrecordAttachmentTarget.POSITION, positionId, "", false, false);

        return attachmentRepository.findByKey(access.key());

    }

    public SubrecordAttachmentRecord uploadPositionAttachment(int uid, int positionId, MultipartFile file) {

        return upload(uid, SubrecordAttachmentTarget.POSITION, positionId, "", file, false);

    }

    public Resource downloadPositionAttachment(int uid, int positionId, long attachmentId) {

        return download(uid, SubrecordAttachmentTarget.POSITION, positionId, "", attachmentId, false);

    }

    public SubrecordAttachmentPreview previewPositionAttachment(int uid, int positionId, long attachmentId) {

        return preview(uid, SubrecordAttachmentTarget.POSITION, positionId, "", attachmentId, false);

    }

    public String downloadFileName(int uid, int positionId, long attachmentId) {

        return fileName(uid, SubrecordAttachmentTarget.POSITION, positionId, "", attachmentId, false);

    }

    public String previewContentType(int uid, int positionId, long attachmentId) {

        return previewContentType(uid, SubrecordAttachmentTarget.POSITION, positionId, "", attachmentId, false);

    }

    public void deletePositionAttachment(int uid, int positionId, long attachmentId) {

        delete(uid, SubrecordAttachmentTarget.POSITION, positionId, "", attachmentId, false);

    }

    public List<SubrecordAttachmentRecord> listAssessmentAttachments(int uid, int assessmentId) {

        AttachmentAccess access = resolveAccess(uid, SubrecordAttachmentTarget.ASSESSMENT, assessmentId, "", false, false);

        return attachmentRepository.findByKey(access.key());

    }

    public SubrecordAttachmentRecord uploadAssessmentAttachment(int uid, int assessmentId, MultipartFile file) {

        return upload(uid, SubrecordAttachmentTarget.ASSESSMENT, assessmentId, "", file, false);

    }

    public Resource downloadAssessmentAttachment(int uid, int assessmentId, long attachmentId) {

        return download(uid, SubrecordAttachmentTarget.ASSESSMENT, assessmentId, "", attachmentId, false);

    }

    public SubrecordAttachmentPreview previewAssessmentAttachment(int uid, int assessmentId, long attachmentId) {

        return preview(uid, SubrecordAttachmentTarget.ASSESSMENT, assessmentId, "", attachmentId, false);

    }

    public String downloadAssessmentFileName(int uid, int assessmentId, long attachmentId) {

        return fileName(uid, SubrecordAttachmentTarget.ASSESSMENT, assessmentId, "", attachmentId, false);

    }

    public String previewAssessmentContentType(int uid, int assessmentId, long attachmentId) {

        return previewContentType(uid, SubrecordAttachmentTarget.ASSESSMENT, assessmentId, "", attachmentId, false);

    }

    public void deleteAssessmentAttachment(int uid, int assessmentId, long attachmentId) {

        delete(uid, SubrecordAttachmentTarget.ASSESSMENT, assessmentId, "", attachmentId, false);

    }

    public List<SubrecordAttachmentRecord> listAwardAttachments(int uid, int awardId) {

        AttachmentAccess access = resolveAccess(uid, SubrecordAttachmentTarget.AWARD, awardId, "", false, false);

        return attachmentRepository.findByKey(access.key());

    }

    public SubrecordAttachmentRecord uploadAwardAttachment(int uid, int awardId, MultipartFile file) {

        return upload(uid, SubrecordAttachmentTarget.AWARD, awardId, "", file, false);

    }

    public Resource downloadAwardAttachment(int uid, int awardId, long attachmentId) {

        return download(uid, SubrecordAttachmentTarget.AWARD, awardId, "", attachmentId, false);

    }

    public SubrecordAttachmentPreview previewAwardAttachment(int uid, int awardId, long attachmentId) {

        return preview(uid, SubrecordAttachmentTarget.AWARD, awardId, "", attachmentId, false);

    }

    public String downloadAwardFileName(int uid, int awardId, long attachmentId) {

        return fileName(uid, SubrecordAttachmentTarget.AWARD, awardId, "", attachmentId, false);

    }

    public String previewAwardContentType(int uid, int awardId, long attachmentId) {

        return previewContentType(uid, SubrecordAttachmentTarget.AWARD, awardId, "", attachmentId, false);

    }

    public void deleteAwardAttachment(int uid, int awardId, long attachmentId) {

        delete(uid, SubrecordAttachmentTarget.AWARD, awardId, "", attachmentId, false);

    }

    public List<SubrecordAttachmentRecord> listRankAttachments(int uid, int rankId) {

        AttachmentAccess access = resolveAccess(uid, SubrecordAttachmentTarget.RANK, rankId, "", false, false);

        return attachmentRepository.findByKey(access.key());

    }

    public SubrecordAttachmentRecord uploadRankAttachment(int uid, int rankId, MultipartFile file) {

        return upload(uid, SubrecordAttachmentTarget.RANK, rankId, "", file, false);

    }

    public Resource downloadRankAttachment(int uid, int rankId, long attachmentId) {

        return download(uid, SubrecordAttachmentTarget.RANK, rankId, "", attachmentId, false);

    }

    public SubrecordAttachmentPreview previewRankAttachment(int uid, int rankId, long attachmentId) {

        return preview(uid, SubrecordAttachmentTarget.RANK, rankId, "", attachmentId, false);

    }

    public String downloadRankFileName(int uid, int rankId, long attachmentId) {

        return fileName(uid, SubrecordAttachmentTarget.RANK, rankId, "", attachmentId, false);

    }

    public String previewRankContentType(int uid, int rankId, long attachmentId) {

        return previewContentType(uid, SubrecordAttachmentTarget.RANK, rankId, "", attachmentId, false);

    }

    public void deleteRankAttachment(int uid, int rankId, long attachmentId) {

        delete(uid, SubrecordAttachmentTarget.RANK, rankId, "", attachmentId, false);

    }

    public List<SubrecordAttachmentRecord> listTransferAttachments(int uid, long transferId) {

        AttachmentAccess access = resolveAccess(uid, SubrecordAttachmentTarget.TRANSFER, Math.toIntExact(transferId), "", false, false);

        return attachmentRepository.findByKey(access.key());

    }

    public SubrecordAttachmentRecord uploadTransferAttachment(int uid, long transferId, MultipartFile file) {

        return upload(uid, SubrecordAttachmentTarget.TRANSFER, Math.toIntExact(transferId), "", file, true);

    }

    public Resource downloadTransferAttachment(int uid, long transferId, long attachmentId) {

        return download(uid, SubrecordAttachmentTarget.TRANSFER, Math.toIntExact(transferId), "", attachmentId, true);

    }

    public SubrecordAttachmentPreview previewTransferAttachment(int uid, long transferId, long attachmentId) {

        return preview(uid, SubrecordAttachmentTarget.TRANSFER, Math.toIntExact(transferId), "", attachmentId, true);

    }

    public String downloadTransferFileName(int uid, long transferId, long attachmentId) {

        return fileName(uid, SubrecordAttachmentTarget.TRANSFER, Math.toIntExact(transferId), "", attachmentId, true);

    }

    public String previewTransferContentType(int uid, long transferId, long attachmentId) {

        return previewContentType(uid, SubrecordAttachmentTarget.TRANSFER, Math.toIntExact(transferId), "", attachmentId, true);

    }

    public void deleteTransferAttachment(int uid, long transferId, long attachmentId) {

        delete(uid, SubrecordAttachmentTarget.TRANSFER, Math.toIntExact(transferId), "", attachmentId, true);

    }

    public List<SubrecordAttachmentRecord> listPayrollAttachments(int uid, String historyId) {

        AttachmentAccess access = resolveAccess(uid, SubrecordAttachmentTarget.PAYROLL, 0, historyId, false, true);

        return attachmentRepository.findByKey(access.key());

    }

    public SubrecordAttachmentRecord uploadPayrollAttachment(int uid, String historyId, MultipartFile file) {

        return upload(uid, SubrecordAttachmentTarget.PAYROLL, 0, historyId, file, true);

    }

    public Resource downloadPayrollAttachment(int uid, String historyId, long attachmentId) {

        return download(uid, SubrecordAttachmentTarget.PAYROLL, 0, historyId, attachmentId, true);

    }

    public SubrecordAttachmentPreview previewPayrollAttachment(int uid, String historyId, long attachmentId) {

        return preview(uid, SubrecordAttachmentTarget.PAYROLL, 0, historyId, attachmentId, true);

    }

    public String downloadPayrollFileName(int uid, String historyId, long attachmentId) {

        return fileName(uid, SubrecordAttachmentTarget.PAYROLL, 0, historyId, attachmentId, true);

    }

    public String previewPayrollContentType(int uid, String historyId, long attachmentId) {

        return previewContentType(uid, SubrecordAttachmentTarget.PAYROLL, 0, historyId, attachmentId, true);

    }

    public void deletePayrollAttachment(int uid, String historyId, long attachmentId) {

        delete(uid, SubrecordAttachmentTarget.PAYROLL, 0, historyId, attachmentId, true);

    }

    public void deleteAllForPosition(int positionId) {

        deleteAllForKey(SubrecordAttachmentTarget.POSITION.keyForInt(positionId));

    }

    public void deleteAllForEducation(int educationId) {

        deleteAllForKey(SubrecordAttachmentTarget.EDUCATION.keyForInt(educationId));

    }

    public void deleteAllForAssessment(int assessmentId) {

        deleteAllForKey(SubrecordAttachmentTarget.ASSESSMENT.keyForInt(assessmentId));

    }

    public void deleteAllForAward(int awardId) {

        deleteAllForKey(SubrecordAttachmentTarget.AWARD.keyForInt(awardId));

    }

    public void deleteAllForRank(int rankId) {

        deleteAllForKey(SubrecordAttachmentTarget.RANK.keyForInt(rankId));

    }

    public void deleteAllForTransfer(long transferId) {

        deleteAllForKey(SubrecordAttachmentTarget.TRANSFER.keyForInt(Math.toIntExact(transferId)));

    }

    public void deleteAllForMain(int uid) {

        deleteAllForKey(SubrecordAttachmentTarget.MAIN.keyForInt(uid));

    }

    public void deleteAllForPayroll(String historyId) {

        deleteAllForKey(SubrecordAttachmentTarget.PAYROLL.keyForString(historyId));

    }

    private SubrecordAttachmentRecord upload(

            int uid,

            SubrecordAttachmentTarget target,

            int recordId,

            String recordKey,

            MultipartFile file,

            boolean payrollWrite) {

        AttachmentAccess access = resolveAccess(uid, target, recordId, recordKey, true, payrollWrite);

        try {

            SubrecordAttachmentStorage.StoredFile stored = attachmentStorage.store(file);

            long id = attachmentRepository.insert(

                    access.key(),

                    stored.originalName(),

                    stored.storedName(),

                    stored.contentType(),

                    stored.fileSize(),

                    currentUsername());

            return attachmentRepository.findById(id)

                    .orElseThrow(() -> new NotFoundException("Attachment not found after upload: " + id));

        } catch (IOException exception) {

            throw new IllegalStateException("附件保存失败：" + exception.getMessage(), exception);

        }

    }

    private Resource download(

            int uid,

            SubrecordAttachmentTarget target,

            int recordId,

            String recordKey,

            long attachmentId,

            boolean payrollWrite) {

        AttachmentAccess access = resolveAccess(uid, target, recordId, recordKey, false, payrollWrite);

        SubrecordAttachmentRecord attachment = requireAttachment(access.key(), attachmentId);

        return attachmentStorage.load(attachmentRepository.findStoredName(attachment.id()).orElse(""));

    }

    private SubrecordAttachmentPreview preview(

            int uid,

            SubrecordAttachmentTarget target,

            int recordId,

            String recordKey,

            long attachmentId,

            boolean payrollWrite) {

        AttachmentAccess access = resolveAccess(uid, target, recordId, recordKey, false, payrollWrite);

        SubrecordAttachmentRecord attachment = requireAttachment(access.key(), attachmentId);

        String storedName = attachmentRepository.findStoredName(attachment.id()).orElse("");

        try {

            SubrecordAttachmentStorage.PreviewPayload payload = attachmentStorage.loadForPreview(

                    storedName, attachment.originalName(), attachment.contentType());

            return new SubrecordAttachmentPreview(payload.resource(), attachment.originalName(), payload.contentType());

        } catch (IOException exception) {

            throw new IllegalStateException("附件预览加载失败：" + exception.getMessage(), exception);

        }

    }

    private String fileName(

            int uid,

            SubrecordAttachmentTarget target,

            int recordId,

            String recordKey,

            long attachmentId,

            boolean payrollWrite) {

        AttachmentAccess access = resolveAccess(uid, target, recordId, recordKey, false, payrollWrite);

        return requireAttachment(access.key(), attachmentId).originalName();

    }

    private String previewContentType(

            int uid,

            SubrecordAttachmentTarget target,

            int recordId,

            String recordKey,

            long attachmentId,

            boolean payrollWrite) {

        AttachmentAccess access = resolveAccess(uid, target, recordId, recordKey, false, payrollWrite);

        SubrecordAttachmentRecord attachment = requireAttachment(access.key(), attachmentId);

        return SubrecordAttachmentStorage.resolveContentType(attachment.originalName(), attachment.contentType());

    }

    private void delete(

            int uid,

            SubrecordAttachmentTarget target,

            int recordId,

            String recordKey,

            long attachmentId,

            boolean payrollWrite) {

        AttachmentAccess access = resolveAccess(uid, target, recordId, recordKey, true, payrollWrite);

        SubrecordAttachmentRecord attachment = requireAttachment(access.key(), attachmentId);

        String storedName = attachmentRepository.findStoredName(attachment.id()).orElse("");

        attachmentRepository.deleteById(attachment.id());

        deleteStoredQuietly(storedName);

    }

    private void deleteAllForKey(SubrecordAttachmentKey key) {

        for (String storedName : attachmentRepository.deleteByKey(key)) {

            deleteStoredQuietly(storedName);

        }

    }

    private AttachmentAccess resolveAccess(

            int uid,

            SubrecordAttachmentTarget target,

            int recordId,

            String recordKey,

            boolean requireWrite,

            boolean payrollWrite) {

        PersonKey personKey = personnelRepository.findByUid(uid)

                .map(detail -> new PersonKey(detail.organizationCode(), detail.personCode()))

                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));

        String approvalStatus = null;

        String organizationCode = personKey.organizationCode();

        SubrecordAttachmentKey key = target == SubrecordAttachmentTarget.PAYROLL

                ? target.keyForString(recordKey)

                : target.keyForInt(recordId);

        switch (target) {

            case MAIN -> {

                PersonnelMaintenanceRecord person = personnelRepository.findMaintenanceByUid(uid)

                        .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));

                approvalStatus = person.approvalStatus();

                organizationCode = person.organizationCode();

            }

            case EDUCATION -> {

                PersonKey recordPersonKey = requireSamePerson(uid, personKey, personnelRepository.findEducationKeyById(recordId), "Education record not found: " + recordId);

                approvalStatus = personnelRepository.findEducationById(recordId)

                        .orElseThrow(() -> new NotFoundException("Education record not found: " + recordId))

                        .approvalStatus();

                organizationCode = recordPersonKey.organizationCode();

            }

            case POSITION -> {

                PersonKey recordPersonKey = requireSamePerson(uid, personKey, personnelRepository.findPositionKeyById(recordId), "Position record not found: " + recordId);

                approvalStatus = personnelRepository.findPositionById(recordId)

                        .orElseThrow(() -> new NotFoundException("Position record not found: " + recordId))

                        .approvalStatus();

                organizationCode = recordPersonKey.organizationCode();

            }

            case ASSESSMENT -> {

                PersonKey recordPersonKey = requireSamePerson(uid, personKey, personnelRepository.findAssessmentKeyById(recordId), "Assessment record not found: " + recordId);

                approvalStatus = personnelRepository.findAssessmentById(recordId)

                        .orElseThrow(() -> new NotFoundException("Assessment record not found: " + recordId))

                        .approvalStatus();

                organizationCode = recordPersonKey.organizationCode();

            }

            case AWARD -> {

                PersonKey recordPersonKey = requireSamePerson(uid, personKey, personnelRepository.findAwardKeyById(recordId), "Award record not found: " + recordId);

                approvalStatus = personnelRepository.findAwardById(recordId)

                        .orElseThrow(() -> new NotFoundException("Award record not found: " + recordId))

                        .approvalStatus();

                organizationCode = recordPersonKey.organizationCode();

            }

            case RANK -> {

                PersonKey recordPersonKey = requireSamePerson(uid, personKey, personnelRepository.findRankKeyById(recordId), "Rank record not found: " + recordId);

                approvalStatus = personnelRepository.findRankById(recordId)

                        .orElseThrow(() -> new NotFoundException("Rank record not found: " + recordId))

                        .approvalStatus();

                organizationCode = recordPersonKey.organizationCode();

            }

            case TRANSFER -> {

                PersonnelTransferRecord transfer = personnelRepository.findTransferById(recordId)

                        .orElseThrow(() -> new NotFoundException("Transfer record not found: " + recordId));

                if (transfer.personUid() != uid) {

                    throw new AccessDeniedException("Record does not belong to the selected personnel");

                }

            }

            case PAYROLL -> {

                var history = payrollRepository.findPayrollHistoryById(recordKey)

                        .orElseThrow(() -> new NotFoundException("Payroll history not found: " + recordKey));

                PersonKey recordPersonKey = new PersonKey(history.organizationCode(), history.personCode());

                requireSamePerson(uid, personKey, java.util.Optional.of(recordPersonKey), "Payroll history not found: " + recordKey);

                organizationCode = history.organizationCode();

            }

            default -> throw new IllegalStateException("Unsupported attachment target: " + target);

        }

        accessControlService.requireOrganization(organizationCode);

        if (requireWrite) {

            if (payrollWrite) {

                requirePayrollWritePermission();

            } else if (!accessControlService.hasPermission(PersonnelFeaturePermissions.LEGACY_WRITE)) {

                throw new AccessDeniedException(PersonnelFeaturePermissions.LEGACY_WRITE + " permission required");

            }

            if (approvalStatus != null) {

                personnelSubrecordEditPolicy.assertEditable(approvalStatus);

            }

        } else if (!accessControlService.hasAnyPermission(

                PersonnelFeaturePermissions.BASIC_READ,

                PersonnelFeaturePermissions.LEGACY_READ,

                PersonnelFeaturePermissions.LEGACY_WRITE)) {

            throw new AccessDeniedException(PersonnelFeaturePermissions.BASIC_READ + " permission required");

        }

        return new AttachmentAccess(key, organizationCode, approvalStatus);

    }

    private PersonKey requireSamePerson(

            int uid,

            PersonKey personKey,

            java.util.Optional<PersonKey> recordKey,

            String notFoundMessage) {

        PersonKey resolved = recordKey.orElseThrow(() -> new NotFoundException(notFoundMessage));

        if (!personKey.organizationCode().equals(resolved.organizationCode())

                || !personKey.personCode().equals(resolved.personCode())) {

            throw new AccessDeniedException("Record does not belong to the selected personnel");

        }

        return resolved;

    }

    private void requirePayrollWritePermission() {

        if (!accessControlService.hasPermission("PAYROLL_WRITE")) {

            throw new AccessDeniedException("PAYROLL_WRITE permission required");

        }

    }

    private SubrecordAttachmentRecord requireAttachment(SubrecordAttachmentKey key, long attachmentId) {

        SubrecordAttachmentRecord attachment = attachmentRepository.findById(attachmentId)

                .orElseThrow(() -> new NotFoundException("Attachment not found: " + attachmentId));

        if (!key.tableName().equals(attachment.tableName())

                || key.recordId() != attachment.recordId()

                || !key.recordKey().equals(attachment.recordKey() == null ? "" : attachment.recordKey())) {

            throw new NotFoundException("Attachment not found: " + attachmentId);

        }

        return attachment;

    }

    private void deleteStoredQuietly(String storedName) {

        if (storedName == null || storedName.isBlank()) {

            return;

        }

        try {

            attachmentStorage.delete(storedName);

        } catch (IOException ignored) {

            // File may already be removed; metadata deletion is authoritative.

        }

    }

    private static String currentUsername() {

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication == null || authentication.getName() == null ? "" : authentication.getName();

    }

    private record AttachmentAccess(SubrecordAttachmentKey key, String organizationCode, String approvalStatus) {

    }

}

