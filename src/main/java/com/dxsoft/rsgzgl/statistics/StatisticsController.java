package com.dxsoft.rsgzgl.statistics;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
class StatisticsController {

    private final StatisticsService statisticsService;

    StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/personnel-summary")
    PersonnelSummaryStatistics personnelSummary(@RequestParam(required = false) String organizationCode) {
        return statisticsService.personnelSummary(organizationCode);
    }

    @GetMapping("/payroll-change-summary")
    List<PayrollChangeSummaryStatistics> payrollChangeSummary(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) List<String> changeTypes) {
        return statisticsService.payrollChangeSummary(organizationCode, year, month, changeTypes);
    }

    @GetMapping("/payroll-change-types")
    List<String> payrollChangeTypes(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String month) {
        return statisticsService.payrollChangeTypes(organizationCode, year, month);
    }

    @GetMapping("/retirement-due-personnel")
    PageResponse<RetirementDuePersonnel> retirementDuePersonnel(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String referencePeriod,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return statisticsService.retirementDuePersonnel(
                organizationCode, keyword, referencePeriod, PageRequest.of(page, size));
    }
}
