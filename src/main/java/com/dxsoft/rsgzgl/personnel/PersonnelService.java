package com.dxsoft.rsgzgl.personnel;

import com.dxsoft.rsgzgl.common.NotFoundException;
import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PersonnelService {

    private final PersonnelRepository personnelRepository;

    public PersonnelService(PersonnelRepository personnelRepository) {
        this.personnelRepository = personnelRepository;
    }

    public PageResponse<PersonnelSummary> list(String organizationCode, String keyword, PageRequest pageRequest) {
        List<PersonnelSummary> rows = personnelRepository.findAll(organizationCode, keyword, pageRequest);
        long total = personnelRepository.countAll(organizationCode, keyword);
        return PageResponse.of(rows, pageRequest, total);
    }

    public PersonnelDetail get(int uid) {
        return personnelRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
    }

    public List<PositionRecord> positions(int uid) {
        PersonKey personKey = getPersonKey(uid);
        return personnelRepository.findPositions(personKey);
    }

    public List<EducationRecord> education(int uid) {
        PersonKey personKey = getPersonKey(uid);
        return personnelRepository.findEducation(personKey);
    }

    public List<AssessmentRecord> assessments(int uid) {
        PersonKey personKey = getPersonKey(uid);
        return personnelRepository.findAssessments(personKey);
    }

    private PersonKey getPersonKey(int uid) {
        return personnelRepository.findKeyByUid(uid)
                .orElseThrow(() -> new NotFoundException("Personnel record not found: " + uid));
    }
}
