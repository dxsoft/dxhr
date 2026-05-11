package com.dxsoft.rsgzgl.payroll;

import com.dxsoft.rsgzgl.common.NotFoundException;
import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.security.AccessControlService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayrollService {

    private static final Set<String> LEVEL_PROMOTION_POSITION_PREFIXES = Set.of(
            "01", "02", "04", "21", "22", "23", "24", "25", "26", "27", "28");
    private static final Set<String> POSITION_SEQUENCE_PREFIXES = Set.of(
            "01", "02", "03", "04", "21", "22", "23", "24", "25", "26", "27", "28");
    private static final Set<String> POLICE_OFFICER_CONVERSION_SOURCE_PREFIXES = Set.of(
            "01", "02", "23", "24", "25", "26", "27", "28");
    private static final Set<String> POLICE_OFFICER_CONVERSION_TARGET_PREFIXES = Set.of("21", "22");
    private static final Set<String> JUDICIAL_CONVERSION_TARGET_PREFIXES = Set.of("03");

    private final PayrollRepository payrollRepository;
    private final AccessControlService accessControlService;

    PayrollService(PayrollRepository payrollRepository, AccessControlService accessControlService) {
        this.payrollRepository = payrollRepository;
        this.accessControlService = accessControlService;
    }

    public PageResponse<PayrollFieldMetadata> fields(Boolean enabledIn2006Policy, PageRequest pageRequest) {
        return PageResponse.of(
                payrollRepository.findFields(enabledIn2006Policy, pageRequest),
                pageRequest,
                payrollRepository.countFields(enabledIn2006Policy));
    }

    public PageResponse<PositionSalaryStandard> positionStandards(
            String standardYearMonth,
            String positionCode,
            PageRequest pageRequest) {
        return PageResponse.of(
                payrollRepository.findPositionStandards(standardYearMonth, positionCode, pageRequest),
                pageRequest,
                payrollRepository.countPositionStandards(standardYearMonth, positionCode));
    }

    public PageResponse<AllowanceStandard> allowanceStandards(
            String standardYearMonth,
            String item,
            String positionCode,
            PageRequest pageRequest) {
        return PageResponse.of(
                payrollRepository.findAllowanceStandards(standardYearMonth, item, positionCode, pageRequest),
                pageRequest,
                payrollRepository.countAllowanceStandards(standardYearMonth, item, positionCode));
    }

    public PageResponse<BasicStandardRecord> basicStandards(
            String standardType,
            String standardYearMonth,
            String code,
            PageRequest pageRequest) {
        return PageResponse.of(
                payrollRepository.findBasicStandards(standardType, standardYearMonth, code, pageRequest),
                pageRequest,
                payrollRepository.countBasicStandards(standardType, standardYearMonth, code));
    }

    public PageResponse<RankAllowanceStandard> rankAllowanceStandards(
            String standardYearMonth,
            String rankName,
            String category,
            PageRequest pageRequest) {
        return PageResponse.of(
                payrollRepository.findRankAllowanceStandards(standardYearMonth, rankName, category, pageRequest),
                pageRequest,
                payrollRepository.countRankAllowanceStandards(standardYearMonth, rankName, category));
    }

    public PageResponse<RetainedAllowanceStandard> retainedAllowanceStandards(
            String keyword,
            PageRequest pageRequest) {
        return PageResponse.of(
                payrollRepository.findRetainedAllowanceStandards(keyword, pageRequest),
                pageRequest,
                payrollRepository.countRetainedAllowanceStandards(keyword));
    }

    public PageResponse<YearAllowanceStandard> yearAllowanceStandards(
            String standardYearMonth,
            PageRequest pageRequest) {
        return PageResponse.of(
                payrollRepository.findYearAllowanceStandards(standardYearMonth, pageRequest),
                pageRequest,
                payrollRepository.countYearAllowanceStandards(standardYearMonth));
    }

    public PageResponse<InternSalaryStandard> internSalaryStandards(
            String standardYearMonth,
            String keyword,
            PageRequest pageRequest) {
        return PageResponse.of(
                payrollRepository.findInternSalaryStandards(standardYearMonth, keyword, pageRequest),
                pageRequest,
                payrollRepository.countInternSalaryStandards(standardYearMonth, keyword));
    }

    public PageResponse<WageReformStandard> wageReformStandards(
            String positionCode,
            PageRequest pageRequest) {
        return PageResponse.of(
                payrollRepository.findWageReformStandards(positionCode, pageRequest),
                pageRequest,
                payrollRepository.countWageReformStandards(positionCode));
    }

    public PageResponse<OtherAllowanceStandard> otherAllowanceStandards(
            String standardType,
            String standardYearMonth,
            String code,
            PageRequest pageRequest) {
        return PageResponse.of(
                payrollRepository.findOtherAllowanceStandards(standardType, standardYearMonth, code, pageRequest),
                pageRequest,
                payrollRepository.countOtherAllowanceStandards(standardType, standardYearMonth, code));
    }

    public PayrollCalculationContext calculationContext(int uid) {
        PayrollHistorySnapshot history = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        accessControlService.requireOrganization(history.organizationCode());
        Map<String, Object> historyValues = payrollRepository.findLatestHistoryValues(uid);
        List<PayrollComponentValue> components = payrollRepository.findCalculationFields().stream()
                .map(field -> new PayrollComponentValue(
                        field.fieldName(),
                        field.caption(),
                        field.inputMode(),
                        field.allowance(),
                        payrollRepository.decimalValue(historyValues, field.fieldName())))
                .toList();

        BasicPayrollCalculation basicCalculation = basicCalculation(history);
        AllowanceCalculation allowanceCalculation = allowanceCalculation(history);
        AdditionalPayrollCalculation additionalCalculation = additionalCalculation(history);
        return new PayrollCalculationContext(
                uid,
                history,
                basicCalculation,
                allowanceCalculation,
                additionalCalculation,
                totalComparison(history, components, basicCalculation, allowanceCalculation, additionalCalculation),
                pgbcComparison(history),
                excludedComponents(components),
                components,
                payrollRepository.findMatchedPositionStandards(history),
                payrollRepository.findMatchedAllowanceStandards(history));
    }

    public PayrollCalculationPreview calculationPreview(int uid) {
        PayrollCalculationContext context = calculationContext(uid);
        PayrollHistorySnapshot history = context.latestHistory();
        PayrollTotalComparison total = context.totalComparison();
        return new PayrollCalculationPreview(
                uid,
                history.organizationCode(),
                history.personCode(),
                history.name(),
                history.calculationYear() + history.calculationMonth(),
                previewComponents(context),
                context.excludedComponents(),
                context.pgbcComparison(),
                total.recalculatedKnownTotal(),
                total.storedTotal(),
                total.totalDifference());
    }

    public PageResponse<PayrollCalculationAudit> calculationAudits(String organizationCode, PageRequest pageRequest) {
        List<PayrollFieldMetadata> calculationFields = payrollRepository.findCalculationFields();
        var scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        List<PayrollCalculationAudit> audits = payrollRepository
                .findPersonnelUidsWithPayrollHistory(scope, pageRequest)
                .stream()
                .map(uid -> calculationAudit(uid, calculationFields))
                .toList();
        return PageResponse.of(
                audits,
                pageRequest,
                payrollRepository.countPersonnelWithPayrollHistory(scope));
    }

    public PayrollAuditSummary auditSummary(String organizationCode, PageRequest pageRequest) {
        List<PayrollFieldMetadata> calculationFields = payrollRepository.findCalculationFields();
        var scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        List<PayrollCalculationAudit> audits = payrollRepository
                .findPersonnelUidsWithPayrollHistory(scope, pageRequest)
                .stream()
                .map(uid -> calculationAudit(uid, calculationFields))
                .toList();
        List<PayrollCalculationAudit> differences = audits.stream()
                .filter(audit -> !audit.matched())
                .toList();
        BigDecimal maxAbsoluteDifference = differences.stream()
                .map(audit -> audit.totalDifference().abs())
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        return new PayrollAuditSummary(
                payrollRepository.countPersonnelWithPayrollHistory(scope),
                audits.size(),
                differences.size(),
                maxAbsoluteDifference,
                differences);
    }

    public PageResponse<PayrollHistoryRecord> payrollHistories(
            String organizationCode,
            String period,
            String keyword,
            PageRequest pageRequest) {
        var scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        return PageResponse.of(
                payrollRepository.findPayrollHistories(scope, emptyToNull(organizationCode), period, keyword, pageRequest),
                pageRequest,
                payrollRepository.countPayrollHistories(scope, emptyToNull(organizationCode), period, keyword));
    }

    public PayrollChangeComparison payrollChangeComparison(String payrollHistoryId) {
        Map<String, Object> afterValues = payrollRepository.findHistoryValuesById(payrollHistoryId);
        String organizationCode = textValue(afterValues, "dwbm");
        accessControlService.requireOrganization(organizationCode);
        Optional<Map<String, Object>> beforeValues = payrollRepository.findPredecessorHistoryValues(payrollHistoryId);
        List<PayrollFieldMetadata> fields = payrollRepository.findCalculationFields();
        List<PayrollChangeComponentComparison> components = new ArrayList<>(fields.stream()
                .map(field -> componentComparison(field.fieldName(), field.caption(), beforeValues.orElse(null), afterValues))
                .toList());
        if (fields.stream().noneMatch(field -> "HJ2".equalsIgnoreCase(field.fieldName()))) {
            components.add(componentComparison("HJ2", "合计", beforeValues.orElse(null), afterValues));
        }
        return new PayrollChangeComparison(
                payrollHistoryId,
                beforeValues.map(values -> textValue(values, "id")).orElse(null),
                organizationCode,
                textValue(afterValues, "grbm"),
                textValue(afterValues, "xm"),
                textValue(afterValues, "jsnf") + textValue(afterValues, "jsyf"),
                textValue(afterValues, "jslb"),
                beforeValues.map(values -> textValue(values, "jsnf") + textValue(values, "jsyf")).orElse(null),
                beforeValues.map(values -> textValue(values, "jslb")).orElse(null),
                components);
    }

    public PageResponse<PayrollHistoryRecord> createPayrollHistory(int uid, PayrollHistoryMaintenanceRequest request) {
        PayrollHistorySnapshot latest = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        accessControlService.requireOrganization(latest.organizationCode());
        requirePayrollWritePermission();
        payrollRepository.createPayrollHistoryFromLatest(uid, request);
        return payrollHistories(latest.organizationCode(), null, latest.personCode(), PageRequest.of(0, 50));
    }

    public PageResponse<PayrollHistoryRecord> updatePayrollHistory(String id, PayrollHistoryMaintenanceRequest request) {
        String organizationCode = payrollRepository.findHistoryOrganizationCode(id)
                .orElseThrow(() -> new NotFoundException("Payroll history not found: " + id));
        accessControlService.requireOrganization(organizationCode);
        requirePayrollWritePermission();
        payrollRepository.updatePayrollHistory(id, request);
        return payrollHistories(organizationCode, null, null, PageRequest.of(0, 50));
    }

    public void deletePayrollHistory(String id) {
        String organizationCode = payrollRepository.findHistoryOrganizationCode(id)
                .orElseThrow(() -> new NotFoundException("Payroll history not found: " + id));
        accessControlService.requireOrganization(organizationCode);
        requirePayrollWritePermission();
        payrollRepository.deletePayrollHistory(id);
    }

    public PageResponse<TeachingAllowanceAdjustment> teachingAllowanceAdjustments(
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        var scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        return PageResponse.of(
                payrollRepository.findTeachingAllowanceAdjustments(scope, emptyToNull(organizationCode), keyword, pageRequest),
                pageRequest,
                payrollRepository.countTeachingAllowanceAdjustments(scope, emptyToNull(organizationCode), keyword));
    }

    public PageResponse<NormalPromotionPreview> normalPromotionPreviews(
            String organizationCode,
            String keyword,
            Boolean dueOnly,
            PageRequest pageRequest) {
        var scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        List<NormalPromotionPreview> previews = payrollRepository
                .findPersonnelUidsWithCurrentPayroll(scope, emptyToNull(organizationCode), keyword, pageRequest)
                .stream()
                .map(this::normalPromotionPreview)
                .filter(preview -> !Boolean.TRUE.equals(dueOnly) || Boolean.TRUE.equals(preview.eligible()))
                .toList();
        if (Boolean.TRUE.equals(dueOnly)) {
            return PageResponse.of(previews, pageRequest, previews.size());
        }
        return PageResponse.of(
                previews,
                pageRequest,
                payrollRepository.countPersonnelWithCurrentPayroll(scope, emptyToNull(organizationCode), keyword));
    }

    @Transactional
    public PromotionActionResult applyNormalPromotion(String payrollHistoryId) {
        int uid = requireCurrentHistoryUid(payrollHistoryId);
        NormalPromotionPreview preview = normalPromotionPreview(uid);
        if (!Boolean.TRUE.equals(preview.eligible()) || preview.increaseAmount() == null || preview.increaseAmount() <= 0) {
            throw new IllegalArgumentException("当前工资记录不满足正常档次/薪级晋升处理条件。");
        }
        String changeType = normalPromotionChangeType(preview.baseSalarySource());
        String promotionYear = preview.calculationPeriod().substring(0, 4);
        PromotionHistoryMutation mutation = new PromotionHistoryMutation(
                promotionYear,
                "01",
                changeType,
                promotionYear,
                null,
                preview.promotedGradeOrLevel(),
                preview.gradeSalaryLevel(),
                "0",
                preview.promotedBaseSalary(),
                nullToZero(payrollRepository.findLatestHistory(uid)
                        .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid))
                        .storedTotal()) + preview.increaseAmount());
        String newId = payrollRepository.createPromotionHistoryFromLatest(uid, mutation);
        return new PromotionActionResult(newId, payrollHistoryId, changeType, "正常档次/薪级晋升处理完成。");
    }

    @Transactional
    public PromotionActionResult rollbackNormalPromotion(String payrollHistoryId) {
        return rollbackPromotion(payrollHistoryId, Set.of("正常档次", "正常薪级"), "正常档次/薪级晋升已还原。");
    }

    public PageResponse<LevelPromotionPreview> levelPromotionPreviews(
            String organizationCode,
            String keyword,
            Boolean dueOnly,
            PageRequest pageRequest) {
        var scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        List<LevelPromotionPreview> previews = payrollRepository
                .findPersonnelUidsWithCurrentPayroll(scope, emptyToNull(organizationCode), keyword, pageRequest)
                .stream()
                .map(this::levelPromotionPreview)
                .filter(preview -> !Boolean.TRUE.equals(dueOnly)
                        || Boolean.TRUE.equals(preview.eligible())
                        && (Boolean.TRUE.equals(preview.levelPromotionDue()) || Boolean.TRUE.equals(preview.stepPromotionDue())))
                .toList();
        if (Boolean.TRUE.equals(dueOnly)) {
            return PageResponse.of(previews, pageRequest, previews.size());
        }
        return PageResponse.of(
                previews,
                pageRequest,
                payrollRepository.countPersonnelWithCurrentPayroll(scope, emptyToNull(organizationCode), keyword));
    }

    @Transactional
    public PromotionActionResult applyLevelPromotion(String payrollHistoryId) {
        int uid = requireCurrentHistoryUid(payrollHistoryId);
        LevelPromotionPreview preview = levelPromotionPreview(uid);
        if (!Boolean.TRUE.equals(preview.eligible())
                || (!Boolean.TRUE.equals(preview.levelPromotionDue()) && !Boolean.TRUE.equals(preview.stepPromotionDue()))) {
            throw new IllegalArgumentException("当前工资记录不满足级别晋升或档次晋升处理条件。");
        }
        String changeType = Boolean.TRUE.equals(preview.levelPromotionDue()) ? "正常级别" : "正常档次";
        int promotedStepValue = payrollRepository.intValue(preview.promotedStep());
        String positionSalaryGrade = String.valueOf(promotedStepValue);
        String gradeSalaryStep = "0";
        String promotionYear = preview.calculationPeriod().substring(0, 4);
        PromotionHistoryMutation mutation = new PromotionHistoryMutation(
                promotionYear,
                "01",
                changeType,
                preview.nextStepAssessmentStartYear(),
                preview.nextLevelAssessmentStartYear(),
                positionSalaryGrade,
                preview.promotedLevel(),
                gradeSalaryStep,
                preview.promotedGradeSalary(),
                nullToZero(payrollRepository.findLatestHistory(uid)
                        .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid))
                        .storedTotal()) + preview.increaseAmount());
        String newId = payrollRepository.createPromotionHistoryFromLatest(uid, mutation);
        return new PromotionActionResult(newId, payrollHistoryId, changeType, "级别晋升处理完成。");
    }

    @Transactional
    public PromotionActionResult rollbackLevelPromotion(String payrollHistoryId) {
        return rollbackPromotion(payrollHistoryId, Set.of("正常级别", "级别滚动", "正常档次"), "级别晋升已还原。");
    }

    public PageResponse<PositionChangePromotionPreview> positionChangePromotionPreviews(
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        var scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        List<PositionChangePromotionPreview> previews = payrollRepository
                .findPersonnelUidsWithCurrentPayroll(scope, emptyToNull(organizationCode), keyword, pageRequest)
                .stream()
                .map(this::positionChangePromotionPreview)
                .toList();
        return PageResponse.of(
                previews,
                pageRequest,
                payrollRepository.countPersonnelWithCurrentPayroll(scope, emptyToNull(organizationCode), keyword));
    }

    public PageResponse<EducationPromotionPreview> educationPromotionPreviews(
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        var scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        List<EducationPromotionPreview> previews = payrollRepository
                .findPersonnelUidsWithCurrentPayroll(scope, emptyToNull(organizationCode), keyword, pageRequest)
                .stream()
                .map(this::educationPromotionPreview)
                .toList();
        return PageResponse.of(
                previews,
                pageRequest,
                payrollRepository.countPersonnelWithCurrentPayroll(scope, emptyToNull(organizationCode), keyword));
    }

    public PageResponse<RegularizationPreview> regularizationPreviews(
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        var scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        List<RegularizationPreview> previews = payrollRepository
                .findProbationPersonnelUidsWithCurrentPayroll(scope, emptyToNull(organizationCode), keyword, pageRequest)
                .stream()
                .map(this::regularizationPreview)
                .toList();
        return PageResponse.of(
                previews,
                pageRequest,
                payrollRepository.countProbationPersonnelWithCurrentPayroll(scope, emptyToNull(organizationCode), keyword));
    }

    private BasicPayrollCalculation basicCalculation(PayrollHistorySnapshot history) {
        String standardYearMonth = history.salaryStandardYearMonth();
        String positionCode = history.positionCode();
        String gradeStep = String.valueOf(
                payrollRepository.intValue(history.positionSalaryGrade())
                        + payrollRepository.intValue(history.gradeSalaryStep()));
        Integer gradeSalary = payrollRepository.gradeSalary(history.gradeSalaryLevel(), gradeStep, standardYearMonth);
        Integer salaryLevelSalary = payrollRepository.salaryLevelSalary(
                history.positionSalaryGrade(),
                history.gradeSalaryStep(),
                standardYearMonth,
                positionCode);
        Integer technicalGradeSalary = payrollRepository.technicalGradeSalary(positionCode, standardYearMonth);
        Integer positionSalary = payrollRepository.positionSalary(positionCode, standardYearMonth)
                + payrollRepository.positionGradeSalary(
                positionCode,
                history.positionSalaryGrade(),
                history.gradeSalaryStep(),
                standardYearMonth);
        String baseSalarySource = baseSalarySource(positionCode);
        Integer selectedBaseSalary = switch (baseSalarySource) {
            case "GRADE" -> gradeSalary;
            case "TECHNICAL_GRADE" -> technicalGradeSalary;
            default -> salaryLevelSalary;
        };

        return new BasicPayrollCalculation(
                standardYearMonth,
                positionCode,
                payrollRepository.mapPositionSalaryCode(positionCode),
                history.positionSalaryGrade(),
                history.gradeSalaryLevel(),
                history.gradeSalaryStep(),
                positionSalary,
                gradeSalary,
                salaryLevelSalary,
                technicalGradeSalary,
                baseSalarySource,
                selectedBaseSalary,
                history.storedPositionSalary(),
                history.storedGradeSalary(),
                history.storedTechnicalGradeSalary(),
                history.storedTotal());
    }

    private List<PayrollPreviewComponent> previewComponents(PayrollCalculationContext context) {
        BasicPayrollCalculation basic = context.basicCalculation();
        AllowanceCalculation allowance = context.allowanceCalculation();
        AdditionalPayrollCalculation additional = context.additionalCalculation();
        PayrollTotalComparison total = context.totalComparison();
        return List.of(
                preview("ZWGZSE2", "职务工资", basic.positionSalary(), "AUTO"),
                preview("JBGZSE2", "级别/薪级工资", basic.selectedBaseSalary(), "AUTO"),
                preview("JSDJGZ2", "技术等级工资", basic.technicalGradeSalary(), "AUTO"),
                preview("DFBT2", dfbt2Caption(context.latestHistory()), allowance.performanceAllowance(), "AUTO"),
                preview("SDBT", "工作性/生活性补贴", allowance.subsidyAllowance(), "AUTO"),
                preview("BLFB2", "保留福补", allowance.retainedAllowance(), "AUTO"),
                preview("NJBT", "年补贴", allowance.yearAllowance(), "AUTO"),
                preview("JXJT", "警衔/警务津贴", additional.rankAllowance(), "AUTO"),
                preview("FDGZ2", "浮动工资", additional.floatingSalary(), "AUTO"),
                preview("JJJY2", "奖金结余", additional.bonusBalance(), "AUTO_OR_PRESERVE"),
                preview("TGBLBF", "套改/特岗保留", additional.retainedSpecialPostAllowance(), "AUTO_OR_PRESERVE"),
                preview("JHLJT", "教护龄津贴", total.teachingAllowance(), "AUTO"),
                preview("JSFSZWTG2", "提高工资", total.salaryIncrease(), "AUTO"));
    }

    private PayrollPreviewComponent preview(String fieldName, String caption, Integer amount, String source) {
        return preview(fieldName, caption, BigDecimal.valueOf(nullToZero(amount)), source);
    }

    private PayrollPreviewComponent preview(String fieldName, String caption, BigDecimal amount, String source) {
        return new PayrollPreviewComponent(fieldName, caption, nullToZero(amount), source);
    }

    private PayrollCalculationAudit calculationAudit(int uid, List<PayrollFieldMetadata> calculationFields) {
        PayrollHistorySnapshot history = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        accessControlService.requireOrganization(history.organizationCode());
        Map<String, Object> historyValues = payrollRepository.findLatestHistoryValues(uid);
        List<PayrollComponentValue> components = calculationFields.stream()
                .map(field -> new PayrollComponentValue(
                        field.fieldName(),
                        field.caption(),
                        field.inputMode(),
                        field.allowance(),
                        payrollRepository.decimalValue(historyValues, field.fieldName())))
                .toList();
        BasicPayrollCalculation basicCalculation = basicCalculation(history);
        AllowanceCalculation allowanceCalculation = allowanceCalculation(history);
        AdditionalPayrollCalculation additionalCalculation = additionalCalculation(history);
        PayrollTotalComparison total = totalComparison(history, components, basicCalculation, allowanceCalculation, additionalCalculation);
        BigDecimal difference = nullToZero(total.totalDifference());
        return new PayrollCalculationAudit(
                uid,
                history.organizationCode(),
                history.personCode(),
                history.name(),
                history.calculationYear() + history.calculationMonth(),
                history.storedTotal(),
                total.recalculatedKnownTotal(),
                difference,
                difference.compareTo(BigDecimal.ZERO) == 0,
                total.componentDifferences());
    }

    private String baseSalarySource(String positionCode) {
        if (positionCode == null || positionCode.length() < 2) {
            return "SALARY_LEVEL";
        }
        return switch (positionCode.substring(0, 2)) {
            case "01", "02", "04", "23", "24", "25", "26", "27", "28" -> "GRADE";
            case "21", "22" -> "TECHNICAL_GRADE";
            default -> "SALARY_LEVEL";
        };
    }

    private AllowanceCalculation allowanceCalculation(PayrollHistorySnapshot history) {
        BigDecimal performanceAllowance;
        int subsidyAllowance;
        if (performanceAndSubsidyDisabled(history)) {
            performanceAllowance = BigDecimal.ZERO;
            subsidyAllowance = 0;
        } else {
            performanceAllowance = payrollRepository.performanceAllowance(
                    history.organizationCode(),
                    history.positionCode(),
                    history.allowanceStandardYearMonth());
            subsidyAllowance = payrollRepository.subsidyAllowance(
                    history.positionCode(),
                    history.allowanceStandardYearMonth());
        }
        int retainedAllowance = payrollRepository.retainedAllowance(history.positionCode());
        BigDecimal yearAllowance = payrollRepository.yearAllowance(
                history.organizationCode(),
                history.allowanceStandardYearMonth());

        return new AllowanceCalculation(
                history.allowanceStandardYearMonth(),
                history.positionCode(),
                payrollRepository.performancePositionCode(history.positionCode(), history.allowanceStandardYearMonth()),
                payrollRepository.subsidyPositionCode(history.positionCode()),
                payrollRepository.organizationPerformanceCategory(history.organizationCode()),
                payrollRepository.organizationPerformanceRatio(history.organizationCode()),
                performanceAllowance,
                subsidyAllowance,
                retainedAllowance,
                yearAllowance,
                history.storedPerformanceAllowance(),
                history.storedSubsidyAllowance(),
                history.storedRetainedAllowance(),
                history.storedYearAllowance());
    }

    private boolean performanceAndSubsidyDisabled(PayrollHistorySnapshot history) {
        String approved = history.individualPerformanceApproved();
        boolean organizationDisabled = history.organizationPerformanceEnabled() == null
                || history.organizationPerformanceEnabled() == 0;
        boolean individualNotApproved = !"是".equals(approved);
        boolean individualExplicitlyRejected = approved != null && !approved.isBlank() && "否".equals(approved);
        return (organizationDisabled && individualNotApproved) || individualExplicitlyRejected;
    }

    private PayrollTotalComparison totalComparison(
            PayrollHistorySnapshot history,
            List<PayrollComponentValue> components,
            BasicPayrollCalculation basic,
            AllowanceCalculation allowance,
            AdditionalPayrollCalculation additional) {
        Integer teachingAllowance = teachingAllowance(history);
        Integer salaryIncrease = salaryIncrease(history, basic);
        List<PayrollComponentDifference> componentDifferences = componentDifferences(
                history,
                basic,
                allowance,
                additional,
                teachingAllowance,
                salaryIncrease);

        BigDecimal storedComponentTotal = components.stream()
                .filter(component -> !"HJ2".equalsIgnoreCase(component.fieldName()))
                .map(PayrollComponentValue::storedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal recalculatedKnownTotal = storedComponentTotal
                .subtract(BigDecimal.valueOf(history.storedPositionSalary()))
                .add(BigDecimal.valueOf(nullToZero(basic.positionSalary())))
                .subtract(BigDecimal.valueOf(history.storedGradeSalary()))
                .add(BigDecimal.valueOf(nullToZero(basic.selectedBaseSalary())))
                .subtract(BigDecimal.valueOf(history.storedTechnicalGradeSalary()))
                .add(BigDecimal.valueOf(nullToZero(basic.technicalGradeSalary())))
                .subtract(BigDecimal.valueOf(history.storedPerformanceAllowance()))
                .add(nullToZero(allowance.performanceAllowance()))
                .subtract(BigDecimal.valueOf(history.storedSubsidyAllowance()))
                .add(BigDecimal.valueOf(nullToZero(allowance.subsidyAllowance())))
                .subtract(BigDecimal.valueOf(history.storedRetainedAllowance()))
                .add(BigDecimal.valueOf(nullToZero(allowance.retainedAllowance())))
                .subtract(BigDecimal.valueOf(history.storedTeachingAllowance()))
                .add(BigDecimal.valueOf(teachingAllowance))
                .subtract(BigDecimal.valueOf(history.storedSalaryIncrease()))
                .add(BigDecimal.valueOf(salaryIncrease))
                .subtract(nullToZero(history.storedYearAllowance()))
                .add(nullToZero(allowance.yearAllowance()));
        recalculatedKnownTotal = recalculatedKnownTotal
                .subtract(BigDecimal.valueOf(history.storedRankAllowance()))
                .add(BigDecimal.valueOf(nullToZero(additional.rankAllowance())))
                .subtract(BigDecimal.valueOf(history.storedFloatingSalary()))
                .add(BigDecimal.valueOf(nullToZero(additional.floatingSalary())))
                .subtract(BigDecimal.valueOf(history.storedBonusBalance()))
                .add(BigDecimal.valueOf(nullToZero(additional.bonusBalance())))
                .subtract(BigDecimal.valueOf(history.storedRetainedSpecialPostAllowance()))
                .add(BigDecimal.valueOf(nullToZero(additional.retainedSpecialPostAllowance())));

        return new PayrollTotalComparison(
                history.teachingStartYearMonth(),
                history.teachingInterruptedYears(),
                teachingAllowance,
                salaryIncrease,
                history.storedTeachingAllowance(),
                history.storedSalaryIncrease(),
                storedComponentTotal,
                recalculatedKnownTotal,
                history.storedTotal(),
                recalculatedKnownTotal.subtract(BigDecimal.valueOf(history.storedTotal())),
                componentDifferences);
    }

    private List<PayrollComponentDifference> componentDifferences(
            PayrollHistorySnapshot history,
            BasicPayrollCalculation basic,
            AllowanceCalculation allowance,
            AdditionalPayrollCalculation additional,
            Integer teachingAllowance,
            Integer salaryIncrease) {
        List<PayrollComponentDifference> differences = new ArrayList<>();
        addDifference(differences, "ZWGZSE2", "职务工资", history.storedPositionSalary(), basic.positionSalary());
        addDifference(differences, "JBGZSE2", "级别/薪级工资", history.storedGradeSalary(), basic.selectedBaseSalary());
        addDifference(differences, "JSDJGZ2", "技术等级工资", history.storedTechnicalGradeSalary(), basic.technicalGradeSalary());
        addDifference(differences, "DFBT2", dfbt2Caption(history), history.storedPerformanceAllowance(), allowance.performanceAllowance());
        addDifference(differences, "SDBT", "工作性/生活性补贴", history.storedSubsidyAllowance(), allowance.subsidyAllowance());
        addDifference(differences, "BLFB2", "保留福补", history.storedRetainedAllowance(), allowance.retainedAllowance());
        addDifference(differences, "NJBT", "年补贴", history.storedYearAllowance(), allowance.yearAllowance());
        addDifference(differences, "JXJT", "警衔/警务津贴", history.storedRankAllowance(), additional.rankAllowance());
        addDifference(differences, "FDGZ2", "浮动工资", history.storedFloatingSalary(), additional.floatingSalary());
        addDifference(differences, "JJJY2", "奖金结余", history.storedBonusBalance(), additional.bonusBalance());
        addDifference(differences, "TGBLBF", "套改/特岗保留", history.storedRetainedSpecialPostAllowance(), additional.retainedSpecialPostAllowance());
        addDifference(differences, "JHLJT", "教护龄津贴", history.storedTeachingAllowance(), teachingAllowance);
        addDifference(differences, "JSFSZWTG2", "提高工资", history.storedSalaryIncrease(), salaryIncrease);
        return differences;
    }

    private AdditionalPayrollCalculation additionalCalculation(PayrollHistorySnapshot history) {
        return new AdditionalPayrollCalculation(
                history.rankAllowanceStandardYearMonth(),
                history.rankName(),
                payrollRepository.rankAllowance(
                        history.positionCode(),
                        history.rankAllowanceStandardYearMonth(),
                        history.rankName()),
                history.floatingStep(),
                payrollRepository.floatingSalary(
                        history.salaryStandardYearMonth(),
                        history.positionCode(),
                        history.positionSalaryGrade(),
                        history.floatingStep()),
                selectedBonusBalance(history),
                history.postAllowanceStandardYearMonth(),
                history.postAllowanceCategory(),
                history.storedPostAllowance(),
                retainedSpecialPostAllowance(history),
                history.storedRankAllowance(),
                history.storedFloatingSalary(),
                history.storedBonusBalance(),
                history.storedPostAllowance(),
                history.storedRetainedSpecialPostAllowance());
    }

    private Integer retainedSpecialPostAllowance(PayrollHistorySnapshot history) {
        if (history.organizationType() != null && history.organizationType().compareTo("07") < 0) {
            return 0;
        }
        return history.storedRetainedSpecialPostAllowance();
    }

    private PgbcComparison pgbcComparison(PayrollHistorySnapshot history) {
        return new PgbcComparison(
                history.storedPgbc(),
                history.storedPgbc(),
                "PRESERVE",
                "特殊人员工资变动保留项：工资总额减少时形成，后续增资时从增资额中冲销；当前只读对账保留旧值。");
    }

    private List<ExcludedPayrollComponent> excludedComponents(List<PayrollComponentValue> components) {
        Set<String> excludedFieldNames = Set.of("QTBT", "SIDBT", "ZWJT", "ZFBT", "JZMCBT", "GWJT2");
        return components.stream()
                .filter(component -> excludedFieldNames.contains(component.fieldName().toUpperCase()))
                .map(component -> new ExcludedPayrollComponent(
                        component.fieldName(),
                        excludedCaption(component.fieldName()),
                        component.storedAmount(),
                        excludedReason(component.fieldName())))
                .toList();
    }

    private String excludedCaption(String fieldName) {
        return switch (fieldName.toUpperCase()) {
            case "QTBT" -> "其他补贴";
            case "SIDBT" -> "不参与迁移补贴";
            case "ZWJT" -> "职务津贴";
            case "ZFBT" -> "住房补贴";
            case "JZMCBT" -> "津补贴保留项";
            case "GWJT2" -> "岗位津贴";
            default -> fieldName;
        };
    }

    private String excludedReason(String fieldName) {
        if ("QTBT".equalsIgnoreCase(fieldName)) {
            return "手工录入项，保留旧值，不做自动计算。";
        }
        if ("GWJT2".equalsIgnoreCase(fieldName)) {
            return "已确认不考虑迁移，保留旧值，不作为自动计算差异。";
        }
        return "已确认暂不考虑迁移，保留旧值，不作为自动计算差异。";
    }

    private Integer selectedBonusBalance(PayrollHistorySnapshot history) {
        if (history.storedBonusBalance() != null && history.storedBonusBalance() > 0) {
            return history.storedBonusBalance();
        }
        return payrollRepository.bonusBalance(history);
    }

    private void addDifference(
            List<PayrollComponentDifference> differences,
            String fieldName,
            String caption,
            Integer storedAmount,
            BigDecimal calculatedAmount) {
        addDifference(
                differences,
                fieldName,
                caption,
                BigDecimal.valueOf(nullToZero(storedAmount)),
                calculatedAmount);
    }

    private void addDifference(
            List<PayrollComponentDifference> differences,
            String fieldName,
            String caption,
            Integer storedAmount,
            Integer calculatedAmount) {
        addDifference(
                differences,
                fieldName,
                caption,
                BigDecimal.valueOf(nullToZero(storedAmount)),
                BigDecimal.valueOf(nullToZero(calculatedAmount)));
    }

    private void addDifference(
            List<PayrollComponentDifference> differences,
            String fieldName,
            String caption,
            BigDecimal storedAmount,
            BigDecimal calculatedAmount) {
        BigDecimal stored = nullToZero(storedAmount);
        BigDecimal calculated = nullToZero(calculatedAmount);
        BigDecimal difference = calculated.subtract(stored);
        if (difference.compareTo(BigDecimal.ZERO) != 0) {
            differences.add(new PayrollComponentDifference(fieldName, caption, stored, calculated, difference));
        }
    }

    private String dfbt2Caption(PayrollHistorySnapshot history) {
        String organizationType = history.organizationType();
        if (organizationType != null && organizationType.compareTo("07") < 0) {
            return "生活性补贴";
        }
        return "基础性绩效工资";
    }

    private Integer teachingAllowance(PayrollHistorySnapshot history) {
        String positionCode = history.positionCode();
        String teachingStart = history.teachingStartYearMonth();
        if (!isEducationPosition(positionCode) || teachingStart == null || teachingStart.replace(".", "").isBlank()) {
            return 0;
        }
        int teachingYears = yearOf(history.calculationYear()) - yearOf(teachingStart) - history.teachingInterruptedYears();
        if (teachingYears < 5) {
            return 0;
        }
        if (teachingYears < 10) {
            return 3;
        }
        if (teachingYears < 15) {
            return 5;
        }
        if (teachingYears < 20) {
            return 7;
        }
        return 10;
    }

    private Integer salaryIncrease(PayrollHistorySnapshot history, BasicPayrollCalculation basic) {
        String positionCode = history.positionCode();
        int percentage = history.raisePercentage();
        if (!isEducationPosition(positionCode) || percentage <= 0) {
            return 0;
        }
        int effectivePercentage = percentage;
        if (history.salaryStandardYearMonth() != null && history.salaryStandardYearMonth().compareTo("201807") >= 0
                && payrollRepository.organizationPerformanceCategory(history.organizationCode()) == 2
                && positionCode != null && positionCode.startsWith("10")) {
            effectivePercentage = percentage - 10;
        }
        BigDecimal base = BigDecimal.valueOf(nullToZero(basic.positionSalary()) + nullToZero(basic.selectedBaseSalary()));
        return base.multiply(BigDecimal.valueOf(effectivePercentage))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private NormalPromotionPreview normalPromotionPreview(int uid) {
        PayrollHistorySnapshot history = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        BasicPayrollCalculation current = basicCalculation(history);
        String promotedGradeOrLevel = String.valueOf(payrollRepository.intValue(history.positionSalaryGrade()) + 1);
        String baseSalarySource = baseSalarySource(history.positionCode());
        Integer promotedBaseSalary = switch (baseSalarySource) {
            case "GRADE" -> payrollRepository.gradeSalary(
                    history.gradeSalaryLevel(),
                    String.valueOf(payrollRepository.intValue(promotedGradeOrLevel) + payrollRepository.intValue(history.gradeSalaryStep())),
                    history.salaryStandardYearMonth());
            case "TECHNICAL_GRADE" -> payrollRepository.technicalGradeSalary(
                    history.positionCode(),
                    history.salaryStandardYearMonth());
            default -> payrollRepository.salaryLevelSalary(
                    promotedGradeOrLevel,
                    history.gradeSalaryStep(),
                    history.salaryStandardYearMonth(),
                    history.positionCode());
        };
        Integer currentBaseSalary = current.selectedBaseSalary();
        int calculationYear = yearOf(history.calculationYear());
        int stepStartYear = assessmentStartYear(
                history.stepAssessmentStartYear(),
                history.positionStartYearMonth(),
                history.positionCode());
        int qualifiedYears = payrollRepository.countQualifiedAssessmentYears(
                history.organizationCode(), history.personCode(), stepStartYear, calculationYear - 1);
        int requiredYears = normalPromotionRequiredYears(history);
        boolean eligible = requiredYears > 0 && qualifiedYears >= requiredYears && calculationYear >= 2007
                && !"TECHNICAL_GRADE".equals(baseSalarySource);
        return new NormalPromotionPreview(
                uid,
                history.id(),
                history.organizationCode(),
                history.personCode(),
                history.name(),
                history.calculationYear() + history.calculationMonth(),
                history.calculationType(),
                history.positionCode(),
                history.positionName(),
                history.salaryStandardYearMonth(),
                history.positionSalaryGrade(),
                promotedGradeOrLevel,
                history.gradeSalaryLevel(),
                history.levelAssessmentStartYear(),
                history.stepAssessmentStartYear(),
                qualifiedYears,
                requiredYears,
                eligible,
                currentBaseSalary,
                promotedBaseSalary,
                nullToZero(promotedBaseSalary) - nullToZero(currentBaseSalary),
                baseSalarySource);
    }

    private LevelPromotionPreview levelPromotionPreview(int uid) {
        PayrollHistorySnapshot history = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        String currentStep = String.valueOf(
                payrollRepository.intValue(history.positionSalaryGrade())
                        + payrollRepository.intValue(history.gradeSalaryStep()));
        Integer currentGradeSalary = payrollRepository.gradeSalary(
                history.gradeSalaryLevel(),
                currentStep,
                history.salaryStandardYearMonth());
        int calculationYear = yearOf(history.calculationYear());
        int levelStartYear = assessmentStartYear(
                history.levelAssessmentStartYear(),
                history.positionStartYearMonth(),
                history.positionCode());
        int stepStartYear = assessmentStartYear(
                history.stepAssessmentStartYear(),
                history.positionStartYearMonth(),
                history.positionCode());
        int qualifiedYearsForLevel = payrollRepository.countQualifiedAssessmentYears(
                history.organizationCode(), history.personCode(), levelStartYear, calculationYear - 1);
        int qualifiedYearsForStep = payrollRepository.countQualifiedAssessmentYears(
                history.organizationCode(), history.personCode(), stepStartYear, calculationYear - 1);
        boolean eligible = isLevelPromotionPosition(history.positionCode())
                && "GRADE".equals(baseSalarySource(history.positionCode()))
                && payrollRepository.intValue(history.gradeSalaryLevel()) > 1
                && calculationYear >= 2007;
        boolean levelPromotionDue = eligible && qualifiedYearsForLevel >= 5;
        boolean stepPromotionDue = eligible && qualifiedYearsForStep >= 2;
        String promotedLevel = history.gradeSalaryLevel();
        String promotedStep = currentStep;
        Integer promotedGradeSalary = currentGradeSalary;
        if (eligible && levelPromotionDue) {
            promotedLevel = String.valueOf(payrollRepository.intValue(history.gradeSalaryLevel()) - 1);
            promotedStep = firstHigherGradeStep(promotedLevel, currentGradeSalary, history.salaryStandardYearMonth());
            promotedGradeSalary = payrollRepository.gradeSalary(promotedLevel, promotedStep, history.salaryStandardYearMonth());
        }
        if (eligible && stepPromotionDue) {
            promotedStep = String.valueOf(payrollRepository.intValue(promotedStep) + 1);
            promotedGradeSalary = payrollRepository.gradeSalary(promotedLevel, promotedStep, history.salaryStandardYearMonth());
        }
        boolean gradeIncreaseExceedsStepDifference = eligible && levelPromotionDue
                && gradeIncreaseExceedsStepDifference(
                history.gradeSalaryLevel(),
                currentStep,
                promotedLevel,
                history.salaryStandardYearMonth());
        String nextLevelAssessmentStartYear = levelPromotionDue ? history.calculationYear() : String.valueOf(levelStartYear);
        String nextStepAssessmentStartYear = stepPromotionDue || gradeIncreaseExceedsStepDifference
                ? history.calculationYear()
                : String.valueOf(stepStartYear);
        return new LevelPromotionPreview(
                uid,
                history.id(),
                history.organizationCode(),
                history.personCode(),
                history.name(),
                history.calculationYear() + history.calculationMonth(),
                history.calculationType(),
                history.positionCode(),
                history.positionName(),
                history.salaryStandardYearMonth(),
                history.gradeSalaryLevel(),
                currentStep,
                promotedLevel,
                promotedStep,
                String.valueOf(levelStartYear),
                String.valueOf(stepStartYear),
                nextLevelAssessmentStartYear,
                nextStepAssessmentStartYear,
                qualifiedYearsForLevel,
                qualifiedYearsForStep,
                levelPromotionDue,
                stepPromotionDue,
                gradeIncreaseExceedsStepDifference,
                currentGradeSalary,
                promotedGradeSalary,
                nullToZero(promotedGradeSalary) - nullToZero(currentGradeSalary),
                eligible,
                levelPromotionNote(eligible, levelPromotionDue, stepPromotionDue, gradeIncreaseExceedsStepDifference));
    }

    private PositionChangePromotionPreview positionChangePromotionPreview(int uid) {
        PayrollHistorySnapshot history = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        PositionChangeCandidate candidate = payrollRepository
                .findCurrentPositionChangeCandidate(history.organizationCode(), history.personCode())
                .orElse(new PositionChangeCandidate(history.positionCode(), history.positionName(), history.positionStartYearMonth()));
        PositionLevelRange levelRange = payrollRepository.findPositionLevelRange(candidate.positionCode()).orElse(null);
        String currentPositionPrefix = positionPrefix(history.positionCode());
        String newPositionPrefix = positionPrefix(candidate.positionCode());
        boolean sequenceConversion = isSequenceConversion(currentPositionPrefix, newPositionPrefix);
        boolean policeOfficerConversion = isPoliceOfficerConversion(currentPositionPrefix, newPositionPrefix);
        boolean judicialConversion = isJudicialConversion(currentPositionPrefix, newPositionPrefix);
        String changeType = positionChangeType(
                history.positionCode(),
                candidate.positionCode(),
                sequenceConversion,
                policeOfficerConversion,
                judicialConversion);
        int currentLevel = payrollRepository.intValue(history.gradeSalaryLevel());
        String currentStep = String.valueOf(
                payrollRepository.intValue(history.positionSalaryGrade())
                        + payrollRepository.intValue(history.gradeSalaryStep()));
        Integer currentPositionSalary = payrollRepository.positionSalary(history.positionCode(), history.salaryStandardYearMonth());
        Integer newPositionSalary = payrollRepository.positionSalary(candidate.positionCode(), history.salaryStandardYearMonth());
        Integer currentGradeSalary = payrollRepository.gradeSalary(history.gradeSalaryLevel(), currentStep, history.salaryStandardYearMonth());
        PoliceOfficerConversionResult policeOfficerResult = policeOfficerConversion
                ? policeOfficerConversionResult(history, candidate, levelRange, currentLevel, currentStep, currentGradeSalary)
                : null;
        String judicialConversionStep = judicialConversion
                ? payrollRepository.judicialConversionStep(history.gradeSalaryLevel(), currentStep, candidate.positionCode())
                : null;
        boolean sameSequenceEligible = !sequenceConversion
                && isCivilServantForPositionChange(history.positionCode())
                && isCivilServantForPositionChange(candidate.positionCode())
                && levelRange != null
                && currentLevel > 0;
        boolean eligible = (policeOfficerResult != null && policeOfficerResult.eligible())
                || (judicialConversion && judicialConversionStep != null && !judicialConversionStep.isBlank())
                || sameSequenceEligible;
        String promotedLevel = history.gradeSalaryLevel();
        if (sameSequenceEligible) {
            if (currentLevel > levelRange.minimumLevel()) {
                promotedLevel = String.valueOf(levelRange.minimumLevel());
            } else if (currentLevel >= levelRange.maximumLevel()) {
                promotedLevel = String.valueOf(Math.max(1, currentLevel - 1));
            }
        }
        String promotedStep = currentStep;
        Integer promotedGradeSalary = currentGradeSalary;
        if (sameSequenceEligible && !promotedLevel.equals(history.gradeSalaryLevel())) {
            promotedStep = firstHigherGradeStep(promotedLevel, currentGradeSalary, history.salaryStandardYearMonth());
            promotedGradeSalary = payrollRepository.gradeSalary(promotedLevel, promotedStep, history.salaryStandardYearMonth());
        }
        if (policeOfficerResult != null && policeOfficerResult.eligible()) {
            promotedLevel = policeOfficerResult.promotedLevel();
            promotedStep = policeOfficerResult.promotedStep();
            promotedGradeSalary = policeOfficerResult.promotedGradeSalary();
        }
        if (judicialConversion && judicialConversionStep != null && !judicialConversionStep.isBlank()) {
            promotedLevel = history.gradeSalaryLevel();
            promotedStep = judicialConversionStep;
            promotedGradeSalary = payrollRepository.gradeSalary(promotedLevel, promotedStep, history.salaryStandardYearMonth());
        }
        int promotedLevels = Math.max(0, currentLevel - payrollRepository.intValue(promotedLevel));
        String nextLevelAssessmentStartYear = promotedLevels >= 2 ? history.calculationYear() : history.levelAssessmentStartYear();
        boolean gradeIncreaseExceedsStepDifference = sameSequenceEligible && promotedLevels > 0
                && gradeIncreaseExceedsStepDifference(
                history.gradeSalaryLevel(),
                currentStep,
                promotedLevel,
                history.salaryStandardYearMonth());
        String nextStepAssessmentStartYear = gradeIncreaseExceedsStepDifference
                ? history.calculationYear()
                : history.stepAssessmentStartYear();
        return new PositionChangePromotionPreview(
                history.id(),
                history.organizationCode(),
                history.personCode(),
                history.name(),
                history.positionCode(),
                history.positionName(),
                candidate.positionCode(),
                candidate.positionName(),
                currentPositionPrefix,
                newPositionPrefix,
                sequenceConversion,
                policeOfficerConversion,
                judicialConversion,
                changeType,
                candidate.startYearMonth(),
                nextMonth(candidate.startYearMonth()),
                history.salaryStandardYearMonth(),
                history.gradeSalaryLevel(),
                currentStep,
                levelRange == null ? null : String.valueOf(levelRange.minimumLevel()),
                levelRange == null ? null : String.valueOf(levelRange.maximumLevel()),
                policeOfficerResult == null ? null : policeOfficerResult.sameRankLevel(),
                policeOfficerResult == null ? null : policeOfficerResult.sameRankStep(),
                policeOfficerResult == null ? null : policeOfficerResult.highPositionPromotion(),
                judicialConversionStep,
                promotedLevel,
                promotedStep,
                currentPositionSalary,
                newPositionSalary,
                currentGradeSalary,
                promotedGradeSalary,
                nullToZero(newPositionSalary) - nullToZero(currentPositionSalary),
                nullToZero(promotedGradeSalary) - nullToZero(currentGradeSalary),
                nullToZero(newPositionSalary) - nullToZero(currentPositionSalary)
                        + nullToZero(promotedGradeSalary) - nullToZero(currentGradeSalary),
                nextLevelAssessmentStartYear,
                nextStepAssessmentStartYear,
                gradeIncreaseExceedsStepDifference,
                eligible,
                positionChangePromotionNote(
                        history,
                        candidate,
                        levelRange,
                        eligible,
                        promotedLevels,
                        gradeIncreaseExceedsStepDifference,
                        sequenceConversion,
                        policeOfficerConversion,
                        judicialConversion,
                        judicialConversionStep,
                        policeOfficerResult));
    }

    private EducationPromotionPreview educationPromotionPreview(int uid) {
        PayrollHistorySnapshot history = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        EducationPromotionSource education = payrollRepository
                .findLatestEducationForPromotion(history.organizationCode(), history.personCode(), history.calculationYear() + history.calculationMonth())
                .orElse(null);
        EducationRegularizationStandard standard = education == null ? null : payrollRepository
                .findEducationRegularizationStandard(history.positionCode(), education.educationCode())
                .orElse(null);
        String currentStep = String.valueOf(
                payrollRepository.intValue(history.positionSalaryGrade())
                        + payrollRepository.intValue(history.gradeSalaryStep()));
        String promotedPositionCode = history.positionCode();
        String promotedLevel = history.gradeSalaryLevel();
        String promotedStep = currentStep;
        boolean eligible = education != null && standard != null;
        if (eligible) {
            if (standard.positionCode().compareTo(history.positionCode()) <= 0) {
                promotedPositionCode = normalizeEducationPromotionPositionCode(standard.positionCode());
            }
            promotedLevel = educationPromotionLevel(history, standard, currentStep);
            promotedStep = educationPromotionStep(history, standard, promotedLevel, currentStep);
        }
        Integer currentPositionSalary = payrollRepository.positionSalary(history.positionCode(), history.salaryStandardYearMonth());
        Integer promotedPositionSalary = payrollRepository.positionSalary(promotedPositionCode, history.salaryStandardYearMonth());
        Integer currentGradeSalary = payrollRepository.gradeSalary(history.gradeSalaryLevel(), currentStep, history.salaryStandardYearMonth());
        Integer promotedGradeSalary = payrollRepository.gradeSalary(promotedLevel, promotedStep, history.salaryStandardYearMonth());
        return new EducationPromotionPreview(
                history.id(),
                history.organizationCode(),
                history.personCode(),
                history.name(),
                history.calculationYear() + history.calculationMonth(),
                history.positionCode(),
                history.positionName(),
                education == null ? null : education.educationCode(),
                education == null ? null : education.educationName(),
                education == null ? null : education.graduationDate(),
                standard == null ? null : standard.positionCode(),
                standard == null ? null : standard.positionName(),
                standard == null ? null : standard.gradeLevel(),
                standard == null ? null : standard.gradeStep(),
                promotedPositionCode,
                promotedLevel,
                promotedStep,
                currentPositionSalary,
                promotedPositionSalary,
                currentGradeSalary,
                promotedGradeSalary,
                nullToZero(promotedPositionSalary) - nullToZero(currentPositionSalary),
                nullToZero(promotedGradeSalary) - nullToZero(currentGradeSalary),
                nullToZero(promotedPositionSalary) - nullToZero(currentPositionSalary)
                        + nullToZero(promotedGradeSalary) - nullToZero(currentGradeSalary),
                eligible,
                educationPromotionNote(education, standard));
    }

    private RegularizationPreview regularizationPreview(int uid) {
        PayrollHistorySnapshot history = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        EducationPromotionSource education = payrollRepository
                .findLatestEducationForPromotion(history.organizationCode(), history.personCode(), history.calculationYear() + history.calculationMonth())
                .orElse(null);
        EducationRegularizationStandard standard = education == null ? null : payrollRepository
                .findEducationRegularizationStandard(history.positionCode(), education.educationCode())
                .orElse(null);
        boolean eligible = history.positionCode() != null && history.positionCode().contains("F")
                && education != null && standard != null;
        String regularPositionCode = eligible ? normalizeEducationPromotionPositionCode(standard.positionCode()) : null;
        String regularLevel = eligible ? standard.gradeLevel() : null;
        String regularStep = eligible ? standard.gradeStep() : null;
        Integer regularPositionSalary = eligible
                ? payrollRepository.positionSalary(regularPositionCode, history.salaryStandardYearMonth())
                : 0;
        Integer regularBaseSalary = eligible
                ? regularizedBaseSalary(regularPositionCode, regularLevel, regularStep, history.salaryStandardYearMonth())
                : 0;
        Integer currentSalary = history.storedPositionSalary() + history.storedGradeSalary() + history.storedTechnicalGradeSalary();
        Integer totalRegularSalary = nullToZero(regularPositionSalary) + nullToZero(regularBaseSalary);
        return new RegularizationPreview(
                history.id(),
                history.organizationCode(),
                history.personCode(),
                history.name(),
                history.calculationYear() + history.calculationMonth(),
                history.positionCode(),
                history.positionName(),
                education == null ? null : education.educationCode(),
                education == null ? null : education.educationName(),
                education == null ? null : education.graduationDate(),
                regularPositionCode,
                standard == null ? null : standard.positionName(),
                regularLevel,
                regularStep,
                currentSalary,
                regularPositionSalary,
                regularBaseSalary,
                totalRegularSalary,
                totalRegularSalary - nullToZero(currentSalary),
                eligible,
                regularizationNote(history, education, standard));
    }

    private Integer regularizedBaseSalary(String positionCode, String levelOrSalaryLevel, String step, String standardYearMonth) {
        return switch (baseSalarySource(positionCode)) {
            case "GRADE" -> payrollRepository.gradeSalary(levelOrSalaryLevel, step, standardYearMonth);
            case "TECHNICAL_GRADE" -> payrollRepository.technicalGradeSalary(positionCode, standardYearMonth);
            default -> payrollRepository.salaryLevelSalary(step, "0", standardYearMonth, positionCode);
        };
    }

    private String regularizationNote(
            PayrollHistorySnapshot history,
            EducationPromotionSource education,
            EducationRegularizationStandard standard) {
        if (history.positionCode() == null || !history.positionCode().contains("F")) {
            return "当前执行工资不是见习岗位，暂不参与转正定级试算。";
        }
        if (education == null) {
            return "未找到可用于转正定级的学历记录。";
        }
        if (standard == null) {
            return "未找到当前见习岗位前缀和学历编码对应的转正定级标准。";
        }
        return "按学历和 bz06_zzdz 转正定级标准试算，暂不写入数据库。";
    }

    private String educationPromotionLevel(
            PayrollHistorySnapshot history,
            EducationRegularizationStandard standard,
            String currentStep) {
        int standardLevel = payrollRepository.intValue(standard.gradeLevel());
        int currentLevel = payrollRepository.intValue(history.gradeSalaryLevel());
        if (standardLevel > 0 && currentLevel > 0 && standardLevel < currentLevel) {
            return standard.gradeLevel();
        }
        return history.gradeSalaryLevel();
    }

    private String educationPromotionStep(
            PayrollHistorySnapshot history,
            EducationRegularizationStandard standard,
            String promotedLevel,
            String currentStep) {
        int standardLevel = payrollRepository.intValue(standard.gradeLevel());
        int currentLevel = payrollRepository.intValue(history.gradeSalaryLevel());
        int standardStep = payrollRepository.intValue(standard.gradeStep());
        int currentSalary = payrollRepository.gradeSalary(history.gradeSalaryLevel(), currentStep, history.salaryStandardYearMonth());
        int standardSalary = payrollRepository.gradeSalary(standard.gradeLevel(), standard.gradeStep(), history.salaryStandardYearMonth());
        if (standardLevel > 0 && currentLevel > 0 && standardLevel < currentLevel) {
            if (standardSalary >= currentSalary) {
                return standard.gradeStep();
            }
            return firstHigherGradeStep(standard.gradeLevel(), currentSalary, history.salaryStandardYearMonth());
        }
        if (standardLevel == currentLevel) {
            return String.valueOf(Math.max(standardStep, payrollRepository.intValue(currentStep)));
        }
        if (standardSalary > currentSalary) {
            return firstHigherGradeStep(promotedLevel, standardSalary, history.salaryStandardYearMonth());
        }
        return currentStep;
    }

    private String normalizeEducationPromotionPositionCode(String positionCode) {
        if (positionCode != null && positionCode.startsWith("07") && positionCode.length() >= 4 && "0".equals(positionCode.substring(3, 4))) {
            return "070" + positionCode.substring(2, 3);
        }
        return positionCode;
    }

    private String educationPromotionNote(EducationPromotionSource education, EducationRegularizationStandard standard) {
        if (education == null) {
            return "未找到可用于学历晋升的学历记录。";
        }
        if (standard == null) {
            return "未找到当前岗位前缀和学历编码对应的转正定级标准。";
        }
        return "按最高学历和 bz06_zzdz 转正定级标准试算，暂不写入数据库。";
    }

    private boolean gradeIncreaseExceedsStepDifference(
            String currentLevel,
            String currentStep,
            String promotedLevel,
            String standardYearMonth) {
        int sourceLevel = payrollRepository.intValue(currentLevel);
        int targetLevel = payrollRepository.intValue(promotedLevel);
        int sourceStep = payrollRepository.intValue(currentStep);
        int previousSalary = payrollRepository.gradeSalary(String.valueOf(sourceLevel), String.valueOf(sourceStep), standardYearMonth);
        for (int level = sourceLevel - 1; level >= targetLevel; level--) {
            String nextLevel = String.valueOf(level);
            String nextStep = firstHigherGradeStep(nextLevel, previousSalary, standardYearMonth);
            int nextSalary = payrollRepository.gradeSalary(nextLevel, nextStep, standardYearMonth);
            int increase = nextSalary - previousSalary;
            int oneStepDifference = gradeStepDifference(nextLevel, nextStep, standardYearMonth);
            if (increase > oneStepDifference) {
                return true;
            }
            previousSalary = nextSalary;
        }
        return false;
    }

    private int gradeStepDifference(String level, String step, String standardYearMonth) {
        int stepValue = payrollRepository.intValue(step);
        if (stepValue > 1) {
            int current = payrollRepository.gradeSalary(level, String.valueOf(stepValue), standardYearMonth);
            int previous = payrollRepository.gradeSalary(level, String.valueOf(stepValue - 1), standardYearMonth);
            return Math.max(0, current - previous);
        }
        int current = payrollRepository.gradeSalary(level, "1", standardYearMonth);
        int next = payrollRepository.gradeSalary(level, "2", standardYearMonth);
        return Math.max(0, next - current);
    }

    private boolean isCivilServantForPositionChange(String positionCode) {
        if (positionCode == null || positionCode.length() < 2) {
            return false;
        }
        return Set.of("01", "02", "04", "23", "24", "25", "26", "27", "28").contains(positionCode.substring(0, 2));
    }

    private String positionPrefix(String positionCode) {
        return positionCode == null || positionCode.length() < 2 ? "" : positionCode.substring(0, 2);
    }

    private boolean isSequenceConversion(String currentPositionPrefix, String newPositionPrefix) {
        return POSITION_SEQUENCE_PREFIXES.contains(currentPositionPrefix)
                && POSITION_SEQUENCE_PREFIXES.contains(newPositionPrefix)
                && !currentPositionPrefix.equals(newPositionPrefix);
    }

    private boolean isPoliceOfficerConversion(String currentPositionPrefix, String newPositionPrefix) {
        return POLICE_OFFICER_CONVERSION_SOURCE_PREFIXES.contains(currentPositionPrefix)
                && POLICE_OFFICER_CONVERSION_TARGET_PREFIXES.contains(newPositionPrefix);
    }

    private boolean isJudicialConversion(String currentPositionPrefix, String newPositionPrefix) {
        return POLICE_OFFICER_CONVERSION_SOURCE_PREFIXES.contains(currentPositionPrefix)
                && JUDICIAL_CONVERSION_TARGET_PREFIXES.contains(newPositionPrefix);
    }

    private PoliceOfficerConversionResult policeOfficerConversionResult(
            PayrollHistorySnapshot history,
            PositionChangeCandidate candidate,
            PositionLevelRange targetLevelRange,
            int currentLevel,
            String currentStep,
            Integer currentGradeSalary) {
        if (targetLevelRange == null || currentLevel <= 0 || payrollRepository.intValue(currentStep) <= 0) {
            return PoliceOfficerConversionResult.ineligible();
        }
        int sameRankCivilLevel = targetLevelRange.minimumLevel() + 7;
        boolean highPositionPromotion = isPoliceHighPositionPromotion(history.positionCode(), candidate.positionCode());
        String sameRankLevel;
        String sameRankStep;
        String promotedLevel;
        String promotedStep;
        if (currentLevel > sameRankCivilLevel) {
            sameRankLevel = String.valueOf(targetLevelRange.minimumLevel());
            sameRankStep = firstHigherGradeStep(String.valueOf(sameRankCivilLevel), currentGradeSalary, "201807");
            promotedLevel = sameRankLevel;
            promotedStep = sameRankStep;
        } else {
            sameRankLevel = String.valueOf(Math.max(1, currentLevel - 7));
            sameRankStep = currentStep;
            if (highPositionPromotion) {
                promotedLevel = String.valueOf(Math.max(1, currentLevel - 8));
                promotedStep = firstHigherPoliceGradeStep(sameRankLevel, currentStep, promotedLevel, "201807");
            } else {
                promotedLevel = sameRankLevel;
                promotedStep = sameRankStep;
            }
        }
        int promotedGradeSalary = payrollRepository.policeOfficerGradeSalary(promotedLevel, promotedStep, "201807");
        if (promotedGradeSalary == 0) {
            promotedGradeSalary = payrollRepository.gradeSalary(promotedLevel, promotedStep, history.salaryStandardYearMonth());
        }
        return new PoliceOfficerConversionResult(
                true,
                sameRankLevel,
                sameRankStep,
                highPositionPromotion,
                promotedLevel,
                promotedStep,
                promotedGradeSalary);
    }

    private boolean isPoliceHighPositionPromotion(String currentPositionCode, String targetPositionCode) {
        return (Set.of("0207", "01C0").contains(currentPositionCode) && "2111".equals(targetPositionCode))
                || (Set.of("0206", "01B0").contains(currentPositionCode) && Set.of("2110", "2109").contains(targetPositionCode))
                || (Set.of("0204", "0205", "01A1", "01A0").contains(currentPositionCode) && Set.of("2108", "2107").contains(targetPositionCode))
                || (Set.of("0203", "0191", "0190").contains(currentPositionCode) && Set.of("2106", "2105").contains(targetPositionCode))
                || (Set.of("0202", "0181", "0180").contains(currentPositionCode) && Set.of("2104", "2103").contains(targetPositionCode))
                || (Set.of("0201", "0171", "0170").contains(currentPositionCode) && "2102".equals(targetPositionCode));
    }

    private String firstHigherPoliceGradeStep(String currentLevel, String currentStep, String promotedLevel, String standardYearMonth) {
        int currentSalary = payrollRepository.policeOfficerGradeSalary(currentLevel, currentStep, standardYearMonth);
        for (int step = 1; step <= 14; step++) {
            int amount = payrollRepository.policeOfficerGradeSalary(promotedLevel, String.valueOf(step), standardYearMonth);
            if (amount > currentSalary) {
                return String.valueOf(step);
            }
        }
        return "14";
    }

    private record PoliceOfficerConversionResult(
            boolean eligible,
            String sameRankLevel,
            String sameRankStep,
            boolean highPositionPromotion,
            String promotedLevel,
            String promotedStep,
            Integer promotedGradeSalary) {

        static PoliceOfficerConversionResult ineligible() {
            return new PoliceOfficerConversionResult(false, null, null, false, null, null, 0);
        }
    }

    private String positionChangeType(
            String currentPositionCode,
            String newPositionCode,
            boolean sequenceConversion,
            boolean policeOfficerConversion,
            boolean judicialConversion) {
        if (newPositionCode == null || newPositionCode.equals(currentPositionCode)) {
            return "未变化";
        }
        if (policeOfficerConversion) {
            return "警员套改";
        }
        if (judicialConversion) {
            return "法检套改";
        }
        if (sequenceConversion) {
            return "转换序列";
        }
        return "同序列职务变化";
    }

    private String nextMonth(String yearMonth) {
        String normalized = yearMonth == null ? "" : yearMonth.replace(".", "");
        if (normalized.length() < 6) {
            return "";
        }
        int year = payrollRepository.intValue(normalized.substring(0, 4));
        int month = payrollRepository.intValue(normalized.substring(4, 6)) + 1;
        if (month > 12) {
            year++;
            month = 1;
        }
        return "%04d%02d".formatted(year, month);
    }

    private String positionChangePromotionNote(
            PayrollHistorySnapshot history,
            PositionChangeCandidate candidate,
            PositionLevelRange levelRange,
            boolean eligible,
            int promotedLevels,
            boolean gradeIncreaseExceedsStepDifference,
            boolean sequenceConversion,
            boolean policeOfficerConversion,
            boolean judicialConversion,
            String judicialConversionStep,
            PoliceOfficerConversionResult policeOfficerResult) {
        if (policeOfficerConversion) {
            if (policeOfficerResult == null || !policeOfficerResult.eligible()) {
                return "识别为警员套改，但未找到套改后职务对应的等级范围，暂不能试算。";
            }
            return policeOfficerResult.highPositionPromotion()
                    ? "识别为警员套改；高套职务按旧系统 jytg 规则先按同级职务平套，再按职务晋升政策晋升到套改后职务。"
                    : "识别为警员套改；按旧系统 jytg 规则先判断是否达到套改后职务最低等级，未达最低进最低，已达最低保持平套等级。";
        }
        if (judicialConversion) {
            return judicialConversionStep == null || judicialConversionStep.isBlank()
                    ? "识别为法检套改，但未在 bz06_fjtgb 中找到当前级别档次和目标法检等级对应的套改档次。"
                    : "识别为法检套改；按当前执行职务、级别、档次和目标法检等级，在 bz06_fjtgb 中确定套改后档次。";
        }
        if (sequenceConversion) {
            return "新旧职务前缀属于不同序列，识别为转换序列；不按同序列职务晋升级别规则试算。";
        }
        if (!eligible) {
            return "仅公务员/参公岗位且存在新任职务级别范围时参与职务变化晋升试算。";
        }
        if (candidate.positionCode() == null || candidate.positionCode().equals(history.positionCode())) {
            return "未发现不同于当前工资记录的新任职务，按当前任职务预览。";
        }
        if (promotedLevels >= 2) {
            return gradeIncreaseExceedsStepDifference
                    ? "晋升职务相应晋升级别达到两级及以上，xckhndjb 应从职务变动级别当年重新计算；逐级计算增资额超过下一级别一个档差，xckhndzw 也应从本次晋升年度重新计算。"
                    : "晋升职务相应晋升级别达到两级及以上，xckhndjb 应从职务变动级别当年重新计算；逐级计算增资额未超过下一级别一个档差，xckhndzw 沿用原起算年。";
        }
        if (promotedLevels == 1) {
            return gradeIncreaseExceedsStepDifference
                    ? "晋升职务相应晋升一个级别，xckhndjb 继续从上一次按考核结果晋升级别当年计算；增资额超过下一级别一个档差，xckhndzw 应从本次晋升年度重新计算。"
                    : "晋升职务相应晋升一个级别，xckhndjb 继续从上一次按考核结果晋升级别当年计算；增资额未超过下一级别一个档差，xckhndzw 沿用原起算年。";
        }
        return "新任职务级别范围未导致级别晋升，仅试算职务工资变化。";
    }

    private String firstHigherGradeStep(String gradeLevel, Integer currentGradeSalary, String standardYearMonth) {
        for (int step = 1; step <= 20; step++) {
            int amount = payrollRepository.gradeSalary(gradeLevel, String.valueOf(step), standardYearMonth);
            if (amount > nullToZero(currentGradeSalary)) {
                return String.valueOf(step);
            }
        }
        return "20";
    }

    private int assessmentStartYear(String storedStartYear, String positionStartYearMonth, String positionCode) {
        int stored = yearOf(storedStartYear);
        String normalizedPositionStart = positionStartYearMonth == null ? "" : positionStartYearMonth.replace(".", "");
        int minimumStartYear = 2006;
        if (normalizedPositionStart.compareTo("200607") > 0) {
            minimumStartYear = yearOf(normalizedPositionStart);
        }
        if (stored > 0) {
            return Math.max(stored, minimumStartYear);
        }
        return minimumStartYear;
    }

    private boolean isLevelPromotionPosition(String positionCode) {
        return positionCode != null && positionCode.length() >= 2
                && LEVEL_PROMOTION_POSITION_PREFIXES.contains(positionCode.substring(0, 2));
    }

    private int normalPromotionRequiredYears(PayrollHistorySnapshot history) {
        if ("SALARY_LEVEL".equals(baseSalarySource(history.positionCode()))) {
            return 1;
        }
        if ("GRADE".equals(baseSalarySource(history.positionCode()))) {
            return 2;
        }
        return 0;
    }

    private String levelPromotionNote(
            boolean eligible,
            boolean levelPromotionDue,
            boolean stepPromotionDue,
            boolean gradeIncreaseExceedsStepDifference) {
        if (!eligible) {
            return "当前岗位前缀或工资类型暂不参与级别晋升试算。";
        }
        if (levelPromotionDue && stepPromotionDue) {
            return gradeIncreaseExceedsStepDifference
                    ? "同年满足晋升级别和晋升档次条件，已按先晋升级别、再晋升档次试算；级别晋升增资额超过下一级别一个档差，xckhndzw 从本次晋升年度重新计算。"
                    : "同年满足晋升级别和晋升档次条件，已按先晋升级别、再晋升档次试算。";
        }
        if (levelPromotionDue) {
            return gradeIncreaseExceedsStepDifference
                    ? "累计5年考核称职及以上，按次年1月晋升1个级别试算；级别晋升增资额超过下一级别一个档差，xckhndzw 从本次晋升年度重新计算。"
                    : "累计5年考核称职及以上，按次年1月晋升1个级别试算；级别晋升增资额未超过下一级别一个档差，xckhndzw 沿用原起算年。";
        }
        if (stepPromotionDue) {
            return "累计2年考核称职及以上，按晋升1个档次试算。";
        }
        return "尚未满足累计5年晋升级别或累计2年晋升档次条件。";
    }

    private boolean isEducationPosition(String positionCode) {
        return positionCode != null && positionCode.length() >= 2
                && positionCode.substring(0, 2).compareTo("07") >= 0
                && positionCode.substring(0, 2).compareTo("20") < 0;
    }

    private int requireCurrentHistoryUid(String payrollHistoryId) {
        int uid = payrollRepository.findPersonnelUidByCurrentHistoryId(payrollHistoryId)
                .orElseThrow(() -> new NotFoundException("Current payroll history not found: " + payrollHistoryId));
        PayrollHistorySnapshot latest = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        accessControlService.requireOrganization(latest.organizationCode());
        requirePayrollWritePermission();
        return uid;
    }

    private PromotionActionResult rollbackPromotion(String payrollHistoryId, Set<String> allowedChangeTypes, String message) {
        PayrollHistorySnapshot current = payrollRepository.findCurrentHistoryById(payrollHistoryId)
                .orElseThrow(() -> new NotFoundException("Current payroll history not found: " + payrollHistoryId));
        accessControlService.requireOrganization(current.organizationCode());
        requirePayrollWritePermission();
        if (!allowedChangeTypes.contains(current.calculationType())) {
            throw new IllegalArgumentException("当前工资变动类别不能通过该模块还原：" + current.calculationType());
        }
        String previousId = payrollRepository.findPredecessorHistoryId(payrollHistoryId)
                .orElseThrow(() -> new IllegalArgumentException("未找到可恢复为当前工资的上一条记录。"));
        payrollRepository.rollbackCurrentHistory(payrollHistoryId, previousId);
        return new PromotionActionResult(previousId, payrollHistoryId, current.calculationType(), message);
    }

    private String normalPromotionChangeType(String baseSalarySource) {
        if ("GRADE".equals(baseSalarySource)) {
            return "正常档次";
        }
        if ("SALARY_LEVEL".equals(baseSalarySource)) {
            return "正常薪级";
        }
        throw new IllegalArgumentException("当前基础工资类型不支持正常档次/薪级晋升处理。");
    }

    private PayrollChangeComponentComparison componentComparison(
            String fieldName,
            String caption,
            Map<String, Object> beforeValues,
            Map<String, Object> afterValues) {
        BigDecimal beforeAmount = beforeValues == null ? BigDecimal.ZERO : payrollRepository.decimalValue(beforeValues, fieldName);
        BigDecimal afterAmount = payrollRepository.decimalValue(afterValues, fieldName);
        return new PayrollChangeComponentComparison(
                fieldName,
                caption,
                beforeAmount,
                afterAmount,
                afterAmount.subtract(beforeAmount));
    }

    private String textValue(Map<String, Object> values, String fieldName) {
        Object value = values.get(fieldName);
        if (value == null) {
            value = values.get(fieldName.toLowerCase());
        }
        return value == null ? "" : value.toString().trim();
    }

    private int yearOf(String yearOrYearMonth) {
        if (yearOrYearMonth == null || yearOrYearMonth.length() < 4) {
            return 0;
        }
        return payrollRepository.intValue(yearOrYearMonth.substring(0, 4));
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    private void requirePayrollWritePermission() {
        if (!accessControlService.hasPermission("PAYROLL_WRITE")) {
            throw new org.springframework.security.access.AccessDeniedException("PAYROLL_WRITE permission required");
        }
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
