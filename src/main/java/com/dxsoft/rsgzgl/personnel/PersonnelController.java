package com.dxsoft.rsgzgl.personnel;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import java.util.List;
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

    PersonnelController(PersonnelService personnelService) {
        this.personnelService = personnelService;
    }

    @GetMapping
    PageResponse<PersonnelSummary> list(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return personnelService.list(organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/{uid}")
    PersonnelDetail get(@PathVariable int uid) {
        return personnelService.get(uid);
    }

    @GetMapping("/{uid}/maintenance")
    PersonnelMaintenanceRecord maintenance(@PathVariable int uid) {
        return personnelService.maintenance(uid);
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

    @DeleteMapping("/{uid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable int uid) {
        personnelService.delete(uid);
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

    @GetMapping("/positions")
    PageResponse<PersonnelPositionHistoryRecord> positionHistories(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return personnelService.positionHistories(organizationCode, keyword, PageRequest.of(page, size));
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

    @GetMapping("/education")
    PageResponse<PersonnelEducationHistoryRecord> educationHistories(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return personnelService.educationHistories(organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/{uid}/assessments")
    List<AssessmentRecord> assessments(@PathVariable int uid) {
        return personnelService.assessments(uid);
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
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return personnelService.annualAssessmentSummary(organizationCode, year, result, PageRequest.of(page, size));
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
}
