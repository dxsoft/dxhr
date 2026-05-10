package com.dxsoft.rsgzgl.payroll;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
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

    @GetMapping("/basic-standards")
    PageResponse<BasicStandardRecord> basicStandards(
            @RequestParam String standardType,
            @RequestParam(required = false) String standardYearMonth,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.basicStandards(standardType, standardYearMonth, code, PageRequest.of(page, size));
    }

    @GetMapping("/rank-allowance-standards")
    PageResponse<RankAllowanceStandard> rankAllowanceStandards(
            @RequestParam(required = false) String standardYearMonth,
            @RequestParam(required = false) String rankName,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.rankAllowanceStandards(standardYearMonth, rankName, category, PageRequest.of(page, size));
    }

    @GetMapping("/retained-allowance-standards")
    PageResponse<RetainedAllowanceStandard> retainedAllowanceStandards(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.retainedAllowanceStandards(keyword, PageRequest.of(page, size));
    }

    @GetMapping("/year-allowance-standards")
    PageResponse<YearAllowanceStandard> yearAllowanceStandards(
            @RequestParam(required = false) String standardYearMonth,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.yearAllowanceStandards(standardYearMonth, PageRequest.of(page, size));
    }

    @GetMapping("/intern-salary-standards")
    PageResponse<InternSalaryStandard> internSalaryStandards(
            @RequestParam(required = false) String standardYearMonth,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.internSalaryStandards(standardYearMonth, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/wage-reform-standards")
    PageResponse<WageReformStandard> wageReformStandards(
            @RequestParam(required = false) String positionCode,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.wageReformStandards(positionCode, PageRequest.of(page, size));
    }

    @GetMapping("/other-allowance-standards")
    PageResponse<OtherAllowanceStandard> otherAllowanceStandards(
            @RequestParam String standardType,
            @RequestParam(required = false) String standardYearMonth,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.otherAllowanceStandards(standardType, standardYearMonth, code, PageRequest.of(page, size));
    }

    @GetMapping("/histories")
    PageResponse<PayrollHistoryRecord> payrollHistories(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.payrollHistories(organizationCode, period, keyword, PageRequest.of(page, size));
    }

    @PostMapping("/personnel/{uid}/histories")
    PageResponse<PayrollHistoryRecord> createPayrollHistory(
            @PathVariable int uid,
            @RequestBody PayrollHistoryMaintenanceRequest request) {
        return payrollService.createPayrollHistory(uid, request);
    }

    @PutMapping("/histories/{id}")
    PageResponse<PayrollHistoryRecord> updatePayrollHistory(
            @PathVariable String id,
            @RequestBody PayrollHistoryMaintenanceRequest request) {
        return payrollService.updatePayrollHistory(id, request);
    }

    @DeleteMapping("/histories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deletePayrollHistory(@PathVariable String id) {
        payrollService.deletePayrollHistory(id);
    }

    @GetMapping("/teaching-allowance-adjustments")
    PageResponse<TeachingAllowanceAdjustment> teachingAllowanceAdjustments(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.teachingAllowanceAdjustments(organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/normal-promotions")
    PageResponse<NormalPromotionPreview> normalPromotions(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.normalPromotionPreviews(organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/level-promotions")
    PageResponse<LevelPromotionPreview> levelPromotions(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean dueOnly,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.levelPromotionPreviews(organizationCode, keyword, dueOnly, PageRequest.of(page, size));
    }

    @GetMapping("/position-change-promotions")
    PageResponse<PositionChangePromotionPreview> positionChangePromotions(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.positionChangePromotionPreviews(organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/education-promotions")
    PageResponse<EducationPromotionPreview> educationPromotions(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.educationPromotionPreviews(organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/regularizations")
    PageResponse<RegularizationPreview> regularizations(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.regularizationPreviews(organizationCode, keyword, PageRequest.of(page, size));
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
