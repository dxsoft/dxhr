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

    @GetMapping("/field-config")
    List<PayrollFieldMetadata> fieldConfig() {
        return payrollService.fieldConfig();
    }

    @PutMapping("/field-config")
    List<PayrollFieldMetadata> updateFieldConfig(@RequestBody PayrollFieldConfigUpdateRequest request) {
        return payrollService.updateFieldConfig(request);
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

    @GetMapping("/allowance-standards/periods")
    List<String> allowanceStandardPeriods() {
        return payrollService.allowanceStandardPeriods();
    }

    @GetMapping("/allowance-standards/categories")
    List<Integer> allowanceStandardCategories(
            @RequestParam(required = false) String standardYearMonth) {
        return payrollService.allowanceStandardCategories(standardYearMonth);
    }

    @GetMapping("/allowance-standards/position-categories")
    List<AllowanceStandardPositionCategory> allowanceStandardPositionCategories(
            @RequestParam(required = false) String standardYearMonth,
            @RequestParam(required = false) Integer performanceCategory) {
        return payrollService.allowanceStandardPositionCategories(standardYearMonth, performanceCategory);
    }

    @GetMapping("/allowance-standards/by-position")
    List<AllowanceStandardPositionRow> allowanceStandardsByPosition(
            @RequestParam(required = false) String standardYearMonth,
            @RequestParam(required = false) Integer performanceCategory,
            @RequestParam(required = false) String positionPrefix) {
        return payrollService.allowanceStandardsByPosition(standardYearMonth, performanceCategory, positionPrefix);
    }

    @GetMapping("/basic-standards")
    List<BasicStandardRecord> basicStandards(
            @RequestParam String standardType,
            @RequestParam(required = false) String standardYearMonth,
            @RequestParam(required = false) String positionPrefix) {
        return payrollService.basicStandards(standardType, standardYearMonth, positionPrefix);
    }

    @GetMapping("/basic-standards/periods")
    List<String> basicStandardPeriods(@RequestParam String standardType) {
        return payrollService.basicStandardPeriods(standardType);
    }

    @GetMapping("/basic-standards/position-categories")
    List<AllowanceStandardPositionCategory> basicStandardPositionCategories(
            @RequestParam String standardType,
            @RequestParam(required = false) String standardYearMonth) {
        return payrollService.basicStandardPositionCategories(standardType, standardYearMonth);
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

    @GetMapping("/rank-allowance-standards/periods")
    List<String> rankAllowanceStandardPeriods(@RequestParam(required = false) String category) {
        return payrollService.rankAllowanceStandardPeriods(category);
    }

    @GetMapping("/retained-allowance-standards")
    PageResponse<RetainedAllowanceStandard> retainedAllowanceStandards(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String positionPrefix,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.retainedAllowanceStandards(keyword, positionPrefix, PageRequest.of(page, size));
    }

    @GetMapping("/retained-allowance-standards/position-categories")
    List<AllowanceStandardPositionCategory> retainedAllowanceStandardPositionCategories() {
        return payrollService.retainedAllowanceStandardPositionCategories();
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
            @RequestParam(required = false) String positionPrefix,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        boolean useFilterList = (positionPrefix != null && !positionPrefix.isBlank())
                || (positionCode != null && !positionCode.isBlank())
                || (page == null && size == null);
        if (useFilterList) {
            return payrollService.wageReformStandardsFiltered(positionCode, positionPrefix);
        }
        return payrollService.wageReformStandards(positionCode, PageRequest.of(page, size));
    }

    @GetMapping("/wage-reform-standards/position-categories")
    List<AllowanceStandardPositionCategory> wageReformStandardPositionCategories() {
        return payrollService.wageReformStandardPositionCategories();
    }

    @GetMapping("/wage-reform-standards/positions")
    List<AllowanceStandardPositionCategory> wageReformStandardPositions(
            @RequestParam(required = false) String positionPrefix) {
        return payrollService.wageReformStandardPositions(positionPrefix);
    }

    @GetMapping("/other-allowance-standards")
    PageResponse<OtherAllowanceStandard> otherAllowanceStandards(
            @RequestParam String standardType,
            @RequestParam(required = false) String standardYearMonth,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String positionPrefix,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page == null && size == null && (code == null || code.isBlank())) {
            return payrollService.otherAllowanceStandardsAll(standardType, standardYearMonth, positionPrefix);
        }
        return payrollService.otherAllowanceStandards(standardType, standardYearMonth, code, PageRequest.of(page, size));
    }

    @GetMapping("/other-allowance-standards/periods")
    List<String> otherAllowanceStandardPeriods(@RequestParam String standardType) {
        return payrollService.otherAllowanceStandardPeriods(standardType);
    }

    @GetMapping("/other-allowance-standards/position-categories")
    List<AllowanceStandardPositionCategory> otherAllowanceStandardPositionCategories(
            @RequestParam String standardType,
            @RequestParam(required = false) String standardYearMonth) {
        return payrollService.otherAllowanceStandardPositionCategories(standardType, standardYearMonth);
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
            @RequestParam(required = false) Boolean includeApply,
            @RequestParam(required = false) Boolean includeProcessed,
            @RequestParam(required = false) Boolean dueOnly,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String laterPeriodMode,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (includeApply != null || includeProcessed != null) {
            return payrollService.normalPromotionPreviews(
                    organizationCode,
                    keyword,
                    includeApply,
                    includeProcessed,
                    year,
                    laterPeriodMode,
                    PageRequest.of(page, size));
        }
        return payrollService.normalPromotionPreviews(organizationCode, keyword, dueOnly, year, PageRequest.of(page, size));
    }

    @GetMapping("/normal-promotions/{payrollHistoryId}")
    NormalPromotionDetail normalPromotionDetail(
            @PathVariable String payrollHistoryId,
            @RequestParam(required = false) String year) {
        return payrollService.normalPromotionDetail(payrollHistoryId, year);
    }

    @PostMapping("/normal-promotions/{payrollHistoryId}/apply")
    PromotionActionResult applyNormalPromotion(
            @PathVariable String payrollHistoryId,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String laterPeriodMode,
            @RequestBody(required = false) NormalPromotionApplyRequest request) {
        return payrollService.applyNormalPromotion(payrollHistoryId, year, request, laterPeriodMode);
    }

    @PostMapping("/normal-promotions/batch-apply")
    NormalPromotionBatchApplyResult batchApplyNormalPromotions(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String laterPeriodMode,
            @RequestBody NormalPromotionBatchApplyRequest request) {
        return payrollService.batchApplyNormalPromotions(year, request, laterPeriodMode);
    }

    @PostMapping("/normal-promotions/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackNormalPromotion(
            @PathVariable String payrollHistoryId,
            @RequestBody(required = false) NormalPromotionRollbackRequest request) {
        return payrollService.rollbackNormalPromotion(payrollHistoryId, request);
    }

    @PostMapping("/normal-promotions/batch-rollback")
    NormalPromotionBatchApplyResult batchRollbackNormalPromotions(
            @RequestBody NormalPromotionBatchRollbackRequest request) {
        return payrollService.batchRollbackNormalPromotions(request);
    }

    @GetMapping("/level-promotions")
    PageResponse<LevelPromotionPreview> levelPromotions(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) Boolean includeApply,
            @RequestParam(required = false) Boolean includeProcessed,
            @RequestParam(required = false) String laterPeriodMode,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.levelPromotionPreviews(
                organizationCode, keyword, year, includeApply, includeProcessed, laterPeriodMode, PageRequest.of(page, size));
    }

    @GetMapping("/level-promotions/{payrollHistoryId}")
    LevelPromotionDetail levelPromotionDetail(
            @PathVariable String payrollHistoryId,
            @RequestParam(required = false) String year) {
        return payrollService.levelPromotionDetail(payrollHistoryId, year);
    }

    @PostMapping("/level-promotions/{payrollHistoryId}/apply")
    PromotionActionResult applyLevelPromotion(
            @PathVariable String payrollHistoryId,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String laterPeriodMode,
            @RequestBody(required = false) LevelPromotionApplyRequest request) {
        return payrollService.applyLevelPromotion(payrollHistoryId, year, request, laterPeriodMode);
    }

    @PostMapping("/level-promotions/batch-apply")
    LevelPromotionBatchApplyResult batchApplyLevelPromotions(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String laterPeriodMode,
            @RequestBody LevelPromotionBatchApplyRequest request) {
        return payrollService.batchApplyLevelPromotions(year, request, laterPeriodMode);
    }

    @PostMapping("/level-promotions/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackLevelPromotion(
            @PathVariable String payrollHistoryId,
            @RequestBody(required = false) LevelPromotionRollbackRequest request) {
        return payrollService.rollbackLevelPromotion(payrollHistoryId, request);
    }

    @PostMapping("/level-promotions/batch-rollback")
    LevelPromotionBatchApplyResult batchRollbackLevelPromotions(
            @RequestBody LevelPromotionBatchRollbackRequest request) {
        return payrollService.batchRollbackLevelPromotions(request);
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
            @RequestParam(required = false) Boolean includeApply,
            @RequestParam(required = false) Boolean includeProcessed,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.positionChangePromotionPreviews(
                organizationCode, keyword, includeApply, includeProcessed, PageRequest.of(page, size));
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

    @GetMapping("/disciplinary-demotion-promotions")
    PageResponse<DisciplinaryDemotionPromotionListItem> disciplinaryDemotionPromotions(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean includeApply,
            @RequestParam(required = false) Boolean includeProcessed,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.disciplinaryDemotionPromotionPreviews(
                organizationCode, keyword, includeApply, includeProcessed, PageRequest.of(page, size));
    }

    @GetMapping("/disciplinary-demotion-promotions/{payrollHistoryId}")
    DisciplinaryDemotionPromotionPreview disciplinaryDemotionPromotionDetail(@PathVariable String payrollHistoryId) {
        return payrollService.disciplinaryDemotionPromotionDetail(payrollHistoryId);
    }

    @PostMapping("/disciplinary-demotion-promotions/{payrollHistoryId}/apply")
    PromotionActionResult applyDisciplinaryDemotionPromotion(@PathVariable String payrollHistoryId) {
        return payrollService.applyDisciplinaryDemotionPromotion(payrollHistoryId);
    }

    @PostMapping("/disciplinary-demotion-promotions/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackDisciplinaryDemotionPromotion(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackDisciplinaryDemotionPromotion(payrollHistoryId);
    }

    @GetMapping("/education-promotions")
    PageResponse<EducationPromotionPreview> educationPromotions(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean includeApply,
            @RequestParam(required = false) Boolean includeProcessed,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.educationPromotionPreviews(
                organizationCode, keyword, includeApply, includeProcessed, PageRequest.of(page, size));
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
            @RequestParam(required = false) Boolean includeApply,
            @RequestParam(required = false) Boolean includeProcessed,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.regularizationPreviews(
                organizationCode, keyword, includeApply, includeProcessed, PageRequest.of(page, size));
    }

    @PostMapping("/regularizations/{payrollHistoryId}/apply")
    PromotionActionResult applyRegularization(
            @PathVariable String payrollHistoryId,
            @RequestBody(required = false) RegularizationApplyRequest request) {
        return payrollService.applyRegularization(payrollHistoryId, request);
    }

    @PostMapping("/regularizations/batch-apply")
    RegularizationBatchApplyResult batchApplyRegularizations(
            @RequestBody RegularizationBatchApplyRequest request) {
        return payrollService.batchApplyRegularizations(request);
    }

    @PostMapping("/regularizations/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackRegularization(
            @PathVariable String payrollHistoryId,
            @RequestBody(required = false) RegularizationRollbackRequest request) {
        return payrollService.rollbackRegularization(payrollHistoryId, request);
    }

    @PostMapping("/regularizations/batch-rollback")
    RegularizationBatchApplyResult batchRollbackRegularizations(
            @RequestBody RegularizationBatchRollbackRequest request) {
        return payrollService.batchRollbackRegularizations(request);
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
            @RequestParam(required = false) String laterPeriodMode,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.policeRankChangePromotions(
                organizationCode, keyword, laterPeriodMode, PageRequest.of(page, size));
    }

    @GetMapping("/prosecution-rank-change-promotions")
    PageResponse<RankAllowanceChangePromotion> prosecutionRankChangePromotions(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String laterPeriodMode,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.prosecutionRankChangePromotions(
                organizationCode, keyword, laterPeriodMode, PageRequest.of(page, size));
    }

    @GetMapping("/judicial-rank-change-promotions")
    PageResponse<RankAllowanceChangePromotion> judicialRankChangePromotions(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String laterPeriodMode,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.judicialRankChangePromotions(
                organizationCode, keyword, laterPeriodMode, PageRequest.of(page, size));
    }

    @GetMapping("/supervision-rank-change-promotions")
    PageResponse<RankAllowanceChangePromotion> supervisionRankChangePromotions(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String laterPeriodMode,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.supervisionRankChangePromotions(
                organizationCode, keyword, laterPeriodMode, PageRequest.of(page, size));
    }

    @PostMapping("/police-rank-change-promotions/{payrollHistoryId}/apply")
    RankAllowanceChangeApplyResult applyPoliceRankChangePromotion(
            @PathVariable String payrollHistoryId,
            @RequestParam(required = false) String laterPeriodMode) {
        return payrollService.applyPoliceRankChangePromotion(payrollHistoryId, laterPeriodMode);
    }

    @GetMapping(value = "/police-rank-change-promotions/mid-chain-export", produces = "text/csv")
    org.springframework.http.ResponseEntity<byte[]> exportPoliceRankChangeMidChain(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword) {
        byte[] csv = payrollService.exportPoliceRankChangeMidChainCsv(organizationCode, keyword);
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"police-rank-change-mid-chain-export.csv\"")
                .body(csv);
    }

    @PostMapping("/police-rank-change-promotions/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackPoliceRankChangePromotion(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackPoliceRankChangePromotion(payrollHistoryId);
    }

    @PostMapping("/prosecution-rank-change-promotions/{payrollHistoryId}/apply")
    RankAllowanceChangeApplyResult applyProsecutionRankChangePromotion(
            @PathVariable String payrollHistoryId,
            @RequestParam(required = false) String laterPeriodMode) {
        return payrollService.applyProsecutionRankChangePromotion(payrollHistoryId, laterPeriodMode);
    }

    @GetMapping(value = "/prosecution-rank-change-promotions/mid-chain-export", produces = "text/csv")
    org.springframework.http.ResponseEntity<byte[]> exportProsecutionRankChangeMidChain(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword) {
        byte[] csv = payrollService.exportProsecutionRankChangeMidChainCsv(organizationCode, keyword);
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"prosecution-rank-change-mid-chain-export.csv\"")
                .body(csv);
    }

    @PostMapping("/prosecution-rank-change-promotions/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackProsecutionRankChangePromotion(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackProsecutionRankChangePromotion(payrollHistoryId);
    }

    @PostMapping("/judicial-rank-change-promotions/{payrollHistoryId}/apply")
    RankAllowanceChangeApplyResult applyJudicialRankChangePromotion(
            @PathVariable String payrollHistoryId,
            @RequestParam(required = false) String laterPeriodMode) {
        return payrollService.applyJudicialRankChangePromotion(payrollHistoryId, laterPeriodMode);
    }

    @GetMapping(value = "/judicial-rank-change-promotions/mid-chain-export", produces = "text/csv")
    org.springframework.http.ResponseEntity<byte[]> exportJudicialRankChangeMidChain(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword) {
        byte[] csv = payrollService.exportJudicialRankChangeMidChainCsv(organizationCode, keyword);
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"judicial-rank-change-mid-chain-export.csv\"")
                .body(csv);
    }

    @PostMapping("/judicial-rank-change-promotions/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackJudicialRankChangePromotion(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackJudicialRankChangePromotion(payrollHistoryId);
    }

    @PostMapping("/supervision-rank-change-promotions/{payrollHistoryId}/apply")
    RankAllowanceChangeApplyResult applySupervisionRankChangePromotion(
            @PathVariable String payrollHistoryId,
            @RequestParam(required = false) String laterPeriodMode) {
        return payrollService.applySupervisionRankChangePromotion(payrollHistoryId, laterPeriodMode);
    }

    @GetMapping(value = "/supervision-rank-change-promotions/mid-chain-export", produces = "text/csv")
    org.springframework.http.ResponseEntity<byte[]> exportSupervisionRankChangeMidChain(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword) {
        byte[] csv = payrollService.exportSupervisionRankChangeMidChainCsv(organizationCode, keyword);
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"supervision-rank-change-mid-chain-export.csv\"")
                .body(csv);
    }

    @PostMapping("/supervision-rank-change-promotions/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackSupervisionRankChangePromotion(@PathVariable String payrollHistoryId) {
        return payrollService.rollbackSupervisionRankChangePromotion(payrollHistoryId);
    }

    @PostMapping("/police-rank-change-promotions/batch-apply")
    RankAllowanceChangeBatchApplyResult batchApplyPoliceRankChangePromotions(
            @RequestBody RankAllowanceChangeBatchApplyRequest request,
            @RequestParam(required = false) String laterPeriodMode) {
        return payrollService.batchApplyPoliceRankChangePromotions(request, laterPeriodMode);
    }

    @PostMapping("/police-rank-change-promotions/batch-rollback")
    LevelPromotionBatchApplyResult batchRollbackPoliceRankChangePromotions(
            @RequestBody RankAllowanceChangeBatchRollbackRequest request) {
        return payrollService.batchRollbackPoliceRankChangePromotions(request);
    }

    @PostMapping("/prosecution-rank-change-promotions/batch-apply")
    RankAllowanceChangeBatchApplyResult batchApplyProsecutionRankChangePromotions(
            @RequestBody RankAllowanceChangeBatchApplyRequest request,
            @RequestParam(required = false) String laterPeriodMode) {
        return payrollService.batchApplyProsecutionRankChangePromotions(request, laterPeriodMode);
    }

    @PostMapping("/prosecution-rank-change-promotions/batch-rollback")
    LevelPromotionBatchApplyResult batchRollbackProsecutionRankChangePromotions(
            @RequestBody RankAllowanceChangeBatchRollbackRequest request) {
        return payrollService.batchRollbackProsecutionRankChangePromotions(request);
    }

    @PostMapping("/judicial-rank-change-promotions/batch-apply")
    RankAllowanceChangeBatchApplyResult batchApplyJudicialRankChangePromotions(
            @RequestBody RankAllowanceChangeBatchApplyRequest request,
            @RequestParam(required = false) String laterPeriodMode) {
        return payrollService.batchApplyJudicialRankChangePromotions(request, laterPeriodMode);
    }

    @PostMapping("/judicial-rank-change-promotions/batch-rollback")
    LevelPromotionBatchApplyResult batchRollbackJudicialRankChangePromotions(
            @RequestBody RankAllowanceChangeBatchRollbackRequest request) {
        return payrollService.batchRollbackJudicialRankChangePromotions(request);
    }

    @PostMapping("/supervision-rank-change-promotions/batch-apply")
    RankAllowanceChangeBatchApplyResult batchApplySupervisionRankChangePromotions(
            @RequestBody RankAllowanceChangeBatchApplyRequest request,
            @RequestParam(required = false) String laterPeriodMode) {
        return payrollService.batchApplySupervisionRankChangePromotions(request, laterPeriodMode);
    }

    @PostMapping("/supervision-rank-change-promotions/batch-rollback")
    LevelPromotionBatchApplyResult batchRollbackSupervisionRankChangePromotions(
            @RequestBody RankAllowanceChangeBatchRollbackRequest request) {
        return payrollService.batchRollbackSupervisionRankChangePromotions(request);
    }

    @GetMapping("/other-payroll-changes")
    PageResponse<OtherPayrollChangePreview> otherPayrollChanges(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.otherPayrollChanges(organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/other-payroll-changes/{payrollHistoryId}")
    OtherPayrollChangeDetail otherPayrollChangeDetail(@PathVariable String payrollHistoryId) {
        return payrollService.otherPayrollChangeDetail(payrollHistoryId);
    }

    @PostMapping("/other-payroll-changes/{payrollHistoryId}/preview")
    OtherPayrollChangeCalcResult previewOtherPayrollChange(
            @PathVariable String payrollHistoryId,
            @RequestBody OtherPayrollChangeCalcRequest request) {
        return payrollService.previewOtherPayrollChange(payrollHistoryId, request);
    }

    @PostMapping("/other-payroll-changes/{payrollHistoryId}/apply")
    PromotionActionResult applyOtherPayrollChange(
            @PathVariable String payrollHistoryId,
            @RequestBody OtherPayrollChangeCalcRequest request) {
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

    @GetMapping("/basic-salary-standard-adjustments/periods")
    List<String> basicSalaryStandardAdjustmentPeriods() {
        return payrollService.basicSalaryStandardAdjustmentPeriods();
    }

    @GetMapping("/basic-salary-standard-adjustments")
    PageResponse<SalaryStandardAdjustmentPreview> basicSalaryStandardAdjustments(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String targetStandardYearMonth,
            @RequestParam(required = false) Boolean includeApply,
            @RequestParam(required = false) Boolean includeProcessed,
            @RequestParam(required = false) String laterPeriodMode,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.basicSalaryStandardAdjustments(
                organizationCode,
                keyword,
                targetStandardYearMonth,
                includeApply,
                includeProcessed,
                laterPeriodMode,
                PageRequest.of(page, size));
    }

    @PostMapping("/basic-salary-standard-adjustments/{payrollHistoryId}/apply")
    BasicSalaryStandardAdjustmentApplyResult applyBasicSalaryStandardAdjustment(
            @PathVariable String payrollHistoryId,
            @RequestParam(required = false) String targetStandardYearMonth,
            @RequestParam(required = false) String laterPeriodMode,
            @RequestBody(required = false) BasicSalaryStandardAdjustmentBatchApplyRequest.BasicSalaryStandardAdjustmentApplyItem request) {
        return payrollService.applyBasicSalaryStandardAdjustment(
                payrollHistoryId, targetStandardYearMonth, request, laterPeriodMode);
    }

    @PostMapping("/basic-salary-standard-adjustments/batch-apply")
    BasicSalaryStandardAdjustmentBatchApplyResult batchApplyBasicSalaryStandardAdjustments(
            @RequestParam String targetStandardYearMonth,
            @RequestParam(required = false) String laterPeriodMode,
            @RequestBody BasicSalaryStandardAdjustmentBatchApplyRequest request) {
        return payrollService.batchApplyBasicSalaryStandardAdjustments(
                targetStandardYearMonth, request, laterPeriodMode);
    }

    @GetMapping(value = "/basic-salary-standard-adjustments/mid-chain-export", produces = "text/csv")
    org.springframework.http.ResponseEntity<byte[]> exportBasicSalaryStandardAdjustmentMidChain(
            @RequestParam String targetStandardYearMonth,
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword) {
        byte[] csv = payrollService.exportBasicSalaryStandardAdjustmentMidChainCsv(
                organizationCode, keyword, targetStandardYearMonth);
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"basic-salary-mid-chain-export.csv\"")
                .body(csv);
    }

    @PostMapping("/basic-salary-standard-adjustments/batch-rollback")
    LevelPromotionBatchApplyResult batchRollbackBasicSalaryStandardAdjustments(
            @RequestBody BasicSalaryStandardAdjustmentBatchRollbackRequest request) {
        return payrollService.batchRollbackBasicSalaryStandardAdjustments(request);
    }

    @PostMapping("/basic-salary-standard-adjustments/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackBasicSalaryStandardAdjustment(
            @PathVariable String payrollHistoryId,
            @RequestBody(required = false) BasicSalaryStandardAdjustmentBatchRollbackRequest.BasicSalaryStandardAdjustmentRollbackItem request) {
        return payrollService.rollbackBasicSalaryStandardAdjustment(payrollHistoryId, request);
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
            @RequestParam(required = false) Boolean includeApply,
            @RequestParam(required = false) Boolean includeProcessed,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.newPersonnelSalaryDeterminations(
                organizationCode, keyword, includeApply, includeProcessed, PageRequest.of(page, size));
    }

    @GetMapping("/new-personnel-salary-determinations/{uid}")
    NewPersonnelSalaryDetail newPersonnelSalaryDeterminationDetail(@PathVariable int uid) {
        return payrollService.newPersonnelSalaryDeterminationDetail(uid);
    }

    @PostMapping("/new-personnel-salary-determinations/{uid}/apply")
    PromotionActionResult applyNewPersonnelSalaryDetermination(@PathVariable int uid) {
        return payrollService.applyNewPersonnelSalaryDetermination(uid);
    }

    @PostMapping("/new-personnel-salary-determinations/batch-apply")
    NewPersonnelSalaryBatchApplyResult batchApplyNewPersonnelSalaryDeterminations(
            @RequestBody NewPersonnelSalaryBatchApplyRequest request) {
        return payrollService.batchApplyNewPersonnelSalaryDeterminations(request);
    }

    @PostMapping("/new-personnel-salary-determinations/{payrollHistoryId}/rollback")
    PromotionActionResult rollbackNewPersonnelSalaryDetermination(
            @PathVariable String payrollHistoryId,
            @RequestBody(required = false) NewPersonnelSalaryRollbackRequest request) {
        return payrollService.rollbackNewPersonnelSalaryDetermination(payrollHistoryId, request);
    }

    @PostMapping("/new-personnel-salary-determinations/batch-rollback")
    NewPersonnelSalaryBatchApplyResult batchRollbackNewPersonnelSalaryDeterminations(
            @RequestBody NewPersonnelSalaryBatchRollbackRequest request) {
        return payrollService.batchRollbackNewPersonnelSalaryDeterminations(request);
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
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.projectionAuditSummary(organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/projection-audit-personnel")
    PageResponse<ProjectionAuditPersonnelRow> projectionAuditPersonnel(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollService.projectionAuditPersonnelPage(organizationCode, keyword, PageRequest.of(page, size));
    }

    @PostMapping("/projection-audit-run")
    PayrollProjectionAuditSummary projectionAuditRun(@RequestBody ProjectionAuditRunRequest request) {
        return payrollService.projectionAuditRun(request == null ? List.of() : request.uids());
    }

    @GetMapping("/personnel/{uid}/projection-history-audits")
    List<PayrollHistoryProjectionAudit> projectionHistoryAudits(
            @PathVariable int uid,
            @RequestParam(required = false, defaultValue = "true") boolean includeStepDetails) {
        return payrollService.projectionHistoryAudits(uid, includeStepDetails);
    }

    @GetMapping("/projection-audit-export.csv")
    org.springframework.http.ResponseEntity<byte[]> downloadProjectionAuditCsv(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "false") boolean mismatchesOnly) {
        return projectionAuditExportService.downloadCsvZip(organizationCode, keyword, mismatchesOnly);
    }

    @GetMapping("/projection-audit-export.xlsx")
    org.springframework.http.ResponseEntity<byte[]> downloadProjectionAuditExcel(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "false") boolean mismatchesOnly) {
        return projectionAuditExportService.downloadExcel(organizationCode, keyword, mismatchesOnly);
    }
}
