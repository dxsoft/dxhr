package com.dxsoft.rsgzgl.payroll;

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
@RequestMapping("/api/payroll")
class PayrollController {

    private final PayrollService payrollService;
    private final PayrollProjectionAuditExportService projectionAuditExportService;

    PayrollController(
            PayrollService payrollService,
            PayrollProjectionAuditExportService projectionAuditExportService) {
        this.payrollService = payrollService;
        this.projectionAuditExportService = projectionAuditExportService;
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

    @GetMapping("/histories/{id}/change-comparison")
    PayrollChangeComparison payrollChangeComparison(@PathVariable String id) {
        return payrollService.payrollChangeComparison(id);
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

    @GetMapping("/prosecution-allowance-adjustments")
    PageResponse<RankAllowanceStandardAdjustment> prosecutionAllowanceAdjustments(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.prosecutionAllowanceAdjustments(organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/judicial-allowance-adjustments")
    PageResponse<RankAllowanceStandardAdjustment> judicialAllowanceAdjustments(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.judicialAllowanceAdjustments(organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/police-allowance-adjustments")
    PageResponse<RankAllowanceStandardAdjustment> policeAllowanceAdjustments(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.policeAllowanceAdjustments(organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/supervision-allowance-adjustments")
    PageResponse<RankAllowanceStandardAdjustment> supervisionAllowanceAdjustments(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.supervisionAllowanceAdjustments(organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/normal-promotions")
    PageResponse<NormalPromotionPreview> normalPromotions(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean dueOnly,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.normalPromotionPreviews(organizationCode, keyword, dueOnly, year, PageRequest.of(page, size));
    }

    @PostMapping("/normal-promotions/{payrollHistoryId}/apply")
    PromotionActionResult applyNormalPromotion(
            @PathVariable String payrollHistoryId,
            @RequestParam(required = false) String year) {
        return payrollService.applyNormalPromotion(payrollHistoryId, year);
    }

    @PostMapping("/normal-promotions/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackNormalPromotion(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackNormalPromotion(payrollHistoryId);
    }

    @GetMapping("/level-promotions")
    PageResponse<LevelPromotionPreview> levelPromotions(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) Boolean includeApply,
            @RequestParam(required = false) Boolean includeProcessed,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.levelPromotionPreviews(
                organizationCode, keyword, year, includeApply, includeProcessed, PageRequest.of(page, size));
    }

    @PostMapping("/level-promotions/{payrollHistoryId}/apply")
    PromotionActionResult applyLevelPromotion(
            @PathVariable String payrollHistoryId,
            @RequestParam(required = false) String year) {
        return payrollService.applyLevelPromotion(payrollHistoryId, year);
    }

    @PostMapping("/level-promotions/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackLevelPromotion(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackLevelPromotion(payrollHistoryId);
    }

    @GetMapping("/reform-level-rollings")
    PageResponse<ReformLevelRollingPreview> reformLevelRollings(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) Boolean includeApply,
            @RequestParam(required = false) Boolean includeProcessed,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.reformLevelRollingPreviews(
                organizationCode, keyword, year, includeApply, includeProcessed, PageRequest.of(page, size));
    }

    @PostMapping("/reform-level-rollings/{payrollHistoryId}/apply")
    PromotionActionResult applyReformLevelRolling(
            @PathVariable String payrollHistoryId,
            @RequestParam(required = false) String year) {
        return payrollService.applyReformLevelRolling(payrollHistoryId, year);
    }

    @PostMapping("/reform-level-rollings/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackReformLevelRolling(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackReformLevelRolling(payrollHistoryId);
    }

    @GetMapping("/regularization-high-grades")
    PageResponse<RegularizationHighGradePreview> regularizationHighGrades(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.regularizationHighGradePreviews(
                organizationCode, keyword, PageRequest.of(page, size));
    }

    @PostMapping("/regularization-high-grades/{payrollHistoryId}/apply")
    PromotionActionResult applyRegularizationHighGrade(@PathVariable String payrollHistoryId) {
        return payrollService.applyRegularizationHighGrade(payrollHistoryId);
    }

    @PostMapping("/regularization-high-grades/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackRegularizationHighGrade(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackRegularizationHighGrade(payrollHistoryId);
    }

    @GetMapping("/monthly-average-salaries")
    PageResponse<MonthlyAverageSalaryPreview> monthlyAverageSalaries(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.monthlyAverageSalaryPreviews(
                organizationCode, keyword, year, PageRequest.of(page, size));
    }

    @PostMapping("/monthly-average-salaries/{payrollHistoryId}/apply")
    PromotionActionResult applyMonthlyAverageSalary(
            @PathVariable String payrollHistoryId,
            @RequestParam(required = false) String year) {
        return payrollService.applyMonthlyAverageSalary(payrollHistoryId, year);
    }

    @PostMapping("/monthly-average-salaries/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackMonthlyAverageSalary(
            @PathVariable String payrollHistoryId,
            @RequestParam(required = false) String year) {
        return payrollService.rollbackMonthlyAverageSalary(payrollHistoryId, year);
    }

    @GetMapping("/wage-reforms-2006")
    PageResponse<WageReform2006Preview> wageReforms2006(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.wageReform2006Previews(
                organizationCode, keyword, PageRequest.of(page, size));
    }

    @PostMapping("/wage-reforms-2006/{uid}/apply")
    PromotionActionResult applyWageReform2006(@PathVariable int uid) {
        return payrollService.applyWageReform2006(uid);
    }

    @PostMapping("/wage-reforms-2006/{uid}/backfill-dtgxx")
    PromotionActionResult backfillWageReform2006Dtgxx(@PathVariable int uid) {
        return payrollService.backfillWageReform2006Dtgxx(uid);
    }

    @PostMapping("/wage-reforms-2006/{uid}/rollback-dtgxx")
    PromotionActionResult rollbackWageReform2006Dtgxx(@PathVariable int uid) {
        return payrollService.rollbackWageReform2006Dtgxx(uid);
    }

    @PostMapping("/wage-reforms-2006/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackWageReform2006(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackWageReform2006(payrollHistoryId);
    }

    @GetMapping("/position-change-promotions")
    PageResponse<PositionChangePromotionListItem> positionChangePromotions(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.positionChangePromotionPreviews(organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/position-change-promotions/{payrollHistoryId}")
    PositionChangePromotionPreview positionChangePromotionDetail(@PathVariable String payrollHistoryId) {
        return payrollService.positionChangePromotionDetail(payrollHistoryId);
    }

    @PostMapping("/position-change-promotions/{payrollHistoryId}/apply")
    PromotionActionResult applyPositionChangePromotion(
            @PathVariable String payrollHistoryId,
            @RequestBody(required = false) PositionChangeApplyRequest request) {
        return payrollService.applyPositionChangePromotion(payrollHistoryId, request);
    }

    @PostMapping("/position-change-promotions/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackPositionChangePromotion(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackPositionChangePromotion(payrollHistoryId);
    }

    @GetMapping("/education-promotions")
    PageResponse<EducationPromotionPreview> educationPromotions(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.educationPromotionPreviews(organizationCode, keyword, PageRequest.of(page, size));
    }

    @PostMapping("/education-promotions/{payrollHistoryId}/apply")
    PromotionActionResult applyEducationPromotion(@PathVariable String payrollHistoryId) {
        return payrollService.applyEducationPromotion(payrollHistoryId);
    }

    @PostMapping("/education-promotions/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackEducationPromotion(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackEducationPromotion(payrollHistoryId);
    }

    @GetMapping("/regularizations")
    PageResponse<RegularizationPreview> regularizations(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.regularizationPreviews(organizationCode, keyword, PageRequest.of(page, size));
    }

    @PostMapping("/regularizations/{payrollHistoryId}/apply")
    PromotionActionResult applyRegularization(@PathVariable String payrollHistoryId) {
        return payrollService.applyRegularization(payrollHistoryId);
    }

    @PostMapping("/regularizations/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackRegularization(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackRegularization(payrollHistoryId);
    }

    @PostMapping("/teaching-allowance-adjustments/{payrollHistoryId}/apply")
    PromotionActionResult applyTeachingAllowanceAdjustment(@PathVariable String payrollHistoryId) {
        return payrollService.applyTeachingAllowanceAdjustment(payrollHistoryId);
    }

    @PostMapping("/teaching-allowance-adjustments/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackTeachingAllowanceAdjustment(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackTeachingAllowanceAdjustment(payrollHistoryId);
    }

    @PostMapping("/prosecution-allowance-adjustments/{payrollHistoryId}/apply")
    PromotionActionResult applyProsecutionAllowanceAdjustment(@PathVariable String payrollHistoryId) {
        return payrollService.applyProsecutionAllowanceAdjustment(payrollHistoryId);
    }

    @PostMapping("/prosecution-allowance-adjustments/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackProsecutionAllowanceAdjustment(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackProsecutionAllowanceAdjustment(payrollHistoryId);
    }

    @PostMapping("/judicial-allowance-adjustments/{payrollHistoryId}/apply")
    PromotionActionResult applyJudicialAllowanceAdjustment(@PathVariable String payrollHistoryId) {
        return payrollService.applyJudicialAllowanceAdjustment(payrollHistoryId);
    }

    @PostMapping("/judicial-allowance-adjustments/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackJudicialAllowanceAdjustment(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackJudicialAllowanceAdjustment(payrollHistoryId);
    }

    @PostMapping("/police-allowance-adjustments/{payrollHistoryId}/apply")
    PromotionActionResult applyPoliceAllowanceAdjustment(@PathVariable String payrollHistoryId) {
        return payrollService.applyPoliceAllowanceAdjustment(payrollHistoryId);
    }

    @PostMapping("/police-allowance-adjustments/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackPoliceAllowanceAdjustment(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackPoliceAllowanceAdjustment(payrollHistoryId);
    }

    @PostMapping("/supervision-allowance-adjustments/{payrollHistoryId}/apply")
    PromotionActionResult applySupervisionAllowanceAdjustment(@PathVariable String payrollHistoryId) {
        return payrollService.applySupervisionAllowanceAdjustment(payrollHistoryId);
    }

    @PostMapping("/supervision-allowance-adjustments/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackSupervisionAllowanceAdjustment(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackSupervisionAllowanceAdjustment(payrollHistoryId);
    }

    @GetMapping("/police-rank-change-promotions")
    PageResponse<RankAllowanceChangePromotion> policeRankChangePromotions(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.policeRankChangePromotions(organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/prosecution-rank-change-promotions")
    PageResponse<RankAllowanceChangePromotion> prosecutionRankChangePromotions(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.prosecutionRankChangePromotions(organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/judicial-rank-change-promotions")
    PageResponse<RankAllowanceChangePromotion> judicialRankChangePromotions(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.judicialRankChangePromotions(organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/supervision-rank-change-promotions")
    PageResponse<RankAllowanceChangePromotion> supervisionRankChangePromotions(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.supervisionRankChangePromotions(organizationCode, keyword, PageRequest.of(page, size));
    }

    @PostMapping("/police-rank-change-promotions/{payrollHistoryId}/apply")
    PromotionActionResult applyPoliceRankChangePromotion(@PathVariable String payrollHistoryId) {
        return payrollService.applyPoliceRankChangePromotion(payrollHistoryId);
    }

    @PostMapping("/police-rank-change-promotions/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackPoliceRankChangePromotion(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackPoliceRankChangePromotion(payrollHistoryId);
    }

    @PostMapping("/prosecution-rank-change-promotions/{payrollHistoryId}/apply")
    PromotionActionResult applyProsecutionRankChangePromotion(@PathVariable String payrollHistoryId) {
        return payrollService.applyProsecutionRankChangePromotion(payrollHistoryId);
    }

    @PostMapping("/prosecution-rank-change-promotions/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackProsecutionRankChangePromotion(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackProsecutionRankChangePromotion(payrollHistoryId);
    }

    @PostMapping("/judicial-rank-change-promotions/{payrollHistoryId}/apply")
    PromotionActionResult applyJudicialRankChangePromotion(@PathVariable String payrollHistoryId) {
        return payrollService.applyJudicialRankChangePromotion(payrollHistoryId);
    }

    @PostMapping("/judicial-rank-change-promotions/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackJudicialRankChangePromotion(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackJudicialRankChangePromotion(payrollHistoryId);
    }

    @PostMapping("/supervision-rank-change-promotions/{payrollHistoryId}/apply")
    PromotionActionResult applySupervisionRankChangePromotion(@PathVariable String payrollHistoryId) {
        return payrollService.applySupervisionRankChangePromotion(payrollHistoryId);
    }

    @PostMapping("/supervision-rank-change-promotions/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackSupervisionRankChangePromotion(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackSupervisionRankChangePromotion(payrollHistoryId);
    }

    @GetMapping("/other-payroll-changes")
    PageResponse<OtherPayrollChangePreview> otherPayrollChanges(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.otherPayrollChanges(organizationCode, keyword, PageRequest.of(page, size));
    }

    @PostMapping("/other-payroll-changes/{payrollHistoryId}/apply")
    PromotionActionResult applyOtherPayrollChange(
            @PathVariable String payrollHistoryId,
            @RequestBody PayrollHistoryMaintenanceRequest request) {
        return payrollService.applyOtherPayrollChange(payrollHistoryId, request);
    }

    @PostMapping("/other-payroll-changes/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackOtherPayrollChange(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackOtherPayrollChange(payrollHistoryId);
    }

    @GetMapping("/salary-standard-adjustments")
    PageResponse<SalaryStandardAdjustmentPreview> salaryStandardAdjustments(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String targetStandardYearMonth,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.salaryStandardAdjustments(
                organizationCode, keyword, targetStandardYearMonth, scope, PageRequest.of(page, size));
    }

    @GetMapping("/basic-salary-standard-adjustments")
    PageResponse<SalaryStandardAdjustmentPreview> basicSalaryStandardAdjustments(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String targetStandardYearMonth,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.basicSalaryStandardAdjustments(
                organizationCode, keyword, targetStandardYearMonth, PageRequest.of(page, size));
    }

    @PostMapping("/basic-salary-standard-adjustments/{payrollHistoryId}/apply")
    PromotionActionResult applyBasicSalaryStandardAdjustment(
            @PathVariable String payrollHistoryId,
            @RequestParam(required = false) String targetStandardYearMonth) {
        return payrollService.applyBasicSalaryStandardAdjustment(payrollHistoryId, targetStandardYearMonth);
    }

    @PostMapping("/basic-salary-standard-adjustments/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackBasicSalaryStandardAdjustment(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackBasicSalaryStandardAdjustment(payrollHistoryId);
    }

    @GetMapping("/civil-allowance-standard-adjustments")
    PageResponse<SalaryStandardAdjustmentPreview> civilAllowanceStandardAdjustments(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String targetStandardYearMonth,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.civilAllowanceStandardAdjustments(
                organizationCode, keyword, targetStandardYearMonth, PageRequest.of(page, size));
    }

    @PostMapping("/civil-allowance-standard-adjustments/{payrollHistoryId}/apply")
    PromotionActionResult applyCivilAllowanceStandardAdjustment(
            @PathVariable String payrollHistoryId,
            @RequestParam(required = false) String targetStandardYearMonth) {
        return payrollService.applyCivilAllowanceStandardAdjustment(payrollHistoryId, targetStandardYearMonth);
    }

    @PostMapping("/civil-allowance-standard-adjustments/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackCivilAllowanceStandardAdjustment(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackCivilAllowanceStandardAdjustment(payrollHistoryId);
    }

    @GetMapping("/performance-standard-adjustments")
    PageResponse<SalaryStandardAdjustmentPreview> performanceStandardAdjustments(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String targetStandardYearMonth,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.performanceStandardAdjustments(
                organizationCode, keyword, targetStandardYearMonth, PageRequest.of(page, size));
    }

    @PostMapping("/performance-standard-adjustments/{payrollHistoryId}/apply")
    PromotionActionResult applyPerformanceStandardAdjustment(
            @PathVariable String payrollHistoryId,
            @RequestParam(required = false) String targetStandardYearMonth) {
        return payrollService.applyPerformanceStandardAdjustment(payrollHistoryId, targetStandardYearMonth);
    }

    @PostMapping("/performance-standard-adjustments/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackPerformanceStandardAdjustment(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackPerformanceStandardAdjustment(payrollHistoryId);
    }

    @GetMapping("/performance-ratio-adjustments")
    PageResponse<PerformanceRatioAdjustmentPreview> performanceRatioAdjustments(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.performanceRatioAdjustments(organizationCode, keyword, PageRequest.of(page, size));
    }

    @PostMapping("/performance-ratio-adjustments/{payrollHistoryId}/apply")
    PromotionActionResult applyPerformanceRatioAdjustment(@PathVariable String payrollHistoryId) {
        return payrollService.applyPerformanceRatioAdjustment(payrollHistoryId);
    }

    @PostMapping("/performance-ratio-adjustments/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackPerformanceRatioAdjustment(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackPerformanceRatioAdjustment(payrollHistoryId);
    }

    @PostMapping("/salary-standard-adjustments/{payrollHistoryId}/apply")
    PromotionActionResult applySalaryStandardAdjustment(
            @PathVariable String payrollHistoryId,
            @RequestParam(required = false) String targetStandardYearMonth) {
        return payrollService.applySalaryStandardAdjustment(payrollHistoryId, targetStandardYearMonth);
    }

    @PostMapping("/salary-standard-adjustments/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackSalaryStandardAdjustment(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackSalaryStandardAdjustment(payrollHistoryId);
    }

    @GetMapping("/allowance-recalculations")
    PageResponse<AllowanceRecalculationPreview> allowanceRecalculations(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.allowanceRecalculations(organizationCode, keyword, PageRequest.of(page, size));
    }

    @PostMapping("/allowance-recalculations/{payrollHistoryId}/apply")
    PromotionActionResult applyAllowanceRecalculation(@PathVariable String payrollHistoryId) {
        return payrollService.applyAllowanceRecalculation(payrollHistoryId);
    }

    @PostMapping("/allowance-recalculations/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackAllowanceRecalculation(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackAllowanceRecalculation(payrollHistoryId);
    }

    @GetMapping("/new-personnel-salary-determinations")
    PageResponse<NewPersonnelSalaryPreview> newPersonnelSalaryDeterminations(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.newPersonnelSalaryDeterminations(organizationCode, keyword, PageRequest.of(page, size));
    }

    @PostMapping("/new-personnel-salary-determinations/{uid}/apply")
    PromotionActionResult applyNewPersonnelSalaryDetermination(@PathVariable int uid) {
        return payrollService.applyNewPersonnelSalaryDetermination(uid);
    }

    @PostMapping("/new-personnel-salary-determinations/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackNewPersonnelSalaryDetermination(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackNewPersonnelSalaryDetermination(payrollHistoryId);
    }

    @GetMapping("/intern-salary-changes")
    PageResponse<InternSalaryChangePreview> internSalaryChanges(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.internSalaryChanges(organizationCode, keyword, PageRequest.of(page, size));
    }

    @PostMapping("/intern-salary-changes/{payrollHistoryId}/apply")
    PromotionActionResult applyInternSalaryChange(@PathVariable String payrollHistoryId) {
        return payrollService.applyInternSalaryChange(payrollHistoryId);
    }

    @PostMapping("/intern-salary-changes/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackInternSalaryChange(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackInternSalaryChange(payrollHistoryId);
    }

    @GetMapping("/floating-to-fixed-conversions")
    PageResponse<FloatingToFixedPreview> floatingToFixedConversions(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.floatingToFixedPreviews(organizationCode, keyword, PageRequest.of(page, size));
    }

    @PostMapping("/floating-to-fixed-conversions/{payrollHistoryId}/apply")
    PromotionActionResult applyFloatingToFixedConversion(@PathVariable String payrollHistoryId) {
        return payrollService.applyFloatingToFixedConversion(payrollHistoryId);
    }

    @PostMapping("/floating-to-fixed-conversions/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackFloatingToFixedConversion(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackFloatingToFixedConversion(payrollHistoryId);
    }

    @GetMapping("/personnel/{uid}/calculation-context")
    PayrollCalculationContext calculationContext(@PathVariable int uid) {
        return payrollService.calculationContext(uid);
    }

    @GetMapping("/personnel/{uid}/calculation-preview")
    PayrollCalculationPreview calculationPreview(
            @PathVariable int uid,
            @RequestParam(required = false) String period) {
        return payrollService.calculationPreview(uid, period);
    }

    @GetMapping("/personnel/{uid}/wage-projection")
    WageProjectionPreview wageProjection(
            @PathVariable int uid,
            @RequestParam(required = false) String period) {
        return payrollService.wageProjection(uid, period);
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

    @GetMapping("/projection-audit-summary")
    PayrollProjectionAuditSummary projectionAuditSummary(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.projectionAuditSummary(organizationCode, PageRequest.of(page, size));
    }

    @GetMapping("/personnel/{uid}/projection-history-audits")
    List<PayrollHistoryProjectionAudit> projectionHistoryAudits(@PathVariable int uid) {
        return payrollService.projectionHistoryAudits(uid);
    }

    @GetMapping("/projection-audit-export.csv")
    org.springframework.http.ResponseEntity<byte[]> downloadProjectionAuditCsv(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false, defaultValue = "false") boolean mismatchesOnly) {
        return projectionAuditExportService.downloadCsvZip(organizationCode, mismatchesOnly);
    }

    @GetMapping("/projection-audit-export.xlsx")
    org.springframework.http.ResponseEntity<byte[]> downloadProjectionAuditExcel(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false, defaultValue = "false") boolean mismatchesOnly) {
        return projectionAuditExportService.downloadExcel(organizationCode, mismatchesOnly);
    }
}
