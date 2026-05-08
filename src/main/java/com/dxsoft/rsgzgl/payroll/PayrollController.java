package com.dxsoft.rsgzgl.payroll;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payroll")
class PayrollController {

    private final PayrollService payrollService;

    PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping("/fields")
    PageResponse<PayrollFieldMetadata> fields(
            @RequestParam(required = false) Boolean enabledIn2006Policy,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.fields(enabledIn2006Policy, PageRequest.of(page, size));
    }

    @GetMapping("/position-standards")
    PageResponse<PositionSalaryStandard> positionStandards(
            @RequestParam(required = false) String standardYearMonth,
            @RequestParam(required = false) String positionCode,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.positionStandards(standardYearMonth, positionCode, PageRequest.of(page, size));
    }

    @GetMapping("/allowance-standards")
    PageResponse<AllowanceStandard> allowanceStandards(
            @RequestParam(required = false) String standardYearMonth,
            @RequestParam(required = false) String item,
            @RequestParam(required = false) String positionCode,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.allowanceStandards(standardYearMonth, item, positionCode, PageRequest.of(page, size));
    }

    @GetMapping("/personnel/{uid}/calculation-context")
    PayrollCalculationContext calculationContext(@PathVariable int uid) {
        return payrollService.calculationContext(uid);
    }

    @GetMapping("/personnel/{uid}/calculation-preview")
    PayrollCalculationPreview calculationPreview(@PathVariable int uid) {
        return payrollService.calculationPreview(uid);
    }

    @GetMapping("/calculation-audits")
    PageResponse<PayrollCalculationAudit> calculationAudits(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.calculationAudits(organizationCode, PageRequest.of(page, size));
    }

    @GetMapping("/calculation-audit-summary")
    PayrollAuditSummary calculationAuditSummary(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.auditSummary(organizationCode, PageRequest.of(page, size));
    }
}
