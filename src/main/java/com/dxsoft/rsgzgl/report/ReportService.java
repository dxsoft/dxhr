package com.dxsoft.rsgzgl.report;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
class ReportService {

    private final ReportRepository reportRepository;
    private final AccessControlService accessControlService;

    ReportService(ReportRepository reportRepository, AccessControlService accessControlService) {
        this.reportRepository = reportRepository;
        this.accessControlService = accessControlService;
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
}
