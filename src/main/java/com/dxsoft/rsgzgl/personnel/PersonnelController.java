package com.dxsoft.rsgzgl.personnel;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/{uid}/positions")
    List<PositionRecord> positions(@PathVariable int uid) {
        return personnelService.positions(uid);
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

    @GetMapping("/assessments")
    PageResponse<AnnualAssessmentRecord> annualAssessments(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return personnelService.annualAssessments(organizationCode, year, keyword, PageRequest.of(page, size));
    }
}
