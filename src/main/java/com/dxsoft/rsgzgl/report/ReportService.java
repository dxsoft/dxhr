package com.dxsoft.rsgzgl.report;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.payroll.PayrollChangeComparison;
import com.dxsoft.rsgzgl.payroll.PayrollService;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
class ReportService {

    private final ReportRepository reportRepository;
    private final PayrollService payrollService;
    private final AccessControlService accessControlService;

    ReportService(ReportRepository reportRepository, PayrollService payrollService, AccessControlService accessControlService) {
        this.reportRepository = reportRepository;
        this.payrollService = payrollService;
        this.accessControlService = accessControlService;
    }

    PageResponse<ReportTypeOption> reportTypes(String category, PageRequest pageRequest) {
        return PageResponse.of(
                reportRepository.findReportTypes(category, pageRequest),
                pageRequest,
                reportRepository.countReportTypes(category));
    }

    PageResponse<PayrollChangeRegisterRow> payrollChangeCandidates(
            String organizationFilter,
            String reportTypeCode,
            String year,
            String keyword,
            PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        return PageResponse.of(
                reportRepository.findPayrollChangeCandidates(scope, organizationFilter, reportTypeCode, year, keyword, pageRequest),
                pageRequest,
                reportRepository.countPayrollChangeCandidates(scope, organizationFilter, reportTypeCode, year, keyword));
    }

    PageResponse<PayrollChangeRegisterRow> payrollChangeRegister(
            String organizationFilter,
            String period,
            String keyword,
            PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        return PageResponse.of(
                reportRepository.findPayrollChangeRegister(scope, organizationFilter, period, keyword, pageRequest),
                pageRequest,
                reportRepository.countPayrollChangeRegister(scope, organizationFilter, period, keyword));
    }

    PayrollChangeComparison payrollChangeApproval(String payrollHistoryId) {
        return payrollService.payrollChangeComparison(payrollHistoryId);
    }
}
