package com.dxsoft.rsgzgl.personnel;

import com.dxsoft.rsgzgl.common.NotFoundException;
import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.util.List;
import java.util.Optional;
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
        OrganizationScope scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        List<PersonnelSummary> rows = personnelRepository.findAll(scope, keyword, pageRequest);
        long total = personnelRepository.countAll(scope, keyword);
        return PageResponse.of(rows, pageRequest, total);
    }

    public PersonnelDetail get(int uid) {
        PersonnelDetail detail = personnelRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
        accessControlService.requireOrganization(detail.organizationCode());
        return detail;
    }

    public List<PositionRecord> positions(int uid) {
        PersonKey personKey = getPersonKey(uid);
        return personnelRepository.findPositions(personKey);
    }

    public PageResponse<PersonnelPositionHistoryRecord> positionHistories(
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        return PageResponse.of(
                personnelRepository.findPositionHistories(scope, emptyToNull(organizationCode), keyword, pageRequest),
                pageRequest,
                personnelRepository.countPositionHistories(scope, emptyToNull(organizationCode), keyword));
    }

    public List<EducationRecord> education(int uid) {
        PersonKey personKey = getPersonKey(uid);
        return personnelRepository.findEducation(personKey);
    }

    public List<AssessmentRecord> assessments(int uid) {
        PersonKey personKey = getPersonKey(uid);
        return personnelRepository.findAssessments(personKey);
    }

    public PageResponse<AnnualAssessmentRecord> annualAssessments(
            String organizationCode,
            String year,
            String keyword,
            PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        return PageResponse.of(
                personnelRepository.findAnnualAssessments(scope, emptyToNull(organizationCode), year, keyword, pageRequest),
                pageRequest,
                personnelRepository.countAnnualAssessments(scope, emptyToNull(organizationCode), year, keyword));
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
}
