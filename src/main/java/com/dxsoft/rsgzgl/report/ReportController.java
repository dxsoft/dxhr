package com.dxsoft.rsgzgl.report;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.payroll.PayrollChangeComparison;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
class ReportController {

    private final ReportService reportService;

    ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/types")
    PageResponse<ReportTypeOption> reportTypes(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return reportService.reportTypes(category, PageRequest.of(page, size));
    }

    @GetMapping("/payroll-change-candidates")
    PageResponse<PayrollChangeRegisterRow> payrollChangeCandidates(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String reportTypeCode,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return reportService.payrollChangeCandidates(organizationCode, reportTypeCode, year, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/payroll-change-register")
    PageResponse<PayrollChangeRegisterRow> payrollChangeRegister(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return reportService.payrollChangeRegister(organizationCode, period, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/payroll-change-approval")
    PayrollChangeComparison payrollChangeApproval(@RequestParam String payrollHistoryId) {
        return reportService.payrollChangeApproval(payrollHistoryId);
    }
}
