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

@Service
public class PayrollService {

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
            PageRequest pageRequest) {
        var scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        List<NormalPromotionPreview> previews = payrollRepository
                .findPersonnelUidsWithCurrentPayroll(scope, emptyToNull(organizationCode), keyword, pageRequest)
                .stream()
                .map(this::normalPromotionPreview)
                .toList();
        return PageResponse.of(
                previews,
                pageRequest,
                payrollRepository.countPersonnelWithCurrentPayroll(scope, emptyToNull(organizationCode), keyword));
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
        return new NormalPromotionPreview(
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
                history.gradeSalaryStep(),
                currentBaseSalary,
                promotedBaseSalary,
                nullToZero(promotedBaseSalary) - nullToZero(currentBaseSalary),
                baseSalarySource);
    }

    private boolean isEducationPosition(String positionCode) {
        return positionCode != null && positionCode.length() >= 2
                && positionCode.substring(0, 2).compareTo("07") >= 0
                && positionCode.substring(0, 2).compareTo("20") < 0;
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

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
