package com.dxsoft.rsgzgl.personnel;

import com.dxsoft.rsgzgl.common.NotFoundException;
import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class PersonnelService {

    private final PersonnelRepository personnelRepository;
    private final AccessControlService accessControlService;

    public PersonnelService(PersonnelRepository personnelRepository, AccessControlService accessControlService) {
        this.personnelRepository = personnelRepository;
        this.accessControlService = accessControlService;
    }

    public PageResponse<PersonnelSummary> list(String organizationCode, String keyword, PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        List<PersonnelSummary> rows = personnelRepository.findAll(scope, emptyToNull(organizationCode), keyword, pageRequest);
        long total = personnelRepository.countAll(scope, emptyToNull(organizationCode), keyword);
        return PageResponse.of(rows, pageRequest, total);
    }

    public PersonnelDetail get(int uid) {
        PersonnelDetail detail = personnelRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(detail.organizationCode());
        return detail;
    }

    public PersonnelMaintenanceRecord maintenance(int uid) {
        PersonnelMaintenanceRecord record = personnelRepository.findMaintenanceByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(record.organizationCode());
        requireWritePermission();
        return record;
    }

    public PersonnelMaintenanceRecord create(PersonnelMaintenanceRequest request) {
        requireWritePermission();
        accessControlService.requireOrganization(requiredOrganizationCode(request));
        int uid = personnelRepository.createPersonnel(request);
        return maintenance(uid);
    }

    public PersonnelMaintenanceRecord update(int uid, PersonnelMaintenanceRequest request) {
        requireWritePermission();
        PersonnelMaintenanceRecord existing = personnelRepository.findMaintenanceByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(existing.organizationCode());
        accessControlService.requireOrganization(requiredOrganizationCode(request));
        personnelRepository.updatePersonnel(uid, request);
        return maintenance(uid);
    }

    public void delete(int uid) {
        requireWritePermission();
        PersonnelMaintenanceRecord existing = personnelRepository.findMaintenanceByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(existing.organizationCode());
        personnelRepository.deletePersonnel(uid);
    }

    public List<PositionRecord> positions(int uid) {
        PersonKey personKey = getPersonKey(uid);
        return personnelRepository.findPositions(personKey);
    }

    public PageResponse<PersonnelPositionHistoryRecord> positionHistories(
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        return PageResponse.of(
                personnelRepository.findPositionHistories(scope, emptyToNull(organizationCode), keyword, pageRequest),
                pageRequest,
                personnelRepository.countPositionHistories(scope, emptyToNull(organizationCode), keyword));
    }

    public List<EducationRecord> education(int uid) {
        PersonKey personKey = getPersonKey(uid);
        return personnelRepository.findEducation(personKey);
    }

    public List<EducationRecord> createEducation(int uid, EducationMaintenanceRequest request) {
        PersonKey key = getPersonKey(uid);
        requireWritePermission();
        int id = personnelRepository.createEducation(key, request);
        return personnelRepository.findEducation(key);
    }

    public List<EducationRecord> updateEducation(int uid, int id, EducationMaintenanceRequest request) {
        PersonKey key = requireSubrecordOrganization(personnelRepository.findEducationKeyById(id), "Education record not found: " + id);
        PersonKey personKey = getPersonKey(uid);
        requireSamePerson(key, personKey);
        requireWritePermission();
        personnelRepository.updateEducation(id, request);
        return personnelRepository.findEducation(personKey);
    }

    public void deleteEducation(int uid, int id) {
        PersonKey key = requireSubrecordOrganization(personnelRepository.findEducationKeyById(id), "Education record not found: " + id);
        requireSamePerson(key, getPersonKey(uid));
        requireWritePermission();
        personnelRepository.deleteEducation(id);
    }

    public PageResponse<PersonnelEducationHistoryRecord> educationHistories(
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        return PageResponse.of(
                personnelRepository.findEducationHistories(scope, emptyToNull(organizationCode), keyword, pageRequest),
                pageRequest,
                personnelRepository.countEducationHistories(scope, emptyToNull(organizationCode), keyword));
    }

    public List<AssessmentRecord> assessments(int uid) {
        PersonKey personKey = getPersonKey(uid);
        return personnelRepository.findAssessments(personKey);
    }

    public List<PositionRecord> createPosition(int uid, PositionMaintenanceRequest request) {
        PersonKey key = getPersonKey(uid);
        requireWritePermission();
        personnelRepository.createPosition(key, request);
        return personnelRepository.findPositions(key);
    }

    public List<PositionRecord> updatePosition(int uid, int id, PositionMaintenanceRequest request) {
        PersonKey key = requireSubrecordOrganization(personnelRepository.findPositionKeyById(id), "Position record not found: " + id);
        PersonKey personKey = getPersonKey(uid);
        requireSamePerson(key, personKey);
        requireWritePermission();
        personnelRepository.updatePosition(id, request);
        return personnelRepository.findPositions(personKey);
    }

    public void deletePosition(int uid, int id) {
        PersonKey key = requireSubrecordOrganization(personnelRepository.findPositionKeyById(id), "Position record not found: " + id);
        requireSamePerson(key, getPersonKey(uid));
        requireWritePermission();
        personnelRepository.deletePosition(id);
    }

    public List<AssessmentRecord> createAssessment(int uid, AssessmentMaintenanceRequest request) {
        PersonKey key = getPersonKey(uid);
        requireWritePermission();
        personnelRepository.createAssessment(key, request);
        return personnelRepository.findAssessments(key);
    }

    public List<AssessmentRecord> updateAssessment(int uid, int id, AssessmentMaintenanceRequest request) {
        PersonKey key = requireSubrecordOrganization(personnelRepository.findAssessmentKeyById(id), "Assessment record not found: " + id);
        PersonKey personKey = getPersonKey(uid);
        requireSamePerson(key, personKey);
        requireWritePermission();
        personnelRepository.updateAssessment(id, request);
        return personnelRepository.findAssessments(personKey);
    }

    public void deleteAssessment(int uid, int id) {
        PersonKey key = requireSubrecordOrganization(personnelRepository.findAssessmentKeyById(id), "Assessment record not found: " + id);
        requireSamePerson(key, getPersonKey(uid));
        requireWritePermission();
        personnelRepository.deleteAssessment(id);
    }

    public PageResponse<AnnualAssessmentRecord> annualAssessments(
            String organizationCode,
            String year,
            String keyword,
            PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        return PageResponse.of(
                personnelRepository.findAnnualAssessments(scope, emptyToNull(organizationCode), year, keyword, pageRequest),
                pageRequest,
                personnelRepository.countAnnualAssessments(scope, emptyToNull(organizationCode), year, keyword));
    }

    public PageResponse<AnnualAssessmentSummaryRecord> annualAssessmentSummary(
            String organizationCode,
            String year,
            String result,
            PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        return PageResponse.of(
                personnelRepository.findAnnualAssessmentSummary(scope, emptyToNull(organizationCode), year, result, pageRequest),
                pageRequest,
                personnelRepository.countAnnualAssessmentSummary(scope, emptyToNull(organizationCode), year, result));
    }

    public PageResponse<ChangedPersonnelRecord> changedPersonnel(
            String organizationCode,
            String period,
            String keyword,
            PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        return PageResponse.of(
                personnelRepository.findChangedPersonnel(scope, emptyToNull(organizationCode), period, keyword, pageRequest),
                pageRequest,
                personnelRepository.countChangedPersonnel(scope, emptyToNull(organizationCode), period, keyword));
    }

    private PersonKey getPersonKey(int uid) {
        PersonKey key = personnelRepository.findKeyByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(key.organizationCode());
        return key;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void requireWritePermission() {
        if (!accessControlService.hasPermission("PERSONNEL_WRITE")) {
            throw new AccessDeniedException("PERSONNEL_WRITE permission required");
        }
    }

    private PersonKey requireSubrecordOrganization(Optional<PersonKey> optionalKey, String message) {
        PersonKey key = optionalKey.orElseThrow(() -> new NotFoundException(message));
        accessControlService.requireOrganization(key.organizationCode());
        return key;
    }

    private void requireSamePerson(PersonKey recordKey, PersonKey personKey) {
        if (!recordKey.organizationCode().equals(personKey.organizationCode()) || !recordKey.personCode().equals(personKey.personCode())) {
            throw new AccessDeniedException("Record does not belong to the selected personnel");
        }
    }

    private String requiredOrganizationCode(PersonnelMaintenanceRequest request) {
        String organizationCode = emptyToNull(request.organizationCode());
        if (organizationCode == null) {
            throw new IllegalArgumentException("单位编码不能为空");
        }
        return organizationCode;
    }
}
