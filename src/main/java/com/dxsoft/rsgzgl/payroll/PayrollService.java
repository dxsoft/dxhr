package com.dxsoft.rsgzgl.payroll;

import com.dxsoft.rsgzgl.common.NotFoundException;
import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.security.AccessControlService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayrollService {

    private static final Logger log = LoggerFactory.getLogger(PayrollService.class);

    private static final Set<String> WAGE_REFORM_POSITION_PREFIXES = Set.of(
            "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "21", "22", "23", "24", "25", "26", "27", "28");
    private static final Set<String> LEVEL_PROMOTION_POSITION_PREFIXES = Set.of(
            "01", "02", "04", "21", "22", "23", "24", "25", "26", "27", "28");
    private static final Set<String> POSITION_SEQUENCE_PREFIXES = Set.of(
            "01", "02", "03", "04", "21", "22", "23", "24", "25", "26", "27", "28");
    private static final Set<String> POLICE_OFFICER_CONVERSION_SOURCE_PREFIXES = Set.of(
            "01", "02", "03", "04", "23", "24", "25", "26", "27", "28");
    private static final Set<String> POLICE_OFFICER_CONVERSION_TARGET_PREFIXES = Set.of("21", "22");
    private static final Set<String> JUDICIAL_CONVERSION_TARGET_PREFIXES = Set.of("03");
    private static final Set<String> INSTITUTION_POSITION_PREFIXES = Set.of("07", "08", "09", "10", "11");
    private static final String ADMINISTRATIVE_REGULARIZATION_LOOKUP_POSITION = "01B0";
    private static final Set<String> REGULARIZATION_LOOKUP_BY_01_PREFIXES = Set.of(
            "01", "02", "03", "04", "21", "22", "23", "24", "25", "26", "27", "28");
    private static final String RANK_ALLOWANCE_COMPONENT_CAPTION = "警衔/检察/审判/监察津贴";

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
        return calculationPreview(uid, null);
    }

    public PayrollCalculationPreview calculationPreview(int uid, String period) {
        String targetPeriod = emptyToNull(projectionPeriod(period));
        if (targetPeriod == null) {
            PayrollCalculationContext context = calculationContext(uid);
            PayrollHistorySnapshot history = context.latestHistory();
            return buildCalculationPreview(uid, history, context, history.calculationYear() + history.calculationMonth());
        }
        WageProjectionRun projection = runWageProjection(uid, period);
        PayrollHistorySnapshot latest = projection.latest();
        if (!projection.eligible()) {
            PayrollCalculationContext context = calculationContext(uid);
            return buildCalculationPreview(uid, latest, context, targetPeriod);
        }
        WageProjectionState state = finalizeProjectionState(
                projection.state(), projection.targetPeriod(), latest.organizationCode());
        PayrollCalculationContext context = calculationContextFromProjection(uid, latest, state);
        return buildCalculationPreview(uid, latest, context, projection.targetPeriod());
    }

    private PayrollCalculationPreview buildCalculationPreview(
            int uid,
            PayrollHistorySnapshot latest,
            PayrollCalculationContext context,
            String calculationPeriod) {
        return new PayrollCalculationPreview(
                uid,
                latest.organizationCode(),
                latest.personCode(),
                latest.name(),
                calculationPeriod,
                previewComponents(context),
                context.excludedComponents(),
                context.pgbcComparison(),
                context.totalComparison().recalculatedKnownTotal(),
                context.totalComparison().storedTotal(),
                context.totalComparison().totalDifference());
    }

    private PayrollCalculationContext calculationContextFromProjection(
            int uid,
            PayrollHistorySnapshot latest,
            WageProjectionState state) {
        Map<String, Object> historyValues = payrollRepository.findLatestHistoryValues(uid);
        List<PayrollComponentValue> components = payrollRepository.findCalculationFields().stream()
                .map(field -> new PayrollComponentValue(
                        field.fieldName(),
                        field.caption(),
                        field.inputMode(),
                        field.allowance(),
                        payrollRepository.decimalValue(historyValues, field.fieldName())))
                .toList();
        BasicPayrollCalculation basicCalculation = basicCalculation(state, latest);
        AllowanceCalculation allowanceCalculation = allowanceCalculation(state, latest);
        AdditionalPayrollCalculation additionalCalculation = additionalCalculation(state, latest);
        return new PayrollCalculationContext(
                uid,
                latest,
                basicCalculation,
                allowanceCalculation,
                additionalCalculation,
                totalComparison(latest, components, basicCalculation, allowanceCalculation, additionalCalculation),
                pgbcComparison(latest),
                excludedComponents(components),
                components,
                payrollRepository.findMatchedPositionStandards(latest),
                payrollRepository.findMatchedAllowanceStandards(latest));
    }

    public WageProjectionPreview wageProjection(int uid, String period) {
        WageProjectionRun projection = runWageProjection(uid, period);
        PayrollHistorySnapshot latest = projection.latest();
        if (!projection.eligible()) {
            return new WageProjectionPreview(
                    uid,
                    latest.organizationCode(),
                    latest.personCode(),
                    latest.name(),
                    projection.targetPeriod(),
                    "",
                    projection.regularizationYearMonth(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    0,
                    0,
                    "",
                    "",
                    0,
                    projection.lines());
        }
        WageProjectionState state = finalizeProjectionState(
                projection.state(), projection.targetPeriod(), latest.organizationCode());
        BasicPayrollCalculation basic = basicCalculation(state, latest);
        return new WageProjectionPreview(
                uid,
                latest.organizationCode(),
                latest.personCode(),
                latest.name(),
                projection.targetPeriod(),
                projection.start().period(),
                projection.regularizationYearMonth(),
                state.positionCode(),
                state.positionName(),
                state.level(),
                state.stepOrSalaryLevel(),
                levelStepDisplay(
                        resolvedBaseSalarySource(state),
                        state.level(),
                        state.stepOrSalaryLevel(),
                        state.gradeStepDifferenceCount()),
                state.levelStartYear(),
                state.stepStartYear(),
                resolvedBaseSalarySource(state),
                state.salaryStandardYearMonth(),
                basic.positionSalary(),
                basic.selectedBaseSalary(),
                state.rankName(),
                state.rankAllowanceStandardYearMonth(),
                state.rankAllowance(),
                projection.lines());
    }

    private WageProjectionRun runWageProjection(int uid, String period) {
        String targetPeriod = projectionPeriod(period);
        PayrollHistorySnapshot latest = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        accessControlService.requireOrganization(latest.organizationCode());
        String regularizationYearMonth = payrollRepository.findRegularizationYearMonth(latest.organizationCode(), latest.personCode());
        List<String> lines = new ArrayList<>();
        lines.add("目标年月：" + targetPeriod + "。");
        WageProjectionStart start = wageProjectionStart(latest);
        if (!start.eligible()) {
            lines.add(start.note());
            return new WageProjectionRun(latest, start, targetPeriod, regularizationYearMonth, null, lines, false);
        }
        RankAllowanceState initialRankAllowance = initialRankAllowanceState(latest, start.period());
        String initialSalaryStandardYearMonth = initialSalaryStandardYearMonth(latest, start.period());
        String initialAllowanceStandardYearMonth = initialAllowanceStandardYearMonth(latest, start.period());
        WageProjectionState state = new WageProjectionState(
                start.positionCode(),
                start.positionName(),
                start.level(),
                start.stepOrSalaryLevel(),
                "0",
                start.levelStartYear(),
                start.stepStartYear(),
                baseSalarySource(start.positionCode(), start.level()),
                initialSalaryStandardYearMonth,
                initialAllowanceStandardYearMonth,
                initialRankAllowance.rankName(),
                initialRankAllowance.standardYearMonth(),
                initialRankAllowance.amount(),
                initialRankAllowance.category());
        lines.add(start.note());
        if (initialRankAllowance.amount() > 0) {
            lines.add(rankAllowanceTitle(initialRankAllowance.rankName()) + "起点："
                    + emptyToDash(initialRankAllowance.rankName())
                    + "，标准 " + emptyToDash(initialRankAllowance.standardYearMonth())
                    + "，津贴 " + initialRankAllowance.amount() + "。");
        }
        String startPeriod = start.period();
        List<WageProjectionEvent> projectionEvents = wageProjectionEvents(
                latest.organizationCode(),
                latest.personCode(),
                startPeriod,
                targetPeriod);
        payrollRepository.findPositionAtOrBefore(latest.organizationCode(), latest.personCode(), targetPeriod)
                .ifPresent(position -> {
                    lines.add("目标年月任职记录：" + position.startYearMonth() + " " + position.positionCode() + " " + position.positionName() + "。");
                });
        int baseYear = yearOf(startPeriod);
        int targetYear = yearOf(targetPeriod);
        Set<Integer> promptedMissingAssessmentYears = new java.util.TreeSet<>();
        int eventIndex = 0;
        for (int year = Math.max(2007, baseYear + 1); year <= targetYear; year++) {
            String yearStart = String.format("%04d01", year);
            while (eventIndex < projectionEvents.size()
                    && projectionEvents.get(eventIndex).period().compareTo(yearStart) < 0) {
                state = applyWageProjectionEvent(state, projectionEvents.get(eventIndex), lines);
                eventIndex++;
            }
            if (supportsGradePromotion(state) && payrollRepository.intValue(state.level()) > 1) {
                int levelStart = assessmentStartYear(state.levelStartYear(), start.positionStartYearMonth(), state.positionCode());
                int stepStart = assessmentStartYear(state.stepStartYear(), start.positionStartYearMonth(), state.positionCode());
                appendMissingAssessmentPrompt(lines, latest.organizationCode(), latest.personCode(), levelStart, year - 1, promptedMissingAssessmentYears);
                appendMissingAssessmentPrompt(lines, latest.organizationCode(), latest.personCode(), stepStart, year - 1, promptedMissingAssessmentYears);
                int qualifiedLevel = payrollRepository.countQualifiedAssessmentYears(latest.organizationCode(), latest.personCode(), levelStart, year - 1);
                int qualifiedStep = payrollRepository.countQualifiedAssessmentYears(latest.organizationCode(), latest.personCode(), stepStart, year - 1);
                boolean specialLevelPromotionDue = specialWageReformLevelPromotionDue(state, start, latest, year);
                boolean levelDue = !specialLevelPromotionDue && qualifiedLevel >= 5;
                boolean stepDue = qualifiedStep >= 2;
                if (specialLevelPromotionDue) {
                    String previousLevel = state.level();
                    String previousStep = state.stepOrSalaryLevel();
                    int currentSalary = gradeSalaryAmount(state, previousLevel, previousStep, state.salaryStandardYearMonth());
                    String nextLevel = cappedPromotedLevel(state.positionCode(), String.valueOf(Math.max(1, payrollRepository.intValue(previousLevel) - 1)));
                    String nextStep = firstHigherGradeStep(nextLevel, currentSalary, state.salaryStandardYearMonth());
                    lines.add(year + " 年：2007-2010 套改后级别滚动，上一年度考核称职及以上且达到套改表规定年限，晋升级别 "
                            + levelStepDisplay(resolvedBaseSalarySource(state), previousLevel, previousStep) + " -> "
                            + levelStepDisplay(baseSalarySource(state.positionCode(), nextLevel), nextLevel, nextStep) + "。");
                    String nextStepStartYear = gradeIncreaseExceedsStepDifference(previousLevel, previousStep, nextLevel, state.salaryStandardYearMonth())
                            ? String.valueOf(year)
                            : state.stepStartYear();
                    state = projectionWithLevelStep(state, nextLevel, nextStep, String.valueOf(year), nextStepStartYear);
                }
                if (levelDue) {
                    String previousLevel = state.level();
                    String previousStep = state.stepOrSalaryLevel();
                    if (atHighestPositionLevel(state.positionCode(), previousLevel)) {
                        state = promoteCivilServantGradeStep(
                                state,
                                year,
                                lines,
                                "累计 " + qualifiedLevel + " 年考核合格，已达到所任职务最高级别，按级别晋升口径级别不变",
                                true);
                    } else {
                        int currentSalary = payrollRepository.gradeSalary(previousLevel, previousStep, state.salaryStandardYearMonth());
                        String nextLevel = String.valueOf(Math.max(1, payrollRepository.intValue(previousLevel) - 1));
                        String nextStep = firstHigherGradeStep(nextLevel, currentSalary, state.salaryStandardYearMonth());
                        lines.add(year + " 年：累计 " + qualifiedLevel + " 年考核合格，晋升级别 "
                                + levelStepDisplay(state.baseSalarySource(), previousLevel, previousStep) + " -> " + levelStepDisplay(state.baseSalarySource(), nextLevel, nextStep) + "。");
                        String nextStepStartYear = gradeIncreaseExceedsStepDifference(previousLevel, previousStep, nextLevel, state.salaryStandardYearMonth())
                                ? String.valueOf(year)
                                : state.stepStartYear();
                        state = projectionWithLevelStep(state, nextLevel, nextStep, String.valueOf(year), nextStepStartYear);
                    }
                }
                if (stepDue) {
                    state = promoteCivilServantGradeStep(
                            state,
                            year,
                            lines,
                            "累计 " + qualifiedStep + " 年考核合格");
                }
            } else if ("SALARY_LEVEL".equals(state.baseSalarySource())) {
                int stepStart = assessmentStartYear(state.stepStartYear(), start.positionStartYearMonth(), state.positionCode());
                appendMissingAssessmentPrompt(lines, latest.organizationCode(), latest.personCode(), stepStart, year - 1, promptedMissingAssessmentYears);
                int qualifiedStep = payrollRepository.countQualifiedAssessmentYears(latest.organizationCode(), latest.personCode(), stepStart, year - 1);
                if (qualifiedStep >= 1) {
                    String promotedStep = String.valueOf(payrollRepository.intValue(state.stepOrSalaryLevel()) + 1);
                    lines.add(year + " 年：事业岗位累计 " + qualifiedStep + " 年考核合格，晋升薪级到 " + promotedStep + "。");
                    state = projectionWithStep(state, promotedStep, String.valueOf(year));
                }
            }
            String nextYearStart = String.format("%04d01", year + 1);
            while (eventIndex < projectionEvents.size()
                    && projectionEvents.get(eventIndex).period().compareTo(nextYearStart) < 0) {
                state = applyWageProjectionEvent(state, projectionEvents.get(eventIndex), lines);
                eventIndex++;
            }
        }
        while (eventIndex < projectionEvents.size()) {
            state = applyWageProjectionEvent(state, projectionEvents.get(eventIndex), lines);
            eventIndex++;
        }
        return new WageProjectionRun(latest, start, targetPeriod, regularizationYearMonth, state, lines, true);
    }

    private WageProjectionState finalizeProjectionState(
            WageProjectionState state,
            String targetPeriod,
            String organizationCode) {
        state = finalizeProjectionSalaryStandard(state, targetPeriod);
        return syncAllowanceStandardWithSalaryStandard(state, organizationCode, null);
    }

    private WageProjectionState finalizeProjectionSalaryStandard(WageProjectionState state, String targetPeriod) {
        String effectiveStandard = payrollRepository.latestBasicSalaryStandardAtOrBefore(targetPeriod);
        if (emptyToNull(effectiveStandard) == null || emptyToNull(state.salaryStandardYearMonth()) == null) {
            return emptyToNull(effectiveStandard) == null ? state : state.withSalaryStandard(effectiveStandard);
        }
        if (effectiveStandard.compareTo(state.salaryStandardYearMonth()) > 0) {
            return state.withSalaryStandard(effectiveStandard);
        }
        return state;
    }

    private List<WageProjectionEvent> wageProjectionEvents(
            String organizationCode,
            String personCode,
            String startPeriod,
            String targetPeriod) {
        List<WageProjectionEvent> events = new ArrayList<>();
        payrollRepository.findPositionChangesBetween(
                organizationCode,
                personCode,
                startPeriod,
                targetPeriod,
                WAGE_REFORM_POSITION_PREFIXES)
                .stream()
                .map(position -> WageProjectionEvent.position(nextMonth(position.startYearMonth()), position, organizationCode, personCode))
                .filter(event -> !event.period().isBlank())
                .filter(event -> event.period().compareTo(startPeriod) > 0 && event.period().compareTo(targetPeriod) <= 0)
                .forEach(events::add);
        payrollRepository.findRankAllowanceChangesBetween(organizationCode, personCode, startPeriod, targetPeriod)
                .stream()
                .map(change -> WageProjectionEvent.rankChange(nextMonth(change.startYearMonth()), change, organizationCode, personCode))
                .filter(event -> !event.period().isBlank())
                .filter(event -> event.period().compareTo(startPeriod) > 0 && event.period().compareTo(targetPeriod) <= 0)
                .forEach(events::add);
        payrollRepository.findRankAllowanceStandardPeriodsBetween(startPeriod, targetPeriod)
                .stream()
                .filter(standardPeriod -> standardPeriod != null && !standardPeriod.isBlank())
                .map(standardPeriod -> WageProjectionEvent.rankStandard(standardPeriod, standardPeriod, organizationCode, personCode))
                .forEach(events::add);
        payrollRepository.findBasicSalaryStandardPeriodsBetween(startPeriod, targetPeriod)
                .stream()
                .filter(standardPeriod -> standardPeriod != null && !standardPeriod.isBlank())
                .map(standardPeriod -> WageProjectionEvent.basicSalaryStandard(standardPeriod, standardPeriod, organizationCode, personCode))
                .forEach(events::add);
        payrollRepository.findEducationRecordsBetween(organizationCode, personCode, startPeriod, targetPeriod)
                .stream()
                .map(education -> WageProjectionEvent.education(
                        nextMonth(normalizeYearMonth(education.graduationDate())),
                        education,
                        organizationCode,
                        personCode))
                .filter(event -> !event.period().isBlank())
                .filter(event -> event.period().compareTo(startPeriod) > 0 && event.period().compareTo(targetPeriod) <= 0)
                .forEach(events::add);
        return events.stream()
                .sorted(Comparator.comparing(WageProjectionEvent::period).thenComparing(WageProjectionEvent::sortOrder))
                .toList();
    }

    private WageProjectionState applyWageProjectionEvent(
            WageProjectionState state,
            WageProjectionEvent event,
            List<String> lines) {
        if (event.basicSalaryStandardYearMonth() != null) {
            return applyBasicSalaryStandardEvent(
                    state, event.basicSalaryStandardYearMonth(), event.organizationCode(), lines);
        }
        if (event.rankStandardYearMonth() != null) {
            return applyRankAllowanceStandardEvent(state, event.rankStandardYearMonth(), lines);
        }
        if (event.rankChange() != null) {
            return applyRankAllowanceChangeEvent(state, event.period(), event.rankChange(), lines);
        }
        if (event.educationChange() != null) {
            return applyEducationPromotionEvent(state, event.period(), event.educationChange(), lines);
        }
        return applyWageProjectionPositionChange(state, event.position(), event.organizationCode(), event.personCode(), lines);
    }

    private WageProjectionState applyEducationPromotionEvent(
            WageProjectionState state,
            String period,
            EducationPromotionSource education,
            List<String> lines) {
        if (!"GRADE".equals(state.baseSalarySource())) {
            return state;
        }
        EducationPromotionResolution resolution = resolveEducationPromotion(
                state.positionCode(),
                state.positionName(),
                state.level(),
                state.stepOrSalaryLevel(),
                state.gradeStepDifferenceCount(),
                state.levelStartYear(),
                state.stepStartYear(),
                state.salaryStandardYearMonth(),
                education);
        if (!resolution.eligible()) {
            return state;
        }
        String changeYear = period.length() >= 4 ? period.substring(0, 4) : period;
        lines.add(period + " 学历变动：取得 " + emptyToDash(education.educationName())
                + "（毕业 " + formatYearMonth(education.graduationDate()) + "），"
                + resolution.note()
                + "，由 " + levelStepDisplay(
                        state.baseSalarySource(),
                        state.level(),
                        state.stepOrSalaryLevel(),
                        state.gradeStepDifferenceCount())
                + " 调整为 " + levelStepDisplay(
                        "GRADE",
                        resolution.promotedLevel(),
                        resolution.promotedGradeStep(),
                        resolution.promotedGradeStepDifference())
                + "。");
        return new WageProjectionState(
                resolution.promotedPositionCode(),
                state.positionName(),
                resolution.promotedLevel(),
                resolution.promotedGradeStep(),
                resolution.promotedGradeStepDifference(),
                resolution.nextLevelAssessmentStartYear(),
                resolution.nextStepAssessmentStartYear(),
                "GRADE",
                state.salaryStandardYearMonth(),
                state.allowanceStandardYearMonth(),
                state.rankName(),
                state.rankAllowanceStandardYearMonth(),
                state.rankAllowance(),
                state.rankAllowanceCategory());
    }

    private WageProjectionState applyBasicSalaryStandardEvent(
            WageProjectionState state,
            String standardYearMonth,
            String organizationCode,
            List<String> lines) {
        if (normalizedEquals(standardYearMonth, state.salaryStandardYearMonth())) {
            return syncAllowanceStandardWithSalaryStandard(state, organizationCode, lines);
        }
        if (!payrollRepository.hasBasicSalaryStandardForSource(standardYearMonth, resolvedBaseSalarySource(state))) {
            return state;
        }
        int positionSalary = projectedPositionSalary(state, standardYearMonth);
        int baseSalary = projectedBaseSalary(state, standardYearMonth);
        lines.add(standardYearMonth + " 调整基本工资标准：执行标准 " + standardYearMonth
                + "，职务工资 " + positionSalary
                + "，" + baseSalaryLabel(resolvedBaseSalarySource(state)) + " " + baseSalary + "。");
        WageProjectionState updated = state.withSalaryStandard(standardYearMonth);
        return syncAllowanceStandardWithSalaryStandard(updated, organizationCode, lines);
    }

    private WageProjectionState syncAllowanceStandardWithSalaryStandard(
            WageProjectionState state,
            String organizationCode,
            List<String> lines) {
        String salaryStandard = state.salaryStandardYearMonth();
        if (emptyToNull(salaryStandard) == null
                || !payrollRepository.hasAllowanceStandard(salaryStandard, organizationCode, state.positionCode())
                || normalizedEquals(salaryStandard, state.allowanceStandardYearMonth())) {
            return state;
        }
        BigDecimal performanceAllowance = projectedPerformanceAllowance(state, organizationCode, salaryStandard);
        int subsidyAllowance = projectedSubsidyAllowance(state, organizationCode, salaryStandard);
        if (lines != null) {
            lines.add(salaryStandard + " 同步调整公务员津补贴标准：执行标准 " + salaryStandard
                    + "，生活性补贴 " + performanceAllowance.stripTrailingZeros().toPlainString()
                    + "，工作性津贴 " + subsidyAllowance + "。");
        }
        return state.withAllowanceStandard(salaryStandard);
    }

    private BigDecimal projectedPerformanceAllowance(
            WageProjectionState state,
            String organizationCode,
            String allowanceStandardYearMonth) {
        return payrollRepository.performanceAllowance(
                organizationCode, state.positionCode(), allowanceStandardYearMonth);
    }

    private int projectedSubsidyAllowance(
            WageProjectionState state,
            String organizationCode,
            String allowanceStandardYearMonth) {
        return payrollRepository.subsidyAllowance(organizationCode, state.positionCode(), allowanceStandardYearMonth);
    }

    private String initialSalaryStandardYearMonth(PayrollHistorySnapshot latest, String startPeriod) {
        if (emptyToNull(latest.salaryStandardYearMonth()) != null) {
            return latest.salaryStandardYearMonth();
        }
        String resolved = payrollRepository.latestBasicSalaryStandardAtOrBefore(startPeriod);
        return emptyToNull(resolved) != null ? resolved : startPeriod;
    }

    private String initialAllowanceStandardYearMonth(PayrollHistorySnapshot latest, String startPeriod) {
        if (emptyToNull(latest.allowanceStandardYearMonth()) != null) {
            return latest.allowanceStandardYearMonth();
        }
        String resolved = payrollRepository.latestAllowanceStandardAtOrBefore(
                startPeriod, latest.organizationCode(), latest.positionCode());
        return emptyToNull(resolved) != null ? resolved : startPeriod;
    }

    private int projectedBaseSalary(WageProjectionState state, String standardYearMonth) {
        return switch (resolvedBaseSalarySource(state)) {
            case "GRADE" -> payrollRepository.civilServantGradeSalary(
                    state.level(), state.stepOrSalaryLevel(), state.gradeStepDifferenceCount(), standardYearMonth);
            case "POLICE_GRADE" -> payrollRepository.policeOfficerGradeSalary(
                    state.level(),
                    policeGradeStep(state.stepOrSalaryLevel(), state.gradeStepDifferenceCount()),
                    standardYearMonth);
            default -> payrollRepository.salaryLevelSalary(
                    state.stepOrSalaryLevel(), "0", standardYearMonth, state.positionCode());
        };
    }

    private int projectedPositionSalary(WageProjectionState state, String standardYearMonth) {
        return payrollRepository.positionSalary(state.positionCode(), standardYearMonth)
                + payrollRepository.positionGradeSalary(
                state.positionCode(),
                state.stepOrSalaryLevel(),
                "0",
                standardYearMonth);
    }

    private String baseSalaryLabel(String baseSalarySource) {
        return switch (baseSalarySource) {
            case "POLICE_GRADE" -> "级别工资";
            case "SALARY_LEVEL" -> "薪级工资";
            default -> "级别/薪级工资";
        };
    }

    private WageProjectionState applyRankAllowanceStandardEvent(
            WageProjectionState state,
            String standardYearMonth,
            List<String> lines) {
        if (emptyToNull(state.rankName()) == null) {
            return state;
        }
        String category = rankAllowanceCategory(state.rankName());
        if (!payrollRepository.hasRankAllowanceStandardForCategory(standardYearMonth, category)) {
            return state;
        }
        String standardLb = payrollRepository.resolveRankAllowanceStandardLb(state.rankName(), null);
        int amount = payrollRepository.rankAllowanceByRank(standardYearMonth, state.rankName(), standardLb);
        lines.add(standardYearMonth + " " + rankAllowanceStandardTitle(state.rankName()) + "："
                + emptyToDash(state.rankName())
                + " 执行新标准 " + standardYearMonth + "，津贴 " + amount + "。");
        return state.withRankAllowance(state.rankName(), standardYearMonth, amount, category);
    }

    private WageProjectionState applyRankAllowanceChangeEvent(
            WageProjectionState state,
            String period,
            RankAllowanceChange change,
            List<String> lines) {
        String standardYearMonth = emptyToNull(state.rankAllowanceStandardYearMonth()) == null
                ? payrollRepository.latestRankAllowanceStandardAtOrBefore(period)
                : state.rankAllowanceStandardYearMonth();
        String category = rankAllowanceCategory(change.rankName());
        String standardLb = payrollRepository.resolveRankAllowanceStandardLb(change.rankName(), change.category());
        int amount = payrollRepository.rankAllowanceByRank(standardYearMonth, change.rankName(), standardLb);
        lines.add(period + " " + rankAllowanceChangeTitle(change.rankName()) + "："
                + rankAllowanceTypeName(change.rankName()) + "由 " + emptyToDash(state.rankName())
                + " 调整为 " + emptyToDash(change.rankName())
                + "，执行标准 " + emptyToDash(standardYearMonth)
                + "，津贴 " + amount + "。");
        return state.withRankAllowance(change.rankName(), standardYearMonth, amount, category);
    }

    private WageProjectionState applyWageProjectionPositionChange(
            WageProjectionState state,
            PositionChangeCandidate position,
            String organizationCode,
            String personCode,
            List<String> lines) {
        String positionCode = position.positionCode();
        String positionName = position.positionName();
        String baseSalarySource = baseSalarySource(positionCode, state.level());
        String salaryStandardYearMonth = state.salaryStandardYearMonth();
        String appointmentPeriod = normalizeYearMonth(position.startYearMonth());
        String period = nextMonth(appointmentPeriod);
        if (isLowerPositionLayer(state.positionCode(), positionCode)
                && payrollRepository.hasDemotionDisciplinaryRecord(organizationCode, personCode, appointmentPeriod)) {
            WageProjectionState demotionBase = disciplinaryDemotionBaseState(state, positionCode);
            WageProjectionState demoted = projectDisciplinaryDemotion(demotionBase, positionCode, positionName, period, salaryStandardYearMonth);
            int layers = positionLayer(positionCode) - positionLayer(state.positionCode());
            lines.add(period + " 撤职处分：采用任职记录 " + formatYearMonth(appointmentPeriod) + " 职务 " + positionDisplay(positionCode, positionName)
                    + disciplinaryDemotionBaseExplanation(state, demotionBase)
                    + "，降低 " + layers + " 个职务层次，级别按每层降低 2 级逐级就近就低套入 "
                    + levelStepDisplay("GRADE", demoted.level(), demoted.stepOrSalaryLevel()) + "。");
            return demoted;
        }
        if (isPoliceOfficerConversion(positionPrefix(state.positionCode()), positionPrefix(positionCode))) {
            PositionLevelRange levelRange = payrollRepository.findPositionLevelRange(positionCode).orElse(null);
            PoliceOfficerConversionResult result = policeOfficerConversionResult(
                    state.positionCode(),
                    positionCode,
                    levelRange,
                    payrollRepository.intValue(state.level()),
                    state.stepOrSalaryLevel(),
                    gradeSalaryAmount(state, state.level(), state.stepOrSalaryLevel(), salaryStandardYearMonth),
                    salaryStandardYearMonth);
            if (result.eligible()) {
                lines.add(period + " 警员套改：采用任职记录 " + formatYearMonth(appointmentPeriod) + " 职务 " + positionDisplay(positionCode, positionName)
                        + "，按警员套改由 " + levelStepDisplay(state.baseSalarySource(), state.level(), state.stepOrSalaryLevel())
                        + " 试算为 " + levelStepDisplay("GRADE", result.promotedLevel(), result.promotedStep()) + "。");
                return new WageProjectionState(
                        positionCode,
                        positionName,
                        result.promotedLevel(),
                        result.promotedStep(),
                        "0",
                        state.levelStartYear(),
                        state.stepStartYear(),
                        "GRADE",
                        state.salaryStandardYearMonth(),
                        state.allowanceStandardYearMonth(),
                        state.rankName(),
                        state.rankAllowanceStandardYearMonth(),
                        state.rankAllowance(),
                        state.rankAllowanceCategory());
            }
            lines.add(period + " 警员套改：任职记录 " + formatYearMonth(appointmentPeriod) + " 为 " + positionDisplay(positionCode, positionName)
                    + "，未找到套改后职务对应的等级范围，暂不能试算。");
            return state;
        }
        if (isPoliceToAdministrativeConversion(positionPrefix(state.positionCode()), positionPrefix(positionCode))) {
            PositionLevelRange levelRange = payrollRepository.findPositionLevelRange(positionCode).orElse(null);
            WageProjectionState promoted = projectPoliceToCivilServantChangeFromState(
                    state,
                    positionCode,
                    positionName,
                    period.length() >= 4 ? period.substring(0, 4) : "",
                    salaryStandardYearMonth,
                    levelRange);
            lines.add(period + " 警员回到其他类：采用任职记录 " + formatYearMonth(appointmentPeriod) + " 职务 " + positionDisplay(positionCode, positionName)
                    + "，按警员等级加 7 后由 " + levelStepDisplay(state.baseSalarySource(), state.level(), state.stepOrSalaryLevel())
                    + " 试算为 " + levelStepDisplay("GRADE", promoted.level(), promoted.stepOrSalaryLevel()) + "。");
            return promoted;
        }
        if (isCivilServantForPositionChange(positionCode)) {
            PositionLevelRange levelRange = payrollRepository.findPositionLevelRange(positionCode).orElse(null);
            WageProjectionState promoted = projectPositionChangeFromState(
                    state,
                    positionCode,
                    positionName,
                    period.length() >= 4 ? period.substring(0, 4) : "",
                    salaryStandardYearMonth,
                    state.level(),
                    state.stepOrSalaryLevel(),
                    levelRange);
            lines.add(period + " 职务变化：采用任职记录 " + formatYearMonth(appointmentPeriod) + " 职务 " + positionDisplay(positionCode, positionName)
                    + "，按职务晋升政策由 " + levelStepDisplay(state.baseSalarySource(), state.level(), state.stepOrSalaryLevel())
                    + " 试算为 " + levelStepDisplay("GRADE", promoted.level(), promoted.stepOrSalaryLevel()) + "。");
            return promoted;
        }
        lines.add(period + " 职务变化：任职记录 " + formatYearMonth(appointmentPeriod) + " 为 " + positionDisplay(positionCode, positionName)
                + "，当前推算暂只自动试算公务员级别工资职务变化。");
        return state;
    }

    private WageProjectionState projectDisciplinaryDemotion(
            WageProjectionState state,
            String positionCode,
            String positionName,
            String period,
            String salaryStandardYearMonth) {
        int currentLevel = payrollRepository.intValue(state.level());
        int targetLayer = positionLayer(positionCode);
        int currentLayer = positionLayer(state.positionCode());
        int layerDrop = Math.max(0, targetLayer - currentLayer);
        int demotedLevel = Math.min(27, currentLevel + layerDrop * 2);
        String demotedStep = lowerGradeStepByLevel(
                currentLevel,
                state.stepOrSalaryLevel(),
                demotedLevel,
                salaryStandardYearMonth);
        String year = period.length() >= 4 ? period.substring(0, 4) : state.levelStartYear();
        return new WageProjectionState(
                positionCode,
                positionName,
                String.valueOf(demotedLevel),
                demotedStep,
                "0",
                year,
                year,
                "GRADE",
                state.salaryStandardYearMonth(),
                state.allowanceStandardYearMonth(),
                state.rankName(),
                state.rankAllowanceStandardYearMonth(),
                state.rankAllowance(),
                state.rankAllowanceCategory());
    }

    private WageProjectionState projectPositionChangeFromState(
            WageProjectionState state,
            String positionCode,
            String positionName,
            String calculationYear,
            String salaryStandardYearMonth,
            String fallbackLevel,
            String fallbackStep,
            PositionLevelRange levelRange) {
        int currentLevel = payrollRepository.intValue(state.level());
        String promotedLevel = state.level();
        String promotedStep = state.stepOrSalaryLevel();
        boolean eligible = isCivilServantForPositionChange(state.positionCode())
                && isCivilServantForPositionChange(positionCode)
                && levelRange != null
                && currentLevel > 0;
        if (eligible) {
            if (currentLevel > levelRange.minimumLevel()) {
                promotedLevel = String.valueOf(levelRange.minimumLevel());
            } else if (isHigherPositionLayer(state.positionCode(), positionCode) && currentLevel >= levelRange.maximumLevel()) {
                promotedLevel = String.valueOf(Math.max(1, currentLevel - 1));
            }
            if (!promotedLevel.equals(state.level())) {
                int currentGradeSalary = payrollRepository.civilServantGradeSalary(
                        state.level(),
                        state.stepOrSalaryLevel(),
                        state.gradeStepDifferenceCount(),
                        salaryStandardYearMonth);
                promotedStep = firstHigherGradeStep(promotedLevel, currentGradeSalary, salaryStandardYearMonth);
            }
        } else {
            promotedLevel = fallbackLevel;
            promotedStep = fallbackStep;
        }
        int promotedLevels = Math.max(0, currentLevel - payrollRepository.intValue(promotedLevel));
        boolean gradeIncreaseExceedsStepDifference = eligible && promotedLevels > 0
                && gradeIncreaseExceedsStepDifference(state.level(), state.stepOrSalaryLevel(), promotedLevel, salaryStandardYearMonth);
        String nextLevelStartYear = promotedLevels >= 2 ? calculationYear : state.levelStartYear();
        String nextStepStartYear = gradeIncreaseExceedsStepDifference ? calculationYear : state.stepStartYear();
        return new WageProjectionState(
                positionCode,
                positionName,
                promotedLevel,
                promotedStep,
                "0",
                nextLevelStartYear,
                nextStepStartYear,
                "GRADE",
                state.salaryStandardYearMonth(),
                state.allowanceStandardYearMonth(),
                state.rankName(),
                state.rankAllowanceStandardYearMonth(),
                state.rankAllowance(),
                state.rankAllowanceCategory());
    }

    private WageProjectionState disciplinaryDemotionBaseState(WageProjectionState state, String targetPositionCode) {
        if (POLICE_OFFICER_CONVERSION_TARGET_PREFIXES.contains(positionPrefix(state.positionCode()))
                && !POLICE_OFFICER_CONVERSION_TARGET_PREFIXES.contains(positionPrefix(targetPositionCode))) {
            return policeConvertedCivilServantState(state, state.positionCode(), state.positionName());
        }
        return state;
    }

    private String disciplinaryDemotionBaseExplanation(WageProjectionState state, WageProjectionState demotionBase) {
        if (!state.level().equals(demotionBase.level())) {
            return "，警员等级先加 7 换算为 " + levelStepDisplay("GRADE", demotionBase.level(), demotionBase.stepOrSalaryLevel());
        }
        return "，由 " + levelStepDisplay(state.baseSalarySource(), state.level(), state.stepOrSalaryLevel());
    }

    private WageProjectionState policeConvertedCivilServantState(
            WageProjectionState state,
            String positionCode,
            String positionName) {
        return new WageProjectionState(
                positionCode,
                positionName,
                String.valueOf(payrollRepository.intValue(state.level()) + 7),
                state.stepOrSalaryLevel(),
                "0",
                state.levelStartYear(),
                state.stepStartYear(),
                "GRADE",
                state.salaryStandardYearMonth(),
                state.allowanceStandardYearMonth(),
                state.rankName(),
                state.rankAllowanceStandardYearMonth(),
                state.rankAllowance(),
                state.rankAllowanceCategory());
    }

    private WageProjectionState projectPoliceToCivilServantChangeFromState(
            WageProjectionState state,
            String positionCode,
            String positionName,
            String calculationYear,
            String salaryStandardYearMonth,
            PositionLevelRange levelRange) {
        int convertedLevel = payrollRepository.intValue(state.level()) + 7;
        String baseLevel = String.valueOf(convertedLevel);
        String promotedLevel = baseLevel;
        String promotedStep = state.stepOrSalaryLevel();
        boolean eligible = levelRange != null && convertedLevel > 0 && payrollRepository.intValue(promotedStep) > 0;
        if (eligible && isHigherPositionLayer(state.positionCode(), positionCode)) {
            if (convertedLevel > levelRange.minimumLevel()) {
                promotedLevel = String.valueOf(levelRange.minimumLevel());
            } else {
                promotedLevel = String.valueOf(Math.max(1, convertedLevel - 1));
            }
            if (!promotedLevel.equals(baseLevel)) {
                int currentGradeSalary = payrollRepository.civilServantGradeSalary(
                        baseLevel, promotedStep, state.gradeStepDifferenceCount(), salaryStandardYearMonth);
                promotedStep = firstHigherGradeStep(promotedLevel, currentGradeSalary, salaryStandardYearMonth);
            }
        }
        int promotedLevels = Math.max(0, convertedLevel - payrollRepository.intValue(promotedLevel));
        boolean gradeIncreaseExceedsStepDifference = eligible && promotedLevels > 0
                && gradeIncreaseExceedsStepDifference(baseLevel, state.stepOrSalaryLevel(), promotedLevel, salaryStandardYearMonth);
        String nextLevelStartYear = promotedLevels >= 2 ? calculationYear : state.levelStartYear();
        String nextStepStartYear = gradeIncreaseExceedsStepDifference ? calculationYear : state.stepStartYear();
        return new WageProjectionState(
                positionCode,
                positionName,
                promotedLevel,
                promotedStep,
                "0",
                nextLevelStartYear,
                nextStepStartYear,
                "GRADE",
                state.salaryStandardYearMonth(),
                state.allowanceStandardYearMonth(),
                state.rankName(),
                state.rankAllowanceStandardYearMonth(),
                state.rankAllowance(),
                state.rankAllowanceCategory());
    }

    private boolean specialWageReformLevelPromotionDue(
            WageProjectionState state,
            WageProjectionStart start,
            PayrollHistorySnapshot latest,
            int year) {
        if (year < 2007 || year > 2010 || start.wageReformYears() <= 0 || yearOf(state.levelStartYear()) > 2006) {
            return false;
        }
        if (positionLayer(state.positionCode()) != positionLayer(start.positionCode())) {
            return false;
        }
        if (payrollRepository.countQualifiedAssessmentYears(latest.organizationCode(), latest.personCode(), year - 1, year - 1) <= 0) {
            return false;
        }
        int appointmentYears = Math.max(1, 2006 - yearOf(start.positionStartYearMonth()) + 1) + (year - 2006);
        int reformYears = start.wageReformYears() + (year - 2006);
        int currentLevel = payrollRepository.intValue(state.level());
        if (currentLevel <= 1) {
            return false;
        }
        return payrollRepository.findWageReformStandard(start.positionCode(), appointmentYears, reformYears)
                .map(standard -> payrollRepository.intValue(standard.convertedLevel()) < currentLevel)
                .orElse(false);
    }

    private String cappedPromotedLevel(String positionCode, String promotedLevel) {
        PositionLevelRange range = payrollRepository.findPositionLevelRange(positionCode).orElse(null);
        if (range == null || promotedLevel == null || promotedLevel.isBlank()) {
            return promotedLevel;
        }
        int promoted = payrollRepository.intValue(promotedLevel);
        if (range.maximumLevel() != null && promoted < range.maximumLevel()) {
            return String.valueOf(range.maximumLevel());
        }
        return promotedLevel;
    }

    private boolean atHighestPositionLevel(String positionCode, String level) {
        PositionLevelRange range = payrollRepository.findPositionLevelRange(positionCode).orElse(null);
        if (range == null || range.maximumLevel() == null) {
            return false;
        }
        int currentLevel = payrollRepository.intValue(level);
        return currentLevel > 0 && currentLevel <= range.maximumLevel();
    }

    private record WageProjectionState(
            String positionCode,
            String positionName,
            String level,
            String stepOrSalaryLevel,
            String gradeStepDifferenceCount,
            String levelStartYear,
            String stepStartYear,
            String baseSalarySource,
            String salaryStandardYearMonth,
            String allowanceStandardYearMonth,
            String rankName,
            String rankAllowanceStandardYearMonth,
            Integer rankAllowance,
            String rankAllowanceCategory) {

        WageProjectionState withRankAllowance(String rankName, String standardYearMonth, Integer amount, String category) {
            return new WageProjectionState(positionCode, positionName, level, stepOrSalaryLevel, gradeStepDifferenceCount, levelStartYear, stepStartYear,
                    baseSalarySource, salaryStandardYearMonth, allowanceStandardYearMonth,
                    rankName, standardYearMonth, amount, category);
        }

        WageProjectionState withSalaryStandard(String standardYearMonth) {
            return new WageProjectionState(positionCode, positionName, level, stepOrSalaryLevel, gradeStepDifferenceCount, levelStartYear, stepStartYear,
                    baseSalarySource, standardYearMonth, allowanceStandardYearMonth,
                    rankName, rankAllowanceStandardYearMonth, rankAllowance, rankAllowanceCategory);
        }

        WageProjectionState withAllowanceStandard(String standardYearMonth) {
            return new WageProjectionState(positionCode, positionName, level, stepOrSalaryLevel, gradeStepDifferenceCount, levelStartYear, stepStartYear,
                    baseSalarySource, salaryStandardYearMonth, standardYearMonth,
                    rankName, rankAllowanceStandardYearMonth, rankAllowance, rankAllowanceCategory);
        }
    }

    private WageProjectionState projectionWithLevelStep(
            WageProjectionState state,
            String level,
            String step,
            String levelStartYear,
            String stepStartYear) {
        return new WageProjectionState(
                state.positionCode(),
                state.positionName(),
                level,
                step,
                "0",
                levelStartYear,
                stepStartYear,
                baseSalarySource(state.positionCode(), level),
                state.salaryStandardYearMonth(),
                state.allowanceStandardYearMonth(),
                state.rankName(),
                state.rankAllowanceStandardYearMonth(),
                state.rankAllowance(),
                state.rankAllowanceCategory());
    }

    private WageProjectionState projectionWithStep(WageProjectionState state, String step, String stepStartYear) {
        return projectionWithLevelStep(state, state.level(), step, state.levelStartYear(), stepStartYear);
    }

    private WageProjectionState projectionWithStepDifference(
            WageProjectionState state,
            String gradeStepDifferenceCount,
            String stepStartYear) {
        return new WageProjectionState(
                state.positionCode(),
                state.positionName(),
                state.level(),
                state.stepOrSalaryLevel(),
                gradeStepDifferenceCount,
                state.levelStartYear(),
                stepStartYear,
                state.baseSalarySource(),
                state.salaryStandardYearMonth(),
                state.allowanceStandardYearMonth(),
                state.rankName(),
                state.rankAllowanceStandardYearMonth(),
                state.rankAllowance(),
                state.rankAllowanceCategory());
    }

    private WageProjectionState promoteCivilServantGradeStep(
            WageProjectionState state,
            int year,
            List<String> lines,
            String reasonPrefix) {
        return promoteCivilServantGradeStep(state, year, lines, reasonPrefix, false);
    }

    private WageProjectionState promoteCivilServantGradeStep(
            WageProjectionState state,
            int year,
            List<String> lines,
            String reasonPrefix,
            boolean resetLevelAssessmentYear) {
        if (!"GRADE".equals(resolvedBaseSalarySource(state))) {
            String promotedStep = String.valueOf(payrollRepository.intValue(state.stepOrSalaryLevel()) + 1);
            lines.add(year + " 年：" + reasonPrefix + "，晋升档次到 "
                    + levelStepDisplay(state.baseSalarySource(), state.level(), promotedStep) + "。");
            return projectionWithStep(state, promotedStep, String.valueOf(year));
        }
        String previousStep = state.stepOrSalaryLevel();
        int currentStep = payrollRepository.intValue(previousStep);
        int highestStep = payrollRepository.highestGradeStepForLevel(state.level());
        if (currentStep >= highestStep) {
            String promotedDifference = String.valueOf(normalizedGradeStepDifferenceCount(state) + 1);
            lines.add(year + " 年：" + reasonPrefix + "，已达到本级别最高档次，增加档差工资，档差个数 "
                    + promotedDifference + "（"
                    + levelStepDisplay(state.baseSalarySource(), state.level(), previousStep, promotedDifference) + "）。");
            return projectionWithStepDifference(state, promotedDifference, String.valueOf(year));
        }
        String promotedStep = String.valueOf(currentStep + 1);
        lines.add(year + " 年：" + reasonPrefix + "，晋升档次到 "
                + levelStepDisplay(state.baseSalarySource(), state.level(), promotedStep) + "。");
        if (resetLevelAssessmentYear) {
            return new WageProjectionState(
                    state.positionCode(),
                    state.positionName(),
                    state.level(),
                    promotedStep,
                    "0",
                    String.valueOf(year),
                    state.stepStartYear(),
                    state.baseSalarySource(),
                    state.salaryStandardYearMonth(),
                    state.allowanceStandardYearMonth(),
                    state.rankName(),
                    state.rankAllowanceStandardYearMonth(),
                    state.rankAllowance(),
                    state.rankAllowanceCategory());
        }
        return projectionWithStep(state, promotedStep, String.valueOf(year));
    }

    private int normalizedGradeStepDifferenceCount(WageProjectionState state) {
        return payrollRepository.intValue(state.gradeStepDifferenceCount());
    }

    private String resolvedBaseSalarySource(WageProjectionState state) {
        return baseSalarySource(state.positionCode(), state.level());
    }

    private boolean supportsGradePromotion(WageProjectionState state) {
        return "GRADE".equals(resolvedBaseSalarySource(state));
    }

    private int gradeSalaryAmount(
            WageProjectionState state,
            String level,
            String step,
            String standardYearMonth) {
        return switch (baseSalarySource(state.positionCode(), level)) {
            case "POLICE_GRADE" -> payrollRepository.policeOfficerGradeSalary(
                    level, policeGradeStep(step, state.gradeStepDifferenceCount()), standardYearMonth);
            case "GRADE" -> payrollRepository.civilServantGradeSalary(
                    level, step, state.gradeStepDifferenceCount(), standardYearMonth);
            default -> payrollRepository.gradeSalary(level, step, standardYearMonth);
        };
    }

    private record RankAllowanceState(
            String rankName,
            String standardYearMonth,
            Integer amount,
            String category) {
    }

    private record WageProjectionEvent(
            String period,
            PositionChangeCandidate position,
            RankAllowanceChange rankChange,
            String basicSalaryStandardYearMonth,
            String rankStandardYearMonth,
            EducationPromotionSource educationChange,
            String organizationCode,
            String personCode,
            int sortOrder) {

        static WageProjectionEvent position(String period, PositionChangeCandidate position, String organizationCode, String personCode) {
            return new WageProjectionEvent(period, position, null, null, null, null, organizationCode, personCode, 3);
        }

        static WageProjectionEvent rankStandard(String period, String standardYearMonth, String organizationCode, String personCode) {
            return new WageProjectionEvent(period, null, null, null, standardYearMonth, null, organizationCode, personCode, 1);
        }

        static WageProjectionEvent rankChange(String period, RankAllowanceChange change, String organizationCode, String personCode) {
            return new WageProjectionEvent(period, null, change, null, null, null, organizationCode, personCode, 4);
        }

        static WageProjectionEvent basicSalaryStandard(
                String period,
                String standardYearMonth,
                String organizationCode,
                String personCode) {
            return new WageProjectionEvent(period, null, null, standardYearMonth, null, null, organizationCode, personCode, 0);
        }

        static WageProjectionEvent education(
                String period,
                EducationPromotionSource education,
                String organizationCode,
                String personCode) {
            return new WageProjectionEvent(period, null, null, null, null, education, organizationCode, personCode, 2);
        }
    }

    private record EducationPromotionResolution(
            boolean eligible,
            String promotedPositionCode,
            String promotedLevel,
            String promotedGradeStep,
            String promotedGradeStepDifference,
            int currentBasicSalary,
            int promotedBasicSalary,
            String nextLevelAssessmentStartYear,
            String nextStepAssessmentStartYear,
            String note) {

        static EducationPromotionResolution ineligible(String note) {
            return new EducationPromotionResolution(
                    false, null, null, null, "0", 0, 0, null, null, note);
        }
    }

    private record RegularizationSalaryPosition(
            EducationRegularizationStandard standard,
            PositionChangeCandidate appointedPosition,
            String salaryPositionCode,
            String salaryPositionName,
            boolean institutionRegularization,
            boolean salaryPositionFromStandard) {
        private static RegularizationSalaryPosition empty() {
            return new RegularizationSalaryPosition(null, null, null, null, false, false);
        }
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

    public PayrollProjectionAuditSummary projectionAuditSummary(String organizationCode, PageRequest pageRequest) {
        var scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        List<PayrollProjectionPersonAudit> audits = payrollRepository
                .findPersonnelUidsWithPayrollHistory(scope, pageRequest)
                .stream()
                .map(this::safeProjectionPersonAudit)
                .toList();
        List<PayrollProjectionPersonAudit> differences = audits.stream()
                .filter(audit -> !Boolean.TRUE.equals(audit.latestMatched()) || audit.historyMismatchCount() > 0)
                .toList();
        BigDecimal maxAbsoluteDifference = differences.stream()
                .map(audit -> audit.latestTotalDifference() == null ? BigDecimal.ZERO : audit.latestTotalDifference().abs())
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        int historyMismatchPersonCount = (int) audits.stream()
                .filter(audit -> audit.historyMismatchCount() > 0)
                .count();
        int totalHistoryRecordsCompared = audits.stream()
                .mapToInt(PayrollProjectionPersonAudit::historyRecordCount)
                .sum();
        int totalHistoryRecordMismatches = audits.stream()
                .mapToInt(PayrollProjectionPersonAudit::historyMismatchCount)
                .sum();
        int latestDifferenceCount = (int) audits.stream()
                .filter(audit -> !Boolean.TRUE.equals(audit.latestMatched()))
                .count();
        return new PayrollProjectionAuditSummary(
                payrollRepository.countPersonnelWithPayrollHistory(scope),
                audits.size(),
                latestDifferenceCount,
                historyMismatchPersonCount,
                totalHistoryRecordsCompared,
                totalHistoryRecordMismatches,
                maxAbsoluteDifference,
                differences);
    }

    public List<PayrollHistoryProjectionAudit> projectionHistoryAudits(int uid) {
        PayrollHistorySnapshot latest = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        accessControlService.requireOrganization(latest.organizationCode());
        return payrollRepository.findHistoryChain(latest.organizationCode(), latest.personCode()).stream()
                .map(record -> auditHistoryWithProjection(uid, record))
                .toList();
    }

    public PayrollProjectionAuditExportData buildProjectionAuditExport(String organizationCode) {
        return buildProjectionAuditExport(organizationCode, false);
    }

    public PayrollProjectionAuditExportData buildProjectionAuditExport(String organizationCode, boolean mismatchesOnly) {
        var scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        List<Integer> uids = payrollRepository.findAllPersonnelUidsWithPayrollHistory(scope);
        long totalPersonnel = payrollRepository.countPersonnelWithPayrollHistory(scope);
        log.info("Starting full projection audit export for {} personnel", uids.size());
        List<PayrollProjectionPersonAudit> summaries = new ArrayList<>(uids.size());
        List<PayrollProjectionAuditDetailRow> details = new ArrayList<>();
        for (int index = 0; index < uids.size(); index++) {
            int uid = uids.get(index);
            if (index == 0 || (index + 1) % 25 == 0 || index + 1 == uids.size()) {
                log.info("Projection audit export progress: {}/{}", index + 1, uids.size());
            }
            PayrollHistorySnapshot latest;
            try {
                latest = payrollRepository.findLatestHistory(uid)
                        .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
                accessControlService.requireOrganization(latest.organizationCode());
            } catch (RuntimeException e) {
                log.warn("Projection audit export skipped for uid {}: {}", uid, e.getMessage());
                summaries.add(skippedProjectionPersonAudit(uid, e.getMessage()));
                continue;
            }
            List<PayrollHistorySnapshot> historyChain = payrollRepository.findHistoryChain(
                    latest.organizationCode(),
                    latest.personCode());
            List<PayrollHistoryProjectionAudit> historyAudits = historyChain.stream()
                    .map(record -> auditHistoryWithProjection(uid, record))
                    .toList();
            summaries.add(buildProjectionPersonAudit(uid, latest, historyAudits));
            for (PayrollHistoryProjectionAudit audit : historyAudits) {
                if (!mismatchesOnly || !Boolean.TRUE.equals(audit.matched())) {
                    details.add(PayrollProjectionAuditDetailRow.of(
                            uid,
                            latest.organizationCode(),
                            latest.personCode(),
                            latest.name(),
                            audit));
                }
            }
        }
        int latestDifferenceCount = (int) summaries.stream()
                .filter(audit -> !Boolean.TRUE.equals(audit.latestMatched()))
                .count();
        int historyMismatchPersonCount = (int) summaries.stream()
                .filter(audit -> audit.historyMismatchCount() > 0)
                .count();
        int totalHistoryRecordsCompared = summaries.stream()
                .mapToInt(PayrollProjectionPersonAudit::historyRecordCount)
                .sum();
        int totalHistoryRecordMismatches = summaries.stream()
                .mapToInt(PayrollProjectionPersonAudit::historyMismatchCount)
                .sum();
        log.info(
                "Projection audit export finished: {} personnel, {} history rows, {} mismatches",
                summaries.size(),
                details.size(),
                totalHistoryRecordMismatches);
        return new PayrollProjectionAuditExportData(
                totalPersonnel,
                summaries.size(),
                latestDifferenceCount,
                historyMismatchPersonCount,
                totalHistoryRecordsCompared,
                totalHistoryRecordMismatches,
                summaries,
                details);
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
                .map(field -> componentComparison(field.fieldName(), approvalCaption(field.fieldName(), field.caption()), beforeValues.orElse(null), afterValues))
                .toList());
        if (fields.stream().noneMatch(field -> "HJ2".equalsIgnoreCase(field.fieldName()))) {
            components.add(componentComparison("HJ2", "合计", beforeValues.orElse(null), afterValues));
        }
        return new PayrollChangeComparison(
                payrollHistoryId,
                beforeValues.map(values -> textValue(values, "id")).orElse(null),
                organizationCode,
                textValue(afterValues, "approval_dwmc"),
                textValue(afterValues, "approval_dwsx"),
                textValue(afterValues, "approval_jxbl"),
                textValue(afterValues, "grbm"),
                textValue(afterValues, "xm"),
                textValue(afterValues, "approval_sfzh"),
                textValue(afterValues, "approval_xb"),
                textValue(afterValues, "approval_csny"),
                textValue(afterValues, "approval_zgxl"),
                textValue(afterValues, "approval_cjgzny"),
                intValue(afterValues, "approval_gznx"),
                textValue(afterValues, "approval_dah"),
                textValue(afterValues, "approval_rzny"),
                textValue(afterValues, "jsnf") + textValue(afterValues, "jsyf"),
                textValue(afterValues, "jslb"),
                beforeValues.map(values -> textValue(values, "zwgw2")).orElse(null),
                textValue(afterValues, "zwgw2"),
                beforeValues.map(values -> textValue(values, "jbgzjb2")).orElse(null),
                textValue(afterValues, "jbgzjb2"),
                beforeValues.map(values -> textValue(values, "zwgzdc2")).orElse(null),
                textValue(afterValues, "zwgzdc2"),
                beforeValues.map(values -> textValue(values, "jsnf") + textValue(values, "jsyf")).orElse(null),
                beforeValues.map(values -> textValue(values, "jslb")).orElse(null),
                components);
    }

    private Integer intValue(Map<String, Object> values, String fieldName) {
        String text = textValue(values, fieldName);
        if (text.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
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

    @Transactional
    public PromotionActionResult applyPositionChangePromotion(String payrollHistoryId) {
        int uid = requireCurrentHistoryUid(payrollHistoryId);
        PayrollHistorySnapshot latest = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        PositionChangePromotionPreview preview = positionChangePromotionPreview(uid);
        if (!Boolean.TRUE.equals(preview.eligible()) || preview.totalIncrease() == null) {
            throw new IllegalArgumentException("当前工资记录不满足职务变化处理条件。");
        }
        String effectivePeriod = preview.effectivePeriod() == null || preview.effectivePeriod().isBlank()
                ? latest.calculationYear() + latest.calculationMonth()
                : preview.effectivePeriod();
        String normalizedPeriod = effectivePeriod.replace(".", "");
        if (normalizedPeriod.length() < 6) {
            throw new IllegalArgumentException("职务变化执行年月不完整。");
        }
        int pgbc = Math.max(0, nullToZero(latest.storedPgbc()) + nullToZero(preview.pgbcRetainedAmount()) - nullToZero(preview.pgbcOffsetAmount()));
        PositionChangeHistoryMutation mutation = new PositionChangeHistoryMutation(
                normalizedPeriod.substring(0, 4),
                normalizedPeriod.substring(4, 6),
                preview.changeType(),
                preview.nextStepAssessmentStartYear(),
                preview.nextLevelAssessmentStartYear(),
                preview.newPositionCode(),
                preview.newPositionName(),
                preview.newPositionSalary(),
                preview.promotedStep(),
                preview.promotedLevel(),
                "0",
                preview.promotedGradeSalary(),
                pgbc,
                nullToZero(latest.storedTotal()) + nullToZero(preview.totalIncrease()));
        String newId = payrollRepository.createPositionChangeHistoryFromLatest(uid, mutation);
        payrollRepository.updatePositionPromotionFlag(
                latest.organizationCode(),
                latest.personCode(),
                preview.positionStartYearMonth(),
                preview.newPositionCode(),
                "1");
        return new PromotionActionResult(newId, payrollHistoryId, preview.changeType(), "职务变化处理完成。");
    }

    @Transactional
    public PromotionActionResult rollbackPositionChangePromotion(String payrollHistoryId) {
        PayrollHistorySnapshot current = payrollRepository.findCurrentHistoryById(payrollHistoryId)
                .orElseThrow(() -> new NotFoundException("Current payroll history not found: " + payrollHistoryId));
        payrollRepository.updatePositionPromotionFlag(
                current.organizationCode(),
                current.personCode(),
                current.positionStartYearMonth(),
                current.positionCode(),
                "");
        return rollbackPromotion(
                payrollHistoryId,
                Set.of("同序列职务变化", "职务变化", "警员套改", "法检套改", "职级套改", "事业岗位变动", "转换序列"),
                "职务变化已还原。");
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

    private BasicPayrollCalculation basicCalculation(WageProjectionState state, PayrollHistorySnapshot latest) {
        String standardYearMonth = state.salaryStandardYearMonth();
        String positionCode = state.positionCode();
        String resolvedBaseSalarySource = resolvedBaseSalarySource(state);
        Integer gradeSalary = switch (resolvedBaseSalarySource) {
            case "GRADE" -> payrollRepository.civilServantGradeSalary(
                    state.level(), state.stepOrSalaryLevel(), state.gradeStepDifferenceCount(), standardYearMonth);
            case "POLICE_GRADE" -> payrollRepository.policeOfficerGradeSalary(
                    state.level(),
                    policeGradeStep(state.stepOrSalaryLevel(), state.gradeStepDifferenceCount()),
                    standardYearMonth);
            default -> null;
        };
        Integer salaryLevelSalary = "SALARY_LEVEL".equals(resolvedBaseSalarySource)
                ? payrollRepository.salaryLevelSalary(state.stepOrSalaryLevel(), "0", standardYearMonth, positionCode)
                : payrollRepository.salaryLevelSalary(
                latest.positionSalaryGrade(),
                latest.gradeSalaryStep(),
                standardYearMonth,
                positionCode);
        Integer technicalGradeSalary = payrollRepository.technicalGradeSalary(positionCode, standardYearMonth);
        Integer positionSalary = payrollRepository.positionSalary(positionCode, standardYearMonth)
                + payrollRepository.positionGradeSalary(
                positionCode,
                state.stepOrSalaryLevel(),
                "0",
                standardYearMonth);
        Integer selectedBaseSalary = switch (resolvedBaseSalarySource) {
            case "GRADE" -> gradeSalary;
            case "POLICE_GRADE" -> gradeSalary;
            default -> salaryLevelSalary;
        };
        return new BasicPayrollCalculation(
                standardYearMonth,
                positionCode,
                payrollRepository.mapPositionSalaryCode(positionCode),
                latest.positionSalaryGrade(),
                state.level(),
                state.stepOrSalaryLevel(),
                positionSalary,
                gradeSalary,
                salaryLevelSalary,
                technicalGradeSalary,
                resolvedBaseSalarySource,
                selectedBaseSalary,
                latest.storedPositionSalary(),
                latest.storedGradeSalary(),
                latest.storedTechnicalGradeSalary(),
                latest.storedTotal());
    }

    private AdditionalPayrollCalculation additionalCalculation(WageProjectionState state, PayrollHistorySnapshot latest) {
        return new AdditionalPayrollCalculation(
                state.rankAllowanceStandardYearMonth(),
                state.rankName(),
                nullToZero(state.rankAllowance()),
                latest.floatingStep(),
                payrollRepository.floatingSalary(
                        state.salaryStandardYearMonth(),
                        state.positionCode(),
                        latest.positionSalaryGrade(),
                        latest.floatingStep()),
                selectedBonusBalance(latest),
                latest.postAllowanceStandardYearMonth(),
                latest.postAllowanceCategory(),
                latest.storedPostAllowance(),
                retainedSpecialPostAllowance(latest),
                latest.storedRankAllowance(),
                latest.storedFloatingSalary(),
                latest.storedBonusBalance(),
                latest.storedPostAllowance(),
                latest.storedRetainedSpecialPostAllowance());
    }

    private BasicPayrollCalculation basicCalculation(PayrollHistorySnapshot history) {
        String standardYearMonth = history.salaryStandardYearMonth();
        String positionCode = history.positionCode();
        Integer gradeSalary = payrollRepository.civilServantGradeSalary(
                history.gradeSalaryLevel(),
                history.positionSalaryGrade(),
                history.gradeSalaryStep(),
                standardYearMonth);
        Integer salaryLevelSalary = payrollRepository.salaryLevelSalary(
                history.positionSalaryGrade(),
                history.gradeSalaryStep(),
                standardYearMonth,
                positionCode);
        Integer technicalGradeSalary = payrollRepository.technicalGradeSalary(positionCode, standardYearMonth);
        Integer policeGradeSalary = payrollRepository.policeOfficerGradeSalary(
                history.gradeSalaryLevel(),
                policeGradeStep(history),
                standardYearMonth);
        Integer positionSalary = payrollRepository.positionSalary(positionCode, standardYearMonth)
                + payrollRepository.positionGradeSalary(
                positionCode,
                history.positionSalaryGrade(),
                history.gradeSalaryStep(),
                standardYearMonth);
        String baseSalarySource = baseSalarySource(positionCode, history.gradeSalaryLevel());
        Integer selectedBaseSalary = switch (baseSalarySource) {
            case "GRADE" -> gradeSalary;
            case "POLICE_GRADE" -> policeGradeSalary;
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
                preview("NJBT", "农教补贴", allowance.yearAllowance(), "AUTO"),
                preview("JXJT", RANK_ALLOWANCE_COMPONENT_CAPTION, additional.rankAllowance(), "AUTO"),
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

    private PayrollProjectionPersonAudit safeProjectionPersonAudit(int uid) {
        try {
            return projectionPersonAudit(uid);
        } catch (RuntimeException e) {
            log.warn("Projection audit skipped for uid {}: {}", uid, e.getMessage());
            return skippedProjectionPersonAudit(uid, e.getMessage());
        }
    }

    private PayrollProjectionPersonAudit skippedProjectionPersonAudit(int uid, String note) {
        return payrollRepository.findPayrollPersonnelRef(uid)
                .map(person -> new PayrollProjectionPersonAudit(
                        person.uid(),
                        person.organizationCode(),
                        person.personCode(),
                        person.name(),
                        "",
                        false,
                        note,
                        null,
                        null,
                        null,
                        false,
                        0,
                        0,
                        List.of()))
                .orElse(new PayrollProjectionPersonAudit(
                        uid, "", "", "", "", false, note, null, null, null, false, 0, 0, List.of()));
    }

    private PayrollProjectionPersonAudit projectionPersonAudit(int uid) {
        PayrollHistorySnapshot latest = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        accessControlService.requireOrganization(latest.organizationCode());
        List<PayrollHistorySnapshot> historyChain = payrollRepository.findHistoryChain(
                latest.organizationCode(),
                latest.personCode());
        List<PayrollHistoryProjectionAudit> historyAudits = historyChain.stream()
                .map(record -> auditHistoryWithProjection(uid, record))
                .toList();
        return buildProjectionPersonAudit(uid, latest, historyAudits);
    }

    private PayrollProjectionPersonAudit buildProjectionPersonAudit(
            int uid,
            PayrollHistorySnapshot latest,
            List<PayrollHistoryProjectionAudit> historyAudits) {
        String latestPeriod = historyCalculationPeriod(latest);
        PayrollHistoryProjectionAudit latestAudit = historyAudits.stream()
                .filter(audit -> latest.id().equals(audit.historyId()))
                .findFirst()
                .orElseGet(() -> auditHistoryWithProjection(uid, latest));
        List<PayrollHistoryProjectionAudit> historyMismatches = historyAudits.stream()
                .filter(audit -> !Boolean.TRUE.equals(audit.matched()))
                .toList();
        return new PayrollProjectionPersonAudit(
                uid,
                latest.organizationCode(),
                latest.personCode(),
                latest.name(),
                latestPeriod,
                latestAudit.projectionEligible(),
                latestAudit.note(),
                latestAudit.storedTotal(),
                latestAudit.projectedTotal(),
                latestAudit.totalDifference(),
                latestAudit.matched(),
                historyAudits.size(),
                historyMismatches.size(),
                historyMismatches);
    }

    private PayrollHistoryProjectionAudit auditHistoryWithProjection(int uid, PayrollHistorySnapshot record) {
        String period = historyCalculationPeriod(record);
        WageProjectionRun projection = runWageProjection(uid, period);
        if (!projection.eligible()) {
            String note = projection.start() != null && !projection.start().eligible()
                    ? projection.start().note()
                    : projection.lines().stream()
                            .filter(line -> line != null && !line.isBlank())
                            .reduce((first, second) -> second)
                            .orElse("当前年月无法完成工资推算。");
            return new PayrollHistoryProjectionAudit(
                    record.id(),
                    period,
                    record.calculationType(),
                    false,
                    note,
                    false,
                    record.storedTotal(),
                    null,
                    null,
                    List.of(),
                    List.of());
        }
        WageProjectionState state = finalizeProjectionState(
                projection.state(),
                projection.targetPeriod(),
                record.organizationCode());
        Optional<Map<String, Object>> historyValuesOptional = payrollRepository.findHistoryValuesById(
                record.id(),
                record.organizationCode(),
                record.personCode(),
                record.calculationYear(),
                record.calculationMonth());
        if (historyValuesOptional.isEmpty()) {
            return new PayrollHistoryProjectionAudit(
                    record.id(),
                    period,
                    record.calculationType(),
                    true,
                    "调资记录数据无法加载（id=" + record.id() + "）",
                    false,
                    record.storedTotal(),
                    null,
                    null,
                    List.of(),
                    List.of());
        }
        Map<String, Object> historyValues = historyValuesOptional.get();
        List<PayrollComponentValue> components = payrollRepository.findCalculationFields().stream()
                .map(field -> new PayrollComponentValue(
                        field.fieldName(),
                        field.caption(),
                        field.inputMode(),
                        field.allowance(),
                        payrollRepository.decimalValue(historyValues, field.fieldName())))
                .toList();
        BasicPayrollCalculation basicCalculation = basicCalculation(state, record);
        AllowanceCalculation allowanceCalculation = allowanceCalculation(state, record);
        AdditionalPayrollCalculation additionalCalculation = additionalCalculation(state, record);
        PayrollTotalComparison total = totalComparison(
                record,
                components,
                basicCalculation,
                allowanceCalculation,
                additionalCalculation);
        List<String> structureMismatches = projectionStructureMismatches(state, record);
        BigDecimal difference = nullToZero(total.totalDifference());
        boolean matched = difference.compareTo(BigDecimal.ZERO) == 0 && structureMismatches.isEmpty();
        List<PayrollComponentDifference> componentDifferences = total.componentDifferences().stream()
                .filter(component -> component.difference().compareTo(BigDecimal.ZERO) != 0)
                .toList();
        return new PayrollHistoryProjectionAudit(
                record.id(),
                period,
                record.calculationType(),
                true,
                "",
                matched,
                record.storedTotal(),
                total.recalculatedKnownTotal(),
                difference,
                structureMismatches,
                componentDifferences);
    }

    private List<String> projectionStructureMismatches(WageProjectionState state, PayrollHistorySnapshot record) {
        List<String> mismatches = new ArrayList<>();
        addStructureMismatch(mismatches, "岗位编码", state.positionCode(), record.positionCode());
        addStructureMismatch(mismatches, "岗位名称", state.positionName(), record.positionName());
        addStructureMismatch(mismatches, "级别/薪级", state.level(), record.gradeSalaryLevel());
        String baseSalarySource = resolvedBaseSalarySource(state);
        if ("GRADE".equals(baseSalarySource) || "POLICE_GRADE".equals(baseSalarySource)) {
            addStructureMismatch(mismatches, "档次", state.stepOrSalaryLevel(), record.positionSalaryGrade());
            addStructureMismatch(mismatches, "级差", state.gradeStepDifferenceCount(), record.gradeSalaryStep());
        } else {
            addStructureMismatch(mismatches, "薪级", state.stepOrSalaryLevel(), record.positionSalaryGrade());
        }
        addStructureMismatch(mismatches, "级别考核起年", state.levelStartYear(), record.levelAssessmentStartYear());
        addStructureMismatch(mismatches, "档次考核起年", state.stepStartYear(), record.stepAssessmentStartYear());
        return mismatches;
    }

    private void addStructureMismatch(List<String> mismatches, String label, String projected, String stored) {
        if (!normalizedEquals(projected, stored)) {
            mismatches.add(label + "：推算=" + displayText(projected) + "，调资=" + displayText(stored));
        }
    }

    private String displayText(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }

    private String historyCalculationPeriod(PayrollHistorySnapshot history) {
        return projectionPeriod(history.calculationYear() + history.calculationMonth());
    }

    private String baseSalarySource(String positionCode) {
        return baseSalarySource(positionCode, null);
    }

    private String baseSalarySource(String positionCode, String gradeLevel) {
        if (positionCode == null || positionCode.length() < 2) {
            return "SALARY_LEVEL";
        }
        return switch (positionCode.substring(0, 2)) {
            case "01", "02", "04", "23", "24", "25", "26", "27", "28" -> "GRADE";
            case "21", "22" -> "POLICE_GRADE";
            default -> "SALARY_LEVEL";
        };
    }

    private String policeGradeStep(PayrollHistorySnapshot history) {
        int combined = payrollRepository.intValue(history.positionSalaryGrade())
                + payrollRepository.intValue(history.gradeSalaryStep());
        if (combined > 0) {
            return String.valueOf(combined);
        }
        if (emptyToNull(history.gradeSalaryStep()) != null) {
            return history.gradeSalaryStep();
        }
        return history.positionSalaryGrade();
    }

    private String policeGradeStep(String positionSalaryGrade, String gradeSalaryStep) {
        int combined = payrollRepository.intValue(positionSalaryGrade) + payrollRepository.intValue(gradeSalaryStep);
        return combined > 0 ? String.valueOf(combined) : "0";
    }

    private RankAllowanceState initialRankAllowanceState(PayrollHistorySnapshot latest, String startPeriod) {
        RankAllowanceChange rank = payrollRepository
                .findRankAllowanceAtOrBefore(latest.organizationCode(), latest.personCode(), startPeriod)
                .orElse(new RankAllowanceChange(latest.rankName(), "", ""));
        String standardYearMonth = payrollRepository.latestRankAllowanceStandardAtOrBefore(startPeriod);
        if (emptyToNull(standardYearMonth) == null) {
            standardYearMonth = latest.rankAllowanceStandardYearMonth();
        }
        String category = rankAllowanceCategory(rank.rankName());
        String standardLb = payrollRepository.resolveRankAllowanceStandardLb(rank.rankName(), rank.category());
        int amount = payrollRepository.rankAllowanceByRank(standardYearMonth, rank.rankName(), standardLb);
        if (amount == 0
                && normalizedEquals(rank.rankName(), latest.rankName())
                && normalizedEquals(standardYearMonth, latest.rankAllowanceStandardYearMonth())) {
            amount = nullToZero(latest.storedRankAllowance());
        }
        return new RankAllowanceState(rank.rankName(), standardYearMonth, amount, category);
    }

    private String rankAllowanceCategory(String rankName) {
        String normalized = rankName == null ? "" : rankName.trim();
        if (normalized.contains("警")) {
            return "警";
        }
        if (normalized.contains("法")) {
            return "法";
        }
        if (normalized.contains("检")) {
            return "检";
        }
        if (normalized.contains("监")) {
            return "监";
        }
        return "";
    }

    private String rankAllowanceTypeName(String rankName) {
        return switch (rankAllowanceCategory(rankName)) {
            case "警" -> "警衔";
            case "法" -> "审判";
            case "检" -> "检察";
            case "监" -> "监察";
            default -> "等级";
        };
    }

    private String rankAllowanceTitle(String rankName) {
        return rankAllowanceTypeName(rankName) + "津贴";
    }

    private String rankAllowanceChangeTitle(String rankName) {
        return rankAllowanceTypeName(rankName) + "变化";
    }

    private String rankAllowanceStandardTitle(String rankName) {
        return "调整" + rankAllowanceTitle(rankName);
    }

    private AllowanceCalculation allowanceCalculation(PayrollHistorySnapshot history) {
        return allowanceCalculation(history, history.positionCode(), history.allowanceStandardYearMonth());
    }

    private AllowanceCalculation allowanceCalculation(WageProjectionState state, PayrollHistorySnapshot latest) {
        return allowanceCalculation(latest, state.positionCode(), state.allowanceStandardYearMonth());
    }

    private AllowanceCalculation allowanceCalculation(
            PayrollHistorySnapshot history,
            String positionCode,
            String allowanceStandardYearMonth) {
        BigDecimal performanceAllowance;
        int subsidyAllowance;
        if (performanceAndSubsidyDisabled(history)) {
            performanceAllowance = BigDecimal.ZERO;
            subsidyAllowance = 0;
        } else {
            performanceAllowance = payrollRepository.performanceAllowance(
                    history.organizationCode(),
                    positionCode,
                    allowanceStandardYearMonth);
            subsidyAllowance = payrollRepository.subsidyAllowance(
                    history.organizationCode(), positionCode, allowanceStandardYearMonth);
        }
        int retainedAllowance = payrollRepository.retainedAllowance(positionCode);
        BigDecimal yearAllowance = supportsRuralTeacherYearAllowance(positionCode)
                ? payrollRepository.yearAllowance(history.organizationCode(), allowanceStandardYearMonth)
                : BigDecimal.ZERO;

        return new AllowanceCalculation(
                allowanceStandardYearMonth,
                positionCode,
                payrollRepository.performancePositionCode(positionCode, allowanceStandardYearMonth),
                payrollRepository.subsidyPositionCode(positionCode),
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
        addDifference(differences, "NJBT", "农教补贴", history.storedYearAllowance(), allowance.yearAllowance());
        addDifference(differences, "JXJT", RANK_ALLOWANCE_COMPONENT_CAPTION, history.storedRankAllowance(), additional.rankAllowance());
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
        int currentGradeStep = payrollRepository.intValue(history.positionSalaryGrade());
        int gradeStepDifferenceCount = payrollRepository.intValue(history.gradeSalaryStep());
        int highestGradeStep = payrollRepository.highestGradeStepForLevel(history.gradeSalaryLevel());
        String promotedGradeOrLevel;
        Integer promotedBaseSalary;
        String baseSalarySource = baseSalarySource(history.positionCode(), history.gradeSalaryLevel());
        if ("GRADE".equals(baseSalarySource)) {
            if (currentGradeStep >= highestGradeStep) {
                promotedGradeOrLevel = String.valueOf(currentGradeStep);
                promotedBaseSalary = payrollRepository.civilServantGradeSalary(
                        history.gradeSalaryLevel(),
                        promotedGradeOrLevel,
                        String.valueOf(gradeStepDifferenceCount + 1),
                        history.salaryStandardYearMonth());
            } else {
                promotedGradeOrLevel = String.valueOf(currentGradeStep + 1);
                promotedBaseSalary = payrollRepository.civilServantGradeSalary(
                        history.gradeSalaryLevel(),
                        promotedGradeOrLevel,
                        String.valueOf(gradeStepDifferenceCount),
                        history.salaryStandardYearMonth());
            }
        } else {
            promotedGradeOrLevel = String.valueOf(currentGradeStep + 1);
            promotedBaseSalary = switch (baseSalarySource) {
                case "POLICE_GRADE" -> payrollRepository.policeOfficerGradeSalary(
                        history.gradeSalaryLevel(),
                        policeGradeStep(history),
                        history.salaryStandardYearMonth());
                default -> payrollRepository.salaryLevelSalary(
                        promotedGradeOrLevel,
                        history.gradeSalaryStep(),
                        history.salaryStandardYearMonth(),
                        history.positionCode());
            };
        }
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
                && !"POLICE_GRADE".equals(baseSalarySource);
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
        Integer currentGradeSalary = payrollRepository.civilServantGradeSalary(
                history.gradeSalaryLevel(),
                history.positionSalaryGrade(),
                history.gradeSalaryStep(),
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
        String currentStep = history.positionSalaryGrade();
        String currentStepDifference = history.gradeSalaryStep();
        String promotedLevel = history.gradeSalaryLevel();
        String promotedStep = currentStep;
        String promotedStepDifference = currentStepDifference;
        Integer promotedGradeSalary = currentGradeSalary;
        if (eligible && levelPromotionDue) {
            promotedLevel = String.valueOf(payrollRepository.intValue(history.gradeSalaryLevel()) - 1);
            promotedStep = firstHigherGradeStep(promotedLevel, currentGradeSalary, history.salaryStandardYearMonth());
            promotedStepDifference = "0";
            promotedGradeSalary = payrollRepository.civilServantGradeSalary(
                    promotedLevel, promotedStep, promotedStepDifference, history.salaryStandardYearMonth());
        }
        if (eligible && stepPromotionDue) {
            int step = payrollRepository.intValue(promotedStep);
            int highestStep = payrollRepository.highestGradeStepForLevel(promotedLevel);
            if (step >= highestStep) {
                promotedStepDifference = String.valueOf(payrollRepository.intValue(promotedStepDifference) + 1);
            } else {
                promotedStep = String.valueOf(step + 1);
            }
            promotedGradeSalary = payrollRepository.civilServantGradeSalary(
                    promotedLevel, promotedStep, promotedStepDifference, history.salaryStandardYearMonth());
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
        boolean rankConversion = isRankConversion(currentPositionPrefix, newPositionPrefix);
        boolean rankHighPositionPromotion = rankConversion && isHigherPositionLayer(history.positionCode(), candidate.positionCode());
        boolean institutionPositionChange = isInstitutionPosition(currentPositionPrefix) && isInstitutionPosition(newPositionPrefix);
        String changeType = positionChangeType(
                history.positionCode(),
                candidate.positionCode(),
                sequenceConversion,
                policeOfficerConversion,
                judicialConversion,
                rankConversion,
                institutionPositionChange);
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
        RankConversionResult rankConversionResult = rankConversion
                ? rankConversionResult(history, candidate, levelRange, currentLevel, currentStep, currentGradeSalary, rankHighPositionPromotion)
                : null;
        InstitutionPositionChangeResult institutionResult = institutionPositionChange
                ? institutionPositionChangeResult(history, candidate, currentStep)
                : null;
        String judicialConversionStep = judicialConversion
                ? payrollRepository.judicialConversionStep(history.gradeSalaryLevel(), currentStep, candidate.positionCode())
                : null;
        boolean policeToAdministrativeConversion = isPoliceToAdministrativeConversion(currentPositionPrefix, newPositionPrefix);
        AdministrativeReplayResult administrativeReplayResult = policeToAdministrativeConversion
                ? administrativeReplayResult(history, candidate)
                : null;
        boolean sameSequenceEligible = !sequenceConversion
                && isCivilServantForPositionChange(history.positionCode())
                && isCivilServantForPositionChange(candidate.positionCode())
                && levelRange != null
                && currentLevel > 0;
        boolean eligible = (policeOfficerResult != null && policeOfficerResult.eligible())
                || (rankConversionResult != null && rankConversionResult.eligible())
                || (institutionResult != null && institutionResult.eligible())
                || (judicialConversion && judicialConversionStep != null && !judicialConversionStep.isBlank())
                || (administrativeReplayResult != null && administrativeReplayResult.eligible())
                || sameSequenceEligible;
        String promotedLevel = history.gradeSalaryLevel();
        if (sameSequenceEligible) {
            if (currentLevel > levelRange.minimumLevel()) {
                promotedLevel = String.valueOf(levelRange.minimumLevel());
            } else if (isHigherPositionLayer(history.positionCode(), candidate.positionCode()) && currentLevel >= levelRange.maximumLevel()) {
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
        if (rankConversionResult != null && rankConversionResult.eligible()) {
            promotedLevel = rankConversionResult.promotedLevel();
            promotedStep = rankConversionResult.promotedStep();
            promotedGradeSalary = rankConversionResult.promotedGradeSalary();
        }
        if (judicialConversion && judicialConversionStep != null && !judicialConversionStep.isBlank()) {
            promotedLevel = history.gradeSalaryLevel();
            promotedStep = judicialConversionStep;
            promotedGradeSalary = payrollRepository.gradeSalary(promotedLevel, promotedStep, history.salaryStandardYearMonth());
        }
        if (administrativeReplayResult != null && administrativeReplayResult.eligible()) {
            promotedLevel = administrativeReplayResult.promotedLevel();
            promotedStep = administrativeReplayResult.promotedStep();
            promotedGradeSalary = administrativeReplayResult.promotedGradeSalary();
        }
        if (institutionResult != null && institutionResult.eligible()) {
            promotedLevel = "";
            promotedStep = institutionResult.promotedSalaryLevel();
            promotedGradeSalary = institutionResult.promotedSalary();
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
        int positionSalaryIncrease = nullToZero(newPositionSalary) - nullToZero(currentPositionSalary);
        int pgbcRetainedAmount = rankConversion && positionSalaryIncrease < 0 ? Math.abs(positionSalaryIncrease) : 0;
        int pgbcOffsetAmount = sameSequenceEligible && positionSalaryIncrease > 0
                ? Math.min(positionSalaryIncrease, Math.max(0, nullToZero(history.storedPgbc())))
                : 0;
        int netPositionSalaryIncrease = positionSalaryIncrease + pgbcRetainedAmount - pgbcOffsetAmount;
        int gradeSalaryIncrease = nullToZero(promotedGradeSalary) - nullToZero(currentGradeSalary);
        String note = positionChangePromotionNote(
                history,
                candidate,
                levelRange,
                eligible,
                promotedLevels,
                gradeIncreaseExceedsStepDifference,
                sequenceConversion,
                policeOfficerConversion,
                judicialConversion,
                rankConversion,
                rankConversionResult,
                institutionResult,
                judicialConversionStep,
                administrativeReplayResult,
                policeOfficerResult);
        List<String> explanationLines = positionChangeExplanationLines(
                history,
                candidate,
                changeType,
                currentStep,
                levelRange,
                policeOfficerResult,
                rankConversionResult,
                institutionResult,
                judicialConversionStep,
                administrativeReplayResult,
                promotedLevel,
                promotedStep,
                nextLevelAssessmentStartYear,
                nextStepAssessmentStartYear,
                positionSalaryIncrease,
                pgbcRetainedAmount,
                pgbcOffsetAmount,
                netPositionSalaryIncrease,
                gradeSalaryIncrease,
                gradeIncreaseExceedsStepDifference,
                note);
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
                rankConversion,
                rankHighPositionPromotion,
                institutionPositionChange,
                institutionResult == null ? null : institutionResult.startSalaryLevel(),
                institutionResult == null ? null : institutionResult.promotedSalaryLevel(),
                institutionResult == null ? null : institutionResult.nextStepAssessmentStartYear(),
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
                policeToAdministrativeConversion,
                administrativeReplayResult == null ? null : administrativeReplayResult.replayedLevel(),
                administrativeReplayResult == null ? null : administrativeReplayResult.replayedStep(),
                administrativeReplayResult == null ? null : administrativeReplayResult.levelStartYear(),
                administrativeReplayResult == null ? null : administrativeReplayResult.stepStartYear(),
                promotedLevel,
                promotedStep,
                currentPositionSalary,
                newPositionSalary,
                currentGradeSalary,
                promotedGradeSalary,
                positionSalaryIncrease,
                pgbcRetainedAmount,
                pgbcOffsetAmount,
                netPositionSalaryIncrease,
                gradeSalaryIncrease,
                netPositionSalaryIncrease + gradeSalaryIncrease,
                nextLevelAssessmentStartYear,
                nextStepAssessmentStartYear,
                gradeIncreaseExceedsStepDifference,
                eligible,
                note,
                explanationLines);
    }

    private EducationPromotionPreview educationPromotionPreview(int uid) {
        PayrollHistorySnapshot history = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        EducationPromotionSource education = payrollRepository
                .findLatestEducationForPromotion(history.organizationCode(), history.personCode(), history.calculationYear() + history.calculationMonth())
                .orElse(null);
        EducationPromotionResolution resolution = resolveEducationPromotion(
                history.positionCode(),
                history.positionName(),
                history.gradeSalaryLevel(),
                history.positionSalaryGrade(),
                history.gradeSalaryStep(),
                history.levelAssessmentStartYear(),
                history.stepAssessmentStartYear(),
                history.salaryStandardYearMonth(),
                education);
        EducationRegularizationStandard standard = education == null ? null : payrollRepository
                .findEducationRegularizationStandard(history.positionCode(), education.educationCode())
                .orElse(null);
        Integer currentPositionSalary = payrollRepository.positionSalary(history.positionCode(), history.salaryStandardYearMonth());
        Integer promotedPositionSalary = resolution.promotedPositionCode() == null
                ? currentPositionSalary
                : payrollRepository.positionSalary(resolution.promotedPositionCode(), history.salaryStandardYearMonth());
        boolean institution = isInstitutionPosition(history.positionCode());
        Integer currentGradeSalary = institution
                ? payrollRepository.salaryLevelSalary(
                history.positionSalaryGrade(),
                history.gradeSalaryStep(),
                history.salaryStandardYearMonth(),
                history.positionCode())
                : civilServantGradeSalaryAmount(
                history.positionCode(),
                history.gradeSalaryLevel(),
                history.positionSalaryGrade(),
                history.gradeSalaryStep(),
                history.salaryStandardYearMonth());
        Integer promotedGradeSalary = resolution.promotedLevel() == null
                ? currentGradeSalary
                : institution
                ? payrollRepository.salaryLevelSalary(
                resolution.promotedGradeStep(),
                "0",
                history.salaryStandardYearMonth(),
                resolution.promotedPositionCode())
                : civilServantGradeSalaryAmount(
                resolution.promotedPositionCode(),
                resolution.promotedLevel(),
                resolution.promotedGradeStep(),
                resolution.promotedGradeStepDifference(),
                history.salaryStandardYearMonth());
        int currentBasicSalary = resolution.currentBasicSalary() > 0
                ? resolution.currentBasicSalary()
                : nullToZero(currentPositionSalary) + nullToZero(currentGradeSalary);
        int promotedBasicSalary = resolution.promotedBasicSalary() > 0
                ? resolution.promotedBasicSalary()
                : nullToZero(promotedPositionSalary) + nullToZero(promotedGradeSalary);
        boolean institutionEducationPromotion = education != null
                && standard != null
                && isInstitutionPosition(history.positionCode());
        boolean eligible = institutionEducationPromotion
                ? payrollRepository.intValue(resolution.promotedGradeStep()) > payrollRepository.intValue(history.positionSalaryGrade())
                : resolution.eligible();
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
                resolution.promotedPositionCode() == null ? history.positionCode() : resolution.promotedPositionCode(),
                resolution.promotedLevel() == null ? history.gradeSalaryLevel() : resolution.promotedLevel(),
                resolution.promotedGradeStep() == null ? history.positionSalaryGrade() : resolution.promotedGradeStep(),
                resolution.promotedGradeStepDifference(),
                currentPositionSalary,
                promotedPositionSalary,
                currentGradeSalary,
                promotedGradeSalary,
                nullToZero(promotedPositionSalary) - nullToZero(currentPositionSalary),
                nullToZero(promotedGradeSalary) - nullToZero(currentGradeSalary),
                nullToZero(promotedPositionSalary) - nullToZero(currentPositionSalary)
                        + nullToZero(promotedGradeSalary) - nullToZero(currentGradeSalary),
                resolution.nextLevelAssessmentStartYear(),
                resolution.nextStepAssessmentStartYear(),
                eligible,
                resolution.note());
    }

    private RegularizationPreview regularizationPreview(int uid) {
        PayrollHistorySnapshot history = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        String regularizationPeriod = normalizeYearMonth(payrollRepository.findRegularizationYearMonth(history.organizationCode(), history.personCode()));
        RegularizationSalaryPosition resolved = resolveRegularizationSalaryPosition(
                history.organizationCode(),
                history.personCode(),
                regularizationPeriod,
                history.positionCode(),
                history.calculationYear() + history.calculationMonth());
        EducationPromotionSource education = payrollRepository
                .findLatestEducationForPromotion(history.organizationCode(), history.personCode(), history.calculationYear() + history.calculationMonth())
                .orElse(null);
        EducationRegularizationStandard standard = resolved.standard();
        boolean eligible = history.positionCode() != null && history.positionCode().contains("F")
                && education != null && standard != null;
        boolean institutionRegularization = resolved.institutionRegularization();
        String regularPositionCode = eligible ? resolved.salaryPositionCode() : null;
        String regularPositionName = eligible ? resolved.salaryPositionName() : null;
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
                regularPositionName,
                regularLevel,
                regularStep,
                currentSalary,
                regularPositionSalary,
                regularBaseSalary,
                totalRegularSalary,
                totalRegularSalary - nullToZero(currentSalary),
                eligible,
                regularizationNote(history, education, standard, institutionRegularization, resolved.salaryPositionFromStandard()));
    }

    private Optional<PositionChangeCandidate> findRegularizationAppointedPosition(
            String organizationCode,
            String personCode,
            String regularizationPeriod) {
        String period = normalizeYearMonth(regularizationPeriod);
        if (period.isBlank()) {
            return Optional.empty();
        }
        Optional<PositionChangeCandidate> exact = payrollRepository.findPositionAtPeriod(organizationCode, personCode, period);
        if (exact.isPresent()) {
            return exact;
        }
        return payrollRepository.findPositionAtOrBefore(organizationCode, personCode, period);
    }

    private boolean isInternSalaryPosition(String positionCode) {
        return positionCode != null && positionCode.contains("F");
    }

    private boolean isRegularizationAppointedPosition(PositionChangeCandidate appointed) {
        return appointed != null
                && appointed.positionCode() != null
                && !appointed.positionCode().isBlank()
                && !isInternSalaryPosition(appointed.positionCode());
    }

    private boolean hasUsablePreReformPosition(List<WageReformPosition> reformPositions) {
        return reformPositions != null
                && !reformPositions.isEmpty()
                && hasUsablePreReformPositionCode(reformPositions.getFirst().positionCode());
    }

    private boolean hasUsablePreReformPosition(Optional<PositionChangeCandidate> preReformPosition) {
        return preReformPosition
                .map(PositionChangeCandidate::positionCode)
                .filter(this::hasUsablePreReformPositionCode)
                .isPresent();
    }

    private boolean hasUsablePreReformPositionCode(String positionCode) {
        return positionCode != null && !positionCode.isBlank() && !isInternSalaryPosition(positionCode);
    }

    private Optional<PositionChangeCandidate> resolvePreReformPositionCandidate(
            List<WageReformPosition> reformPositions,
            Optional<PositionChangeCandidate> latestPositionBefore) {
        if (hasUsablePreReformPosition(latestPositionBefore)) {
            return latestPositionBefore;
        }
        if (hasUsablePreReformPosition(reformPositions)) {
            WageReformPosition current = reformPositions.getFirst();
            return Optional.of(new PositionChangeCandidate(
                    current.positionCode(), current.positionName(), current.startYearMonth()));
        }
        return latestPositionBefore;
    }

    private WageReformPosition currentReformPositionForProjection(
            List<WageReformPosition> reformPositions,
            PositionChangeCandidate latestPositionBefore) {
        return reformPositions.stream()
                .filter(position -> latestPositionBefore.positionCode().equals(position.positionCode()))
                .findFirst()
                .orElseGet(() -> new WageReformPosition(
                        latestPositionBefore.positionCode(),
                        latestPositionBefore.positionName(),
                        latestPositionBefore.startYearMonth(),
                        0));
    }

    private boolean uses01PrefixRegularizationLookupPosition(
            PayrollHistorySnapshot latest,
            Optional<PositionChangeCandidate> preReformPosition) {
        if (isRegularizationLookupBy01Prefix(positionPrefix(latest.positionCode()))) {
            return true;
        }
        return preReformPosition
                .map(candidate -> isRegularizationLookupBy01Prefix(positionPrefix(candidate.positionCode())))
                .orElse(false);
    }

    private boolean isRegularizationLookupBy01Prefix(String prefix) {
        return prefix != null && REGULARIZATION_LOOKUP_BY_01_PREFIXES.contains(prefix);
    }

    private RegularizationSalaryPosition resolveRegularizationSalaryPosition(
            String organizationCode,
            String personCode,
            String regularizationPeriod,
            String fallbackPositionCode,
            String educationLookupPeriod) {
        return resolveRegularizationSalaryPosition(
                organizationCode,
                personCode,
                regularizationPeriod,
                fallbackPositionCode,
                educationLookupPeriod,
                false);
    }

    private RegularizationSalaryPosition resolveRegularizationSalaryPosition(
            String organizationCode,
            String personCode,
            String regularizationPeriod,
            String fallbackPositionCode,
            String educationLookupPeriod,
            boolean forceAdministrativeRegularizationLookup) {
        PositionChangeCandidate appointed = findRegularizationAppointedPosition(organizationCode, personCode, regularizationPeriod)
                .orElse(null);
        EducationPromotionSource education = findEducationForRegularization(
                organizationCode,
                personCode,
                regularizationPeriod,
                educationLookupPeriod);
        String standardLookupCode;
        if (isRegularizationAppointedPosition(appointed) && isInstitutionPosition(appointed.positionCode())) {
            standardLookupCode = appointed.positionCode();
        } else if (forceAdministrativeRegularizationLookup && !isRegularizationAppointedPosition(appointed)) {
            standardLookupCode = ADMINISTRATIVE_REGULARIZATION_LOOKUP_POSITION;
        } else {
            standardLookupCode = fallbackPositionCode;
        }
        EducationRegularizationStandard standard = findEducationRegularizationStandard(standardLookupCode, education);
        if (isRegularizationAppointedPosition(appointed) && isInstitutionPosition(appointed.positionCode())) {
            return new RegularizationSalaryPosition(
                    standard,
                    appointed,
                    appointed.positionCode(),
                    appointed.positionName(),
                    true,
                    false);
        }
        if (standard == null) {
            return new RegularizationSalaryPosition(null, appointed, null, null, false, false);
        }
        boolean salaryPositionFromStandard = !isRegularizationAppointedPosition(appointed);
        String salaryPositionCode = salaryPositionFromStandard
                ? normalizeEducationPromotionPositionCode(standard.positionCode())
                : appointed.positionCode();
        String salaryPositionName = salaryPositionFromStandard ? standard.positionName() : appointed.positionName();
        return new RegularizationSalaryPosition(
                standard,
                appointed,
                salaryPositionCode,
                salaryPositionName,
                false,
                salaryPositionFromStandard);
    }

    private EducationPromotionSource findEducationForRegularization(
            String organizationCode,
            String personCode,
            String regularizationPeriod,
            String educationLookupPeriod) {
        LinkedHashSet<String> periods = new LinkedHashSet<>();
        String normalizedRegularization = normalizeYearMonth(regularizationPeriod);
        String normalizedLookup = normalizeYearMonth(educationLookupPeriod);
        if (!normalizedRegularization.isBlank()) {
            periods.add(normalizedRegularization);
        }
        if (!normalizedLookup.isBlank()) {
            periods.add(normalizedLookup);
        }
        periods.add("200607");
        for (String period : periods) {
            Optional<EducationPromotionSource> education = payrollRepository.findLatestEducationForPromotion(
                    organizationCode, personCode, period);
            if (education.isPresent()) {
                return education.get();
            }
        }
        return payrollRepository.findPersonnelEducationCode(organizationCode, personCode)
                .filter(code -> !code.isBlank())
                .map(code -> new EducationPromotionSource(code, null, null))
                .orElse(null);
    }

    private EducationRegularizationStandard findEducationRegularizationStandard(
            String fallbackPositionCode,
            EducationPromotionSource education) {
        if (education == null || education.educationCode() == null || education.educationCode().isBlank()) {
            return null;
        }
        LinkedHashSet<String> lookupCodes = new LinkedHashSet<>();
        if (fallbackPositionCode != null && !fallbackPositionCode.isBlank()) {
            if (ADMINISTRATIVE_REGULARIZATION_LOOKUP_POSITION.equals(fallbackPositionCode)) {
                lookupCodes.add(ADMINISTRATIVE_REGULARIZATION_LOOKUP_POSITION);
            } else {
                lookupCodes.add(fallbackPositionCode);
                lookupCodes.add(administrativeEquivalentPosition(fallbackPositionCode));
                lookupCodes.add(normalizeEducationStandardPositionCodeForLookup(fallbackPositionCode));
            }
        }
        for (String lookupCode : lookupCodes) {
            Optional<EducationRegularizationStandard> standard = payrollRepository.findEducationRegularizationStandard(
                    lookupCode, education.educationCode());
            if (standard.isPresent()) {
                return standard.get();
            }
        }
        return null;
    }

    private String normalizeEducationStandardPositionCodeForLookup(String positionCode) {
        if (positionCode == null || positionCode.length() < 2) {
            return positionCode;
        }
        String prefix = positionCode.substring(0, 2);
        if ("01".equals(prefix) || "02".equals(prefix)) {
            return positionCode;
        }
        if (Set.of("21", "22", "23", "24", "25", "26", "27", "28").contains(prefix)) {
            return "01" + positionCode.substring(2);
        }
        return "01" + positionCode.substring(2);
    }

    private void addWageReformPositionCandidate(Set<String> positionCodes, String positionCode) {
        if (positionCode == null || positionCode.isBlank() || isInternSalaryPosition(positionCode)) {
            return;
        }
        positionCodes.add(positionCode.trim());
        positionCodes.add(administrativeEquivalentPosition(positionCode));
    }

    private Optional<WageReformStandard> lookupWageReformStandard(String positionCode, int appointmentYears, int reformYears) {
        Optional<WageReformStandard> standard = payrollRepository.findWageReformStandard(
                positionCode, appointmentYears, reformYears);
        if (standard.isPresent()) {
            return standard;
        }
        standard = payrollRepository.findNearestWageReformStandard(positionCode, appointmentYears, reformYears);
        if (standard.isPresent()) {
            return standard;
        }
        return payrollRepository.findFirstWageReformStandardForPosition(positionCode);
    }

    private Optional<WageReformStandard> lookupCurrentPositionWageReformStandard(
            WageReformPosition currentPosition,
            int reformYears) {
        LinkedHashSet<Integer> appointmentYearCandidates = new LinkedHashSet<>();
        appointmentYearCandidates.add(Math.max(1, wageReformAppointmentYears(currentPosition)));
        appointmentYearCandidates.add(1);
        for (int appointmentYears : appointmentYearCandidates) {
            Optional<WageReformStandard> standard = lookupWageReformStandard(
                    currentPosition.positionCode(), appointmentYears, reformYears);
            if (standard.isPresent()) {
                return standard;
            }
        }
        return Optional.empty();
    }

    private Integer regularizedBaseSalary(String positionCode, String levelOrSalaryLevel, String step, String standardYearMonth) {
        return switch (baseSalarySource(positionCode, levelOrSalaryLevel)) {
            case "GRADE" -> payrollRepository.civilServantGradeSalary(levelOrSalaryLevel, step, "0", standardYearMonth);
            case "POLICE_GRADE" -> payrollRepository.policeOfficerGradeSalary(levelOrSalaryLevel, step, standardYearMonth);
            default -> payrollRepository.salaryLevelSalary(step, "0", standardYearMonth, positionCode);
        };
    }

    private WageReformSelection wageReformSelection(
            PayrollHistorySnapshot latest,
            WageReformStandard currentStandard,
            int reformYears) {
        return wageReformSelection(latest, currentStandard, reformYears, null);
    }

    private Optional<WageReformPosition> findPriorLowerReformPosition(
            List<WageReformPosition> positions,
            String currentPositionCode) {
        if (positions == null || positions.size() < 2 || currentPositionCode == null || currentPositionCode.isBlank()) {
            return Optional.empty();
        }
        int currentLayer = positionLayer(currentPositionCode);
        if (currentLayer <= 0) {
            return Optional.empty();
        }
        int immediateLowerLayer = currentLayer + 1;
        Optional<WageReformPosition> immediateLower = positions.stream()
                .skip(1)
                .filter(position -> positionLayer(position.positionCode()) == immediateLowerLayer)
                .findFirst();
        if (immediateLower.isPresent()) {
            return immediateLower;
        }
        return positions.stream()
                .skip(1)
                .filter(position -> isLowerPositionLayer(currentPositionCode, position.positionCode()))
                .findFirst();
    }

    private WageReformSelection wageReformSelection(
            PayrollHistorySnapshot latest,
            WageReformStandard currentStandard,
            int reformYears,
            RegularizationSalaryPosition regularizationPositionForFloor) {
        List<WageReformPosition> positions = payrollRepository.findWageReformPositionsBefore(
                latest.organizationCode(),
                latest.personCode(),
                "200607",
                WAGE_REFORM_POSITION_PREFIXES);
        WageReformPosition currentPosition = positions.isEmpty()
                ? new WageReformPosition(currentStandard.positionCode(), "", "", 0)
                : positions.getFirst();
        Optional<WageReformPosition> lowerPositionOptional = findPriorLowerReformPosition(positions, currentPosition.positionCode());
        WageReformSelection selection;
        if (lowerPositionOptional.isEmpty() || !"GRADE".equals(baseSalarySource(currentStandard.positionCode()))) {
            selection = WageReformSelection.fromCurrent(
                    currentStandard,
                    positionNameForProjectionStart(positions.stream()
                            .findFirst()
                            .map(position -> new PositionChangeCandidate(position.positionCode(), position.positionName(), position.startYearMonth())),
                            currentStandard.positionCode()),
                    "");
            return applyEducationRegularizationFloor(latest, selection, regularizationPositionForFloor);
        }
        WageReformPosition lowerPosition = lowerPositionOptional.get();
        int lowerAppointmentYears = wageReformAppointmentYears(lowerPosition);
        Optional<WageReformStandard> lowerStandard = payrollRepository.findWageReformStandard(
                lowerPosition.positionCode(),
                lowerAppointmentYears,
                reformYears);
        if (lowerStandard.isEmpty()) {
            return applyEducationRegularizationFloor(
                    latest,
                    WageReformSelection.fromCurrent(currentStandard, currentPosition.positionName(), ""),
                    regularizationPositionForFloor);
        }
        int currentLevel = payrollRepository.intValue(currentStandard.convertedLevel());
        int lowerLevel = payrollRepository.intValue(lowerStandard.get().convertedLevel());
        int currentSalary = payrollRepository.gradeSalary(currentStandard.convertedLevel(), currentStandard.convertedStep(), "200607");
        int lowerSalary = payrollRepository.gradeSalary(lowerStandard.get().convertedLevel(), lowerStandard.get().convertedStep(), "200607");
        if (currentLevel >= lowerLevel && lowerLevel > 1) {
            String promotedLevel = String.valueOf(lowerLevel - 1);
            String promotedStep = firstHigherGradeStep(promotedLevel, lowerSalary, "200607");
            selection = new WageReformSelection(
                    currentStandard.positionCode(),
                    currentPosition.positionName(),
                    promotedLevel,
                    promotedStep,
                    "；现任职务套改级别低于或等于原任低一职务，按原任低一职务合并任职年限套改后高套一级");
            return applyEducationRegularizationFloor(latest, selection, regularizationPositionForFloor);
        }
        if (currentLevel < lowerLevel
                && payrollRepository.intValue(currentStandard.convertedStep()) < payrollRepository.intValue(lowerStandard.get().convertedStep())
                && currentSalary < lowerSalary) {
            String promotedStep = firstHigherGradeStep(currentStandard.convertedLevel(), lowerSalary, "200607");
            selection = new WageReformSelection(
                    currentStandard.positionCode(),
                    currentPosition.positionName(),
                    currentStandard.convertedLevel(),
                    promotedStep,
                    "；现任职务级别较高但工资额低于原任低一职务，按原任低一职务工资额就近就高套入现任职务级别");
            return applyEducationRegularizationFloor(latest, selection, regularizationPositionForFloor);
        }
        return applyEducationRegularizationFloor(
                latest,
                WageReformSelection.fromCurrent(currentStandard, currentPosition.positionName(), ""),
                regularizationPositionForFloor);
    }

    private WageReformSelection applyEducationRegularizationFloor(PayrollHistorySnapshot latest, WageReformSelection selection) {
        return applyEducationRegularizationFloor(latest, selection, null);
    }

    private WageReformSelection applyEducationRegularizationFloor(
            PayrollHistorySnapshot latest,
            WageReformSelection selection,
            RegularizationSalaryPosition regularizationPositionForFloor) {
        if (!"GRADE".equals(baseSalarySource(selection.positionCode()))) {
            return selection;
        }
        EducationRegularizationStandard standard = null;
        if (regularizationPositionForFloor != null
                && regularizationPositionForFloor.standard() != null
                && !regularizationPositionForFloor.institutionRegularization()) {
            standard = regularizationPositionForFloor.standard();
        }
        if (standard == null) {
            String regularization = normalizeYearMonth(payrollRepository.findRegularizationYearMonth(
                    latest.organizationCode(), latest.personCode()));
            EducationPromotionSource education = findEducationForRegularization(
                    latest.organizationCode(),
                    latest.personCode(),
                    regularization,
                    "200607");
            standard = findEducationRegularizationStandard(selection.positionCode(), education);
        }
        if (standard == null) {
            return selection;
        }
        int reformLevel = payrollRepository.intValue(selection.level());
        int regularizationLevel = payrollRepository.intValue(standard.gradeLevel());
        int reformSalary = payrollRepository.civilServantGradeSalary(selection.level(), selection.step(), "0", "200607");
        int regularizationSalary = payrollRepository.civilServantGradeSalary(
                standard.gradeLevel(), standard.gradeStep(), "0", "200607");
        if (reformLevel <= 0 || regularizationLevel <= 0 || reformSalary <= 0 || regularizationSalary <= 0 || reformSalary >= regularizationSalary) {
            return selection;
        }
        if (reformLevel > regularizationLevel) {
            return selection.withLevelStep(
                    standard.gradeLevel(),
                    standard.gradeStep(),
                    "；套改级别和级别工资额低于相同学历新参加工作人员转正定级标准，执行定级级别/档次 "
                            + levelStepDisplay("GRADE", standard.gradeLevel(), standard.gradeStep()));
        }
        if (reformLevel < regularizationLevel) {
            String adjustedStep = firstHigherGradeStep(selection.level(), regularizationSalary, "200607");
            return selection.withLevelStep(
                    selection.level(),
                    adjustedStep,
                    "；套改级别高于相同学历新参加工作人员定级级别但级别工资额较低，按定级工资额就近就高套入套改级别");
        }
        return selection;
    }

    private int wageReformAppointmentYears(WageReformPosition position) {
        return Math.max(0, 2006 - yearOf(position.startYearMonth()) + 1 - nullToZero(position.interruptedYears()));
    }

    private int wageReformAppointmentYearsForProjection(
            Optional<PositionChangeCandidate> positionBeforeReform,
            String regularization,
            PayrollHistorySnapshot latest) {
        if (positionBeforeReform.isPresent()) {
            return Math.max(1, 2006 - yearOf(positionBeforeReform.get().startYearMonth()) + 1);
        }
        if (!regularization.isBlank()) {
            return Math.max(1, 2006 - yearOf(regularization) + 1);
        }
        int positionStartYear = yearOf(latest.positionStartYearMonth());
        if (positionStartYear > 0 && positionStartYear <= 2006) {
            return Math.max(1, 2006 - positionStartYear + 1);
        }
        return Math.max(1, 2006 - yearOf(latest.workStartYearMonth()) + 1 - nullToZero(latest.interruptedSalaryYears()));
    }

    private Optional<WageReformStandard> resolveWageReformStandardForProjection(
            String reformPositionCode,
            int appointmentYears,
            int reformYears,
            boolean allowRegularizationPositionFallback,
            RegularizationSalaryPosition regularizationPosition) {
        LinkedHashSet<String> positionCodes = new LinkedHashSet<>();
        addWageReformPositionCandidate(positionCodes, reformPositionCode);
        if (allowRegularizationPositionFallback
                && (reformPositionCode == null || isInternSalaryPosition(reformPositionCode))) {
            if (regularizationPosition != null && regularizationPosition.standard() != null) {
                addWageReformPositionCandidate(positionCodes, regularizationPosition.salaryPositionCode());
                addWageReformPositionCandidate(positionCodes, regularizationPosition.standard().positionCode());
                addWageReformPositionCandidate(
                        positionCodes,
                        normalizeEducationPromotionPositionCode(regularizationPosition.standard().positionCode()));
            }
            addWageReformPositionCandidate(positionCodes, ADMINISTRATIVE_REGULARIZATION_LOOKUP_POSITION);
        }
        LinkedHashSet<Integer> appointmentYearCandidates = new LinkedHashSet<>();
        appointmentYearCandidates.add(Math.max(1, appointmentYears));
        appointmentYearCandidates.add(1);
        for (String positionCode : positionCodes) {
            for (int years : appointmentYearCandidates) {
                Optional<WageReformStandard> standard = lookupWageReformStandard(positionCode, years, reformYears);
                if (standard.isPresent()) {
                    return standard;
                }
            }
        }
        return Optional.empty();
    }

    private WageProjectionStart wageProjectionStart(PayrollHistorySnapshot latest) {
        String regularization = normalizeYearMonth(payrollRepository.findRegularizationYearMonth(latest.organizationCode(), latest.personCode()));
        if (!regularization.isBlank() && regularization.compareTo("200607") < 0) {
            List<WageReformPosition> reformPositions = payrollRepository.findWageReformPositionsBefore(
                    latest.organizationCode(),
                    latest.personCode(),
                    "200607",
                    WAGE_REFORM_POSITION_PREFIXES);
            Optional<PositionChangeCandidate> latestPositionBefore = payrollRepository.findLatestPositionBefore(
                    latest.organizationCode(),
                    latest.personCode(),
                    "200607",
                    WAGE_REFORM_POSITION_PREFIXES);
            Optional<PositionChangeCandidate> position = resolvePreReformPositionCandidate(reformPositions, latestPositionBefore);
            Optional<PositionChangeCandidate> regularizationAppointed = findRegularizationAppointedPosition(
                    latest.organizationCode(), latest.personCode(), regularization);
            boolean missingRegularizationAppointment = !isRegularizationAppointedPosition(regularizationAppointed.orElse(null));
            boolean hasPreReformPosition = hasUsablePreReformPosition(latestPositionBefore);
            boolean needRegularizationSalaryPosition = !hasPreReformPosition
                    && missingRegularizationAppointment
                    && uses01PrefixRegularizationLookupPosition(latest, position);
            RegularizationSalaryPosition regularizationPosition = needRegularizationSalaryPosition
                    ? resolveRegularizationSalaryPosition(
                            latest.organizationCode(),
                            latest.personCode(),
                            regularization,
                            latest.positionCode(),
                            regularization,
                            true)
                    : RegularizationSalaryPosition.empty();
            String reformPositionCode = position.map(PositionChangeCandidate::positionCode).orElse(null);
            boolean reformUsedRegularizationStandard = false;
            if (reformPositionCode == null || isInternSalaryPosition(reformPositionCode)) {
                if (needRegularizationSalaryPosition
                        && regularizationPosition.standard() != null
                        && !regularizationPosition.institutionRegularization()) {
                    reformPositionCode = regularizationPosition.salaryPositionCode();
                    reformUsedRegularizationStandard = true;
                } else if (!isInternSalaryPosition(latest.positionCode())) {
                    reformPositionCode = latest.positionCode();
                }
            }
            int appointmentYears = reformUsedRegularizationStandard && !regularization.isBlank()
                    ? Math.max(1, 2006 - yearOf(regularization) + 1)
                    : wageReformAppointmentYearsForProjection(position, regularization, latest);
            int reformYears = payrollRepository.calculatedWageReformYears(latest.organizationCode(), latest.personCode());
            if (reformYears <= 0) {
                reformYears = Math.max(1, 2006 - yearOf(latest.workStartYearMonth()) + 1 - nullToZero(latest.interruptedSalaryYears()));
            }
            Optional<WageReformStandard> standard = hasPreReformPosition
                    ? lookupCurrentPositionWageReformStandard(
                            currentReformPositionForProjection(reformPositions, latestPositionBefore.get()),
                            reformYears)
                    : resolveWageReformStandardForProjection(
                            reformPositionCode,
                            appointmentYears,
                            reformYears,
                            needRegularizationSalaryPosition,
                            regularizationPosition);
            if (standard.isEmpty()) {
                return WageProjectionStart.ineligible("2006.07 前已转正，但未能按基本信息匹配 2006 套改标准。");
            }
            RegularizationSalaryPosition regularizationPositionForFloor = needRegularizationSalaryPosition
                    && regularizationPosition.standard() != null
                    ? regularizationPosition
                    : null;
            WageReformSelection selection = wageReformSelection(
                    latest, standard.get(), reformYears, regularizationPositionForFloor);
            String reformNote = "2006.07 前已转正，按基础信息折算套改年限 " + reformYears + " 年，并按 2006 套改标准确定起点：职务 "
                    + positionDisplay(selection.positionCode(), selection.positionName())
                    + "，级别/档次 " + levelStepDisplay("GRADE", selection.level(), selection.step()) + selection.note();
            if (needRegularizationSalaryPosition && regularizationPosition.standard() != null) {
                reformNote = "2006.07 前已转正，未找到转正定级任职记录，按转正定级标准确认执行工资职务 "
                        + positionDisplay(
                                regularizationPosition.salaryPositionCode(),
                                regularizationPosition.salaryPositionName())
                        + "；按基础信息折算套改年限 " + reformYears + " 年，并按 2006 套改标准确定起点：级别/档次 "
                        + levelStepDisplay("GRADE", selection.level(), selection.step()) + selection.note() + "。";
            } else {
                reformNote += "。";
            }
            String positionStartYearMonth = position.map(PositionChangeCandidate::startYearMonth).orElse("2006.07");
            if (reformUsedRegularizationStandard && !regularization.isBlank()) {
                positionStartYearMonth = regularization;
            }
            return new WageProjectionStart(
                    true,
                    "200607",
                    selection.positionCode(),
                    selection.positionName(),
                    selection.level(),
                    selection.step(),
                    "2006",
                    "2006",
                    positionStartYearMonth,
                    reformYears,
                    reformNote);
        }
        if (regularization.isBlank()) {
            return WageProjectionStart.ineligible("未找到转正年月，无法从转正定级规则确定起点。");
        }
        RegularizationSalaryPosition resolved = resolveRegularizationSalaryPosition(
                latest.organizationCode(),
                latest.personCode(),
                regularization,
                latest.positionCode(),
                regularization);
        EducationRegularizationStandard standard = resolved.standard();
        if (standard == null) {
            return WageProjectionStart.ineligible("2006.07 及以后转正，但未能按学历转正定级标准确定起点。");
        }
        String positionCode = resolved.salaryPositionCode();
        String positionName = resolved.salaryPositionName();
        PositionChangeCandidate appointed = resolved.appointedPosition();
        boolean institution = resolved.institutionRegularization();
        String positionStartYearMonth = appointed == null || !isRegularizationAppointedPosition(appointed)
                ? regularization
                : appointed.startYearMonth();
        String civilServantNote = resolved.salaryPositionFromStandard()
                ? "2006.07 及以后转正，转正时间 " + formatYearMonth(regularization)
                + "，未找到转正定级任职记录，按转正定级标准确认执行工资职务 "
                + positionDisplay(positionCode, positionName)
                + "，级别/档次 " + standard.gradeLevel() + "/" + standard.gradeStep() + "。"
                : "2006.07 及以后转正，转正时间 " + formatYearMonth(regularization)
                + "，按学历转正定级标准确定起点：岗位 " + positionDisplay(positionCode, positionName)
                + "，级别/档次 " + standard.gradeLevel() + "/" + standard.gradeStep() + "。";
        return new WageProjectionStart(
                true,
                regularization,
                positionCode,
                positionName,
                institution ? "" : standard.gradeLevel(),
                standard.gradeStep(),
                String.valueOf(yearOf(regularization)),
                String.valueOf(yearOf(regularization)),
                positionStartYearMonth,
                0,
                institution
                        ? "2006.07 及以后转正，转正时间 " + formatYearMonth(regularization)
                        + "，转正定级，" + positionDisplayWithoutCode(positionName, positionCode)
                        + "，薪级" + standard.gradeStep() + "级。"
                        : civilServantNote);
    }

    private String regularizationNote(
            PayrollHistorySnapshot history,
            EducationPromotionSource education,
            EducationRegularizationStandard standard,
            boolean institutionRegularization,
            boolean salaryPositionFromStandard) {
        if (history.positionCode() == null || !history.positionCode().contains("F")) {
            return "当前执行工资不是见习岗位，暂不参与转正定级试算。";
        }
        if (education == null) {
            return "未找到可用于转正定级的学历记录。";
        }
        if (standard == null) {
            return "未找到当前见习岗位前缀和学历编码对应的转正定级标准。";
        }
        if (institutionRegularization) {
            return "事业人员转正按学历转正定级标准确定薪级，职务岗位取转正时聘任岗位记录。";
        }
        if (salaryPositionFromStandard) {
            return "未找到转正定级任职记录，按转正定级标准确认执行工资职务，暂不写入数据库。";
        }
        return "按学历和 bz06_zzdz 转正定级标准试算，暂不写入数据库。";
    }

    private EducationPromotionResolution resolveEducationPromotion(
            String positionCode,
            String positionName,
            String currentLevel,
            String currentGradeStep,
            String currentGradeStepDifference,
            String currentLevelAssessmentStartYear,
            String currentStepAssessmentStartYear,
            String salaryStandardYearMonth,
            EducationPromotionSource education) {
        if (education == null) {
            return EducationPromotionResolution.ineligible("未找到可用于学历晋升的学历记录。");
        }
        EducationRegularizationStandard standard = payrollRepository
                .findEducationRegularizationStandard(positionCode, education.educationCode())
                .orElse(null);
        if (standard == null) {
            return EducationPromotionResolution.ineligible("未找到当前岗位前缀和学历编码对应的转正定级标准。");
        }
        if (isInstitutionPosition(positionCode)) {
            int currentSalaryLevel = payrollRepository.intValue(currentGradeStep);
            int standardSalaryLevel = payrollRepository.intValue(standard.gradeStep());
            String promotedStep = currentSalaryLevel >= standardSalaryLevel
                    ? currentGradeStep
                    : standard.gradeStep();
            String changeYear = yearOf(education.graduationDate()) > 0
                    ? String.valueOf(yearOf(education.graduationDate()))
                    : currentStepAssessmentStartYear;
            return new EducationPromotionResolution(
                    standardSalaryLevel > currentSalaryLevel,
                    positionCode,
                    currentLevel,
                    promotedStep,
                    "0",
                    0,
                    0,
                    currentLevelAssessmentStartYear,
                    standardSalaryLevel > currentSalaryLevel
                            ? nextAssessmentYear(education.graduationDate(), currentStepAssessmentStartYear)
                            : currentStepAssessmentStartYear,
                    standardSalaryLevel > currentSalaryLevel
                            ? "事业人员取得国家承认的较高学历后，现薪级低于相同学历转正定级标准，执行新学历转正定级薪级；次年不再正常晋升薪级。"
                            : "事业人员取得国家承认的较高学历后，现薪级不低于相同学历转正定级标准，薪级不变。");
        }
        if (!"GRADE".equals(baseSalarySource(positionCode, currentLevel))) {
            return EducationPromotionResolution.ineligible("当前岗位不属于公务员级别工资序列，不适用在职学历晋升规则。");
        }
        String promotedPositionCode = positionCode;
        if (standard.positionCode().compareTo(positionCode) <= 0) {
            promotedPositionCode = normalizeEducationPromotionPositionCode(standard.positionCode());
        }
        String promotedLevel = educationPromotionLevel(currentLevel, standard);
        String promotedGradeStep = educationPromotionGradeStep(
                positionCode,
                currentLevel,
                currentGradeStep,
                currentGradeStepDifference,
                standard,
                promotedLevel,
                salaryStandardYearMonth);
        int currentPositionSalary = payrollRepository.positionSalary(positionCode, salaryStandardYearMonth);
        int promotedPositionSalary = payrollRepository.positionSalary(promotedPositionCode, salaryStandardYearMonth);
        int currentGradeSalary = civilServantGradeSalaryAmount(
                positionCode, currentLevel, currentGradeStep, currentGradeStepDifference, salaryStandardYearMonth);
        int promotedGradeSalary = civilServantGradeSalaryAmount(
                promotedPositionCode, promotedLevel, promotedGradeStep, "0", salaryStandardYearMonth);
        int currentBasicSalary = nullToZero(currentPositionSalary) + nullToZero(currentGradeSalary);
        int promotedBasicSalary = nullToZero(promotedPositionSalary) + nullToZero(promotedGradeSalary);
        if (promotedBasicSalary <= currentBasicSalary) {
            return EducationPromotionResolution.ineligible(
                    "当前基本工资（职务工资 " + currentPositionSalary + " + 级别工资 " + currentGradeSalary
                            + "）不低于相同学历新录用公务员转正定级工资待遇（"
                            + promotedPositionSalary + " + " + promotedGradeSalary + "），不执行学历晋升。");
        }
        String changeYear = yearOf(education.graduationDate()) > 0
                ? String.valueOf(yearOf(education.graduationDate()))
                : currentStepAssessmentStartYear;
        boolean levelChanged = !promotedLevel.equals(currentLevel);
        int promotedLevels = Math.max(0, payrollRepository.intValue(currentLevel) - payrollRepository.intValue(promotedLevel));
        boolean positionHierarchyChanged = positionLayer(promotedPositionCode) != positionLayer(positionCode);
        boolean resetLevelAssessmentYear = levelChanged
                && (!positionHierarchyChanged || promotedLevels >= 2);
        boolean gradeIncreaseExceedsStepDifference = levelChanged
                && gradeIncreaseExceedsStepDifference(
                        currentLevel,
                        currentGradeStep,
                        currentGradeStepDifference,
                        promotedLevel,
                        promotedGradeStep,
                        "0",
                        salaryStandardYearMonth);
        String nextLevelAssessmentStartYear = resetLevelAssessmentYear
                ? changeYear
                : currentLevelAssessmentStartYear;
        String nextStepAssessmentStartYear = levelChanged
                ? (gradeIncreaseExceedsStepDifference ? changeYear : currentStepAssessmentStartYear)
                : currentStepAssessmentStartYear;
        String levelAssessmentNote = !levelChanged
                ? positionHierarchyChanged
                        ? "级别未变，仅执行工资职务层次变化，级别考核年限沿用原起算年"
                        : "级别未变，级别考核年限沿用原起算年"
                : resetLevelAssessmentYear
                        ? positionHierarchyChanged
                                ? "按第二条第二款，执行工资职务层次变动且晋升级别达到两级及以上，级别考核年限从变动当年起重新计算"
                                : "按第二条第二款，未引起执行工资职务层次变化但引起级别变化，级别考核年限从变动当年起重新计算"
                        : "按第二条第二款，执行工资职务层次变动且仅晋升一个级别，级别考核年限沿用原起算年";
        String stepAssessmentNote = !levelChanged
                ? positionHierarchyChanged
                        ? "级别和档次均未变，仅执行工资职务层次变化，档次考核年限沿用原起算年"
                        : "级别和档次均未变，档次考核年限沿用原起算年"
                : gradeIncreaseExceedsStepDifference
                        ? "按第二条第二款第四项，晋升级别相应增加级别工资的增资额超过下一级别一个工资档差，档次考核年限从变动当年起重新计算"
                        : "按第二条第二款第四项，晋升级别相应增加级别工资的增资额未超过下一级别一个工资档差，档次考核年限沿用原晋档起算年";
        return new EducationPromotionResolution(
                true,
                promotedPositionCode,
                promotedLevel,
                promotedGradeStep,
                "0",
                currentBasicSalary,
                promotedBasicSalary,
                nextLevelAssessmentStartYear,
                nextStepAssessmentStartYear,
                "按国人部发〔2006〕58号第二条第一款，执行相同学历新录用公务员转正定级工资待遇 "
                        + levelStepDisplay("GRADE", standard.gradeLevel(), standard.gradeStep())
                        + "；" + levelAssessmentNote + "；" + stepAssessmentNote + "。");
    }

    private int civilServantBasicSalary(
            String positionCode,
            String level,
            String gradeStep,
            String gradeStepDifference,
            String salaryStandardYearMonth) {
        int positionSalary = payrollRepository.positionSalary(positionCode, salaryStandardYearMonth);
        int gradeSalary = civilServantGradeSalaryAmount(
                positionCode, level, gradeStep, gradeStepDifference, salaryStandardYearMonth);
        return nullToZero(positionSalary) + nullToZero(gradeSalary);
    }

    private int civilServantGradeSalaryAmount(
            String positionCode,
            String level,
            String gradeStep,
            String gradeStepDifference,
            String salaryStandardYearMonth) {
        if (!"GRADE".equals(baseSalarySource(positionCode, level))) {
            return 0;
        }
        return payrollRepository.civilServantGradeSalary(level, gradeStep, gradeStepDifference, salaryStandardYearMonth);
    }

    private String educationPromotionLevel(String currentLevel, EducationRegularizationStandard standard) {
        int standardLevel = payrollRepository.intValue(standard.gradeLevel());
        int level = payrollRepository.intValue(currentLevel);
        if (standardLevel > 0 && level > 0 && standardLevel < level) {
            return standard.gradeLevel();
        }
        return currentLevel;
    }

    private String educationPromotionGradeStep(
            String positionCode,
            String currentLevel,
            String currentGradeStep,
            String currentGradeStepDifference,
            EducationRegularizationStandard standard,
            String promotedLevel,
            String salaryStandardYearMonth) {
        int standardLevel = payrollRepository.intValue(standard.gradeLevel());
        int level = payrollRepository.intValue(currentLevel);
        int standardStep = payrollRepository.intValue(standard.gradeStep());
        int currentSalary = civilServantGradeSalaryAmount(
                positionCode, currentLevel, currentGradeStep, currentGradeStepDifference, salaryStandardYearMonth);
        int standardSalary = civilServantGradeSalaryAmount(
                positionCode, standard.gradeLevel(), standard.gradeStep(), "0", salaryStandardYearMonth);
        if (standardLevel > 0 && level > 0 && standardLevel < level) {
            if (standardSalary >= currentSalary) {
                return standard.gradeStep();
            }
            return firstHigherGradeStep(standard.gradeLevel(), currentSalary, salaryStandardYearMonth);
        }
        if (standardLevel == level) {
            if (standardSalary > currentSalary) {
                return firstHigherGradeStep(promotedLevel, standardSalary, salaryStandardYearMonth);
            }
            return String.valueOf(Math.max(standardStep, payrollRepository.intValue(currentGradeStep)));
        }
        if (standardSalary > currentSalary) {
            return firstHigherGradeStep(promotedLevel, standardSalary, salaryStandardYearMonth);
        }
        return currentGradeStep;
    }

    private String normalizeEducationPromotionPositionCode(String positionCode) {
        if (positionCode != null && positionCode.startsWith("07") && positionCode.length() >= 4 && "0".equals(positionCode.substring(3, 4))) {
            return "070" + positionCode.substring(2, 3);
        }
        return positionCode;
    }

    private boolean gradeIncreaseExceedsStepDifference(
            String currentLevel,
            String currentStep,
            String promotedLevel,
            String standardYearMonth) {
        return gradeIncreaseExceedsStepDifference(
                currentLevel, currentStep, "0", promotedLevel, null, "0", standardYearMonth);
    }

    private boolean gradeIncreaseExceedsStepDifference(
            String currentLevel,
            String currentStep,
            String currentGradeStepDifference,
            String promotedLevel,
            String promotedGradeStep,
            String promotedGradeStepDifference,
            String standardYearMonth) {
        int sourceLevel = payrollRepository.intValue(currentLevel);
        int targetLevel = payrollRepository.intValue(promotedLevel);
        if (sourceLevel <= 0 || targetLevel <= 0 || sourceLevel <= targetLevel) {
            return false;
        }
        String previousStep = currentStep;
        String previousStepDifference = blankToZero(currentGradeStepDifference);
        int previousSalary = payrollRepository.civilServantGradeSalary(
                String.valueOf(sourceLevel), previousStep, previousStepDifference, standardYearMonth);
        for (int level = sourceLevel - 1; level >= targetLevel; level--) {
            String nextLevel = String.valueOf(level);
            String nextStep = level == targetLevel && promotedGradeStep != null && !promotedGradeStep.isBlank()
                    ? promotedGradeStep
                    : firstHigherGradeStep(nextLevel, previousSalary, standardYearMonth);
            String nextStepDifference = level == targetLevel ? blankToZero(promotedGradeStepDifference) : "0";
            int nextSalary = payrollRepository.civilServantGradeSalary(
                    nextLevel, nextStep, nextStepDifference, standardYearMonth);
            int increase = nextSalary - previousSalary;
            int oneStepDifference = gradeStepDifference(nextLevel, nextStep, standardYearMonth);
            if (increase > oneStepDifference) {
                return true;
            }
            previousSalary = nextSalary;
            previousStep = nextStep;
            previousStepDifference = nextStepDifference;
        }
        return false;
    }

    private static String blankToZero(String value) {
        return value == null || value.isBlank() ? "0" : value.trim();
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

    private String lowerGradeStepByLevel(String currentLevel, String currentStep, String demotedLevel, String standardYearMonth) {
        return lowerGradeStepByLevel(
                payrollRepository.intValue(currentLevel),
                currentStep,
                payrollRepository.intValue(demotedLevel),
                standardYearMonth);
    }

    private String lowerGradeStepByLevel(int currentLevel, String currentStep, int demotedLevel, String standardYearMonth) {
        int previousSalary = payrollRepository.gradeSalary(String.valueOf(currentLevel), currentStep, standardYearMonth);
        for (int level = currentLevel + 1; level <= demotedLevel; level++) {
            String nextStep = nearestLowerGradeStep(String.valueOf(level), previousSalary, standardYearMonth);
            previousSalary = payrollRepository.gradeSalary(String.valueOf(level), nextStep, standardYearMonth);
        }
        return currentLevel < demotedLevel ? nearestLowerGradeStep(String.valueOf(demotedLevel), previousSalary, standardYearMonth) : currentStep;
    }

    private String nearestLowerGradeStep(String level, Integer currentGradeSalary, String standardYearMonth) {
        String selectedStep = "1";
        for (int step = 1; step <= 20; step++) {
            int amount = payrollRepository.gradeSalary(level, String.valueOf(step), standardYearMonth);
            if (amount <= nullToZero(currentGradeSalary)) {
                selectedStep = String.valueOf(step);
            } else {
                break;
            }
        }
        return selectedStep;
    }

    private boolean isCivilServantForPositionChange(String positionCode) {
        if (positionCode == null || positionCode.length() < 2) {
            return false;
        }
        return Set.of("01", "02", "04", "21", "22", "23", "24", "25", "26", "27", "28").contains(positionCode.substring(0, 2));
    }

    private String positionPrefix(String positionCode) {
        return positionCode == null || positionCode.length() < 2 ? "" : positionCode.substring(0, 2);
    }

    private boolean isSequenceConversion(String currentPositionPrefix, String newPositionPrefix) {
        if (POLICE_OFFICER_CONVERSION_TARGET_PREFIXES.contains(currentPositionPrefix)
                && POLICE_OFFICER_CONVERSION_TARGET_PREFIXES.contains(newPositionPrefix)) {
            return false;
        }
        if ("01".equals(currentPositionPrefix) && "02".equals(newPositionPrefix)) {
            return false;
        }
        return POSITION_SEQUENCE_PREFIXES.contains(currentPositionPrefix)
                && POSITION_SEQUENCE_PREFIXES.contains(newPositionPrefix)
                && !currentPositionPrefix.equals(newPositionPrefix);
    }

    private boolean isPoliceOfficerConversion(String currentPositionPrefix, String newPositionPrefix) {
        return POLICE_OFFICER_CONVERSION_SOURCE_PREFIXES.contains(currentPositionPrefix)
                && POLICE_OFFICER_CONVERSION_TARGET_PREFIXES.contains(newPositionPrefix);
    }

    private boolean isLegacyPolicePositionConversion(String currentPositionPrefix, String newPositionPrefix) {
        return "01".equals(currentPositionPrefix) && "02".equals(newPositionPrefix);
    }

    private boolean isJudicialConversion(String currentPositionPrefix, String newPositionPrefix) {
        return POLICE_OFFICER_CONVERSION_SOURCE_PREFIXES.contains(currentPositionPrefix)
                && JUDICIAL_CONVERSION_TARGET_PREFIXES.contains(newPositionPrefix);
    }

    private boolean isRankConversion(String currentPositionPrefix, String newPositionPrefix) {
        return "01".equals(currentPositionPrefix)
                && POSITION_SEQUENCE_PREFIXES.contains(newPositionPrefix)
                && !Set.of("01", "02", "03", "21", "22").contains(newPositionPrefix);
    }

    private boolean isPoliceToAdministrativeConversion(String currentPositionPrefix, String newPositionPrefix) {
        return POLICE_OFFICER_CONVERSION_TARGET_PREFIXES.contains(currentPositionPrefix)
                && POLICE_OFFICER_CONVERSION_SOURCE_PREFIXES.contains(newPositionPrefix);
    }

    private RankConversionResult rankConversionResult(
            PayrollHistorySnapshot history,
            PositionChangeCandidate candidate,
            PositionLevelRange targetLevelRange,
            int currentLevel,
            String currentStep,
            Integer currentGradeSalary,
            boolean highPositionPromotion) {
        if (targetLevelRange == null || currentLevel <= 0 || payrollRepository.intValue(currentStep) <= 0) {
            return RankConversionResult.ineligible();
        }
        String promotedLevel;
        if (currentLevel > targetLevelRange.minimumLevel()) {
            promotedLevel = String.valueOf(targetLevelRange.minimumLevel());
        } else if (highPositionPromotion) {
            promotedLevel = String.valueOf(Math.max(1, currentLevel - 1));
        } else {
            promotedLevel = String.valueOf(currentLevel);
        }
        String promotedStep = currentStep;
        if (!promotedLevel.equals(String.valueOf(currentLevel))) {
            promotedStep = firstHigherGradeStep(promotedLevel, currentGradeSalary, history.salaryStandardYearMonth());
        }
        return new RankConversionResult(
                true,
                promotedLevel,
                promotedStep,
                payrollRepository.gradeSalary(promotedLevel, promotedStep, history.salaryStandardYearMonth()));
    }

    private InstitutionPositionChangeResult institutionPositionChangeResult(
            PayrollHistorySnapshot history,
            PositionChangeCandidate candidate,
            String currentSalaryLevel) {
        int currentLevel = payrollRepository.intValue(currentSalaryLevel);
        if (currentLevel <= 0) {
            return InstitutionPositionChangeResult.ineligible("当前薪级不完整，无法试算事业岗位变动。");
        }
        String currentPrefix = positionPrefix(history.positionCode());
        String targetPrefix = positionPrefix(candidate.positionCode());
        String startLevel = String.valueOf(currentLevel);
        String promotedLevel = startLevel;
        String nextStepStartYear = history.stepAssessmentStartYear();
        String note;
        if (currentPrefix.equals(targetPrefix)) {
            PositionLevelRange range = payrollRepository.findPositionLevelRange(candidate.positionCode()).orElse(null);
            if (range != null && currentLevel < range.minimumLevel()) {
                promotedLevel = String.valueOf(range.minimumLevel());
                nextStepStartYear = nextAssessmentYear(candidate.startYearMonth(), history.calculationYear());
                note = "事业人员同序列岗位变动：未达新岗位最低薪级，进入最低薪级，第二年不再考核晋升薪级。";
            } else {
                note = "事业人员同序列岗位变动：已达新岗位最低薪级，薪级不变。";
            }
        } else if ("08".equals(currentPrefix) && Set.of("07", "10", "11").contains(targetPrefix)) {
            InstitutionStart start = institutionTransferStart(history, candidate);
            if (!start.eligible()) {
                return InstitutionPositionChangeResult.ineligible(start.note());
            }
            startLevel = start.startSalaryLevel();
            promotedLevel = replayInstitutionSalaryLevel(
                    history,
                    start.historyStartPeriod(),
                    startLevel,
                    targetPrefix);
            nextStepStartYear = start.stepStartYear();
            note = start.note() + "；回放套改/转正以来正常薪级晋升和学历变化后确定转岗薪级。";
        } else {
            PositionLevelRange range = payrollRepository.findPositionLevelRange(candidate.positionCode()).orElse(null);
            if (range != null && currentLevel < range.minimumLevel()) {
                promotedLevel = String.valueOf(range.minimumLevel());
                nextStepStartYear = nextAssessmentYear(candidate.startYearMonth(), history.calculationYear());
                note = "事业人员不同序列岗位变动：未达新岗位最低薪级，进入最低薪级。";
            } else {
                note = "事业人员不同序列岗位变动：已达新岗位最低薪级，薪级不变。";
            }
        }
        int promotedSalary = payrollRepository.salaryLevelSalary(promotedLevel, "0", history.salaryStandardYearMonth(), candidate.positionCode());
        return new InstitutionPositionChangeResult(true, startLevel, promotedLevel, nextStepStartYear, promotedSalary, note);
    }

    private InstitutionStart institutionTransferStart(PayrollHistorySnapshot history, PositionChangeCandidate candidate) {
        String regularization = normalizeYearMonth(payrollRepository.findRegularizationYearMonth(history.organizationCode(), history.personCode()));
        if (regularization.isBlank()) {
            return InstitutionStart.ineligible("未找到转正年月，无法确定事业转岗起点。");
        }
        if (regularization.compareTo("200607") >= 0) {
            EducationPromotionSource education = payrollRepository
                    .findLatestEducationForPromotion(history.organizationCode(), history.personCode(), regularization)
                    .orElse(null);
            EducationRegularizationStandard standard = education == null ? null : payrollRepository
                    .findEducationRegularizationStandard(candidate.positionCode(), education.educationCode())
                    .orElse(null);
            if (standard == null) {
                return InstitutionStart.ineligible("2006.07 后转正人员未能按学历匹配新岗位序列转正薪级。");
            }
            return new InstitutionStart(true, regularization, standard.gradeStep(), String.valueOf(yearOf(regularization)),
                    "2006.07 后转正，按转正时学历确定新任岗位序列薪级 " + standard.gradeStep());
        }
        boolean hasManagementOrTechnicalBeforeWorker = payrollRepository
                .findLatestPositionBefore(history.organizationCode(), history.personCode(), normalizeYearMonth(history.positionStartYearMonth()), Set.of("07", "10", "11"))
                .isPresent();
        EducationPromotionSource education = payrollRepository
                .findLatestEducationForPromotion(history.organizationCode(), history.personCode(), "200607")
                .orElse(null);
        EducationRegularizationStandard standard = education == null ? null : payrollRepository
                .findEducationRegularizationStandard(candidate.positionCode(), education.educationCode())
                .orElse(null);
        String reformPosition = hasManagementOrTechnicalBeforeWorker
                ? candidate.positionCode()
                : standard == null ? candidate.positionCode() : normalizeEducationPromotionPositionCode(standard.positionCode());
        int appointmentYears = hasManagementOrTechnicalBeforeWorker
                ? Math.max(1, 2006 - yearOf(candidate.startYearMonth()) + 1)
                : 1;
        int reformYears = history.salaryYears() == null || history.salaryYears() <= 0
                ? Math.max(1, 2006 - yearOf(history.workStartYearMonth()) + 1 - nullToZero(history.interruptedSalaryYears()))
                : history.salaryYears();
        Optional<WageReformStandard> wageReform = payrollRepository.findWageReformStandard(reformPosition, appointmentYears, reformYears);
        if (wageReform.isEmpty()) {
            return InstitutionStart.ineligible("2006.07 前已转正事业人员未能匹配新任岗位序列套改薪级。");
        }
        return new InstitutionStart(
                true,
                "200607",
                wageReform.get().convertedStep(),
                "2006",
                hasManagementOrTechnicalBeforeWorker
                        ? "2006.07 前已转正，曾有管理/专技岗位，按新聘岗位序列套改薪级 " + wageReform.get().convertedStep()
                        : "2006.07 前已转正，工勤转岗，按学历模拟定级岗位、任职年限1年套改薪级 " + wageReform.get().convertedStep());
    }

    private String replayInstitutionSalaryLevel(
            PayrollHistorySnapshot history,
            String startPeriod,
            String startSalaryLevel,
            String targetPrefix) {
        int level = payrollRepository.intValue(startSalaryLevel);
        if (level <= 0) {
            return startSalaryLevel;
        }
        List<PayrollHistorySnapshot> chain = payrollRepository.findHistoryChain(history.organizationCode(), history.personCode());
        for (PayrollHistorySnapshot row : chain) {
            String rowPeriod = normalizeYearMonth(row.calculationYear() + row.calculationMonth());
            if (rowPeriod.compareTo(startPeriod) <= 0) {
                continue;
            }
            if (containsAny(row.calculationType(), "正常档次", "薪级")) {
                level++;
            } else if (containsAny(row.calculationType(), "学历")) {
                String rowLevel = String.valueOf(payrollRepository.intValue(row.positionSalaryGrade()) + payrollRepository.intValue(row.gradeSalaryStep()));
                int value = payrollRepository.intValue(rowLevel);
                if (value > level && isInstitutionPosition(row.positionCode())) {
                    level = value;
                }
            }
        }
        return String.valueOf(level);
    }

    private String nextAssessmentYear(String yearMonth, String fallbackYear) {
        int year = yearOf(yearMonth);
        return year > 0 ? String.valueOf(year + 1) : fallbackYear;
    }

    private AdministrativeReplayResult administrativeReplayResult(PayrollHistorySnapshot current, PositionChangeCandidate candidate) {
        int convertedLevel = payrollRepository.intValue(current.gradeSalaryLevel()) + 7;
        String level = String.valueOf(convertedLevel);
        String step = String.valueOf(
                payrollRepository.intValue(current.positionSalaryGrade()) + payrollRepository.intValue(current.gradeSalaryStep()));
        String levelStartYear = current.levelAssessmentStartYear();
        String stepStartYear = current.stepAssessmentStartYear();
        PositionLevelRange targetRange = payrollRepository.findPositionLevelRange(candidate.positionCode()).orElse(null);
        if (targetRange == null || convertedLevel <= 0 || payrollRepository.intValue(step) <= 0) {
            return AdministrativeReplayResult.ineligible("未找到回到其他类目标职务的级别范围，或当前警员等级档次不完整。");
        }
        int promotedLevelValue = convertedLevel;
        if (isHigherPositionLayer(current.positionCode(), candidate.positionCode())) {
            if (convertedLevel > targetRange.minimumLevel()) {
            promotedLevelValue = targetRange.minimumLevel();
            } else {
                promotedLevelValue = Math.max(1, convertedLevel - 1);
            }
        }
        String promotedLevel = String.valueOf(promotedLevelValue);
        String promotedStep = step;
        if (!promotedLevel.equals(level)) {
            int currentSalary = payrollRepository.gradeSalary(level, step, current.salaryStandardYearMonth());
            promotedStep = firstHigherGradeStep(promotedLevel, currentSalary, current.salaryStandardYearMonth());
            int promotedLevels = convertedLevel - promotedLevelValue;
            if (promotedLevels >= 2) {
                levelStartYear = yearOf(candidate.startYearMonth()) > 0 ? String.valueOf(yearOf(candidate.startYearMonth())) : current.calculationYear();
            }
            if (gradeIncreaseExceedsStepDifference(level, step, promotedLevel, current.salaryStandardYearMonth())) {
                stepStartYear = yearOf(candidate.startYearMonth()) > 0 ? String.valueOf(yearOf(candidate.startYearMonth())) : current.calculationYear();
            }
        }
        int promotedSalary = payrollRepository.gradeSalary(promotedLevel, promotedStep, current.salaryStandardYearMonth());
        return new AdministrativeReplayResult(
                true,
                level,
                step,
                levelStartYear,
                stepStartYear,
                promotedLevel,
                promotedStep,
                promotedSalary,
                "警员回到其他类，按警员等级加 7 得其他类级别；同职务层次级别不变，高于原职务层次时未达最低进最低，已达最低晋升 1 级。");
    }

    private AdministrativeReplayStart administrativeReplayStart(PayrollHistorySnapshot current, List<PayrollHistorySnapshot> chain, PositionChangeCandidate candidate) {
        String regularization = normalizeYearMonth(payrollRepository.findRegularizationYearMonth(current.organizationCode(), current.personCode()));
        if (!regularization.isBlank() && regularization.compareTo("200607") < 0) {
            Optional<PositionChangeCandidate> administrativePosition = payrollRepository.findAdministrativePositionBeforeReform(current.organizationCode(), current.personCode());
            String startPositionCode = administrativePosition.map(PositionChangeCandidate::positionCode)
                    .orElseGet(() -> administrativeEquivalentPosition(current.positionCode()));
            int appointmentYears = Math.max(1, 2006 - yearOf(administrativePosition.map(PositionChangeCandidate::startYearMonth).orElse(current.positionStartYearMonth())) + 1);
            int reformYears = current.salaryYears() == null || current.salaryYears() <= 0
                    ? Math.max(1, 2006 - yearOf(current.workStartYearMonth()) + 1 - nullToZero(current.interruptedSalaryYears()))
                    : current.salaryYears();
            Optional<WageReformStandard> standard = payrollRepository.findWageReformStandard(startPositionCode, appointmentYears, reformYears);
            if (standard.isEmpty()) {
                return AdministrativeReplayStart.ineligible("2006.07 前已转正人员未能按基本信息匹配 2006 套改标准。");
            }
            WageReformStandard start = standard.get();
            int historyStartIndex = firstHistoryIndexAtOrAfter(chain, "200607");
            return new AdministrativeReplayStart(
                    true,
                    historyStartIndex,
                    start.positionCode(),
                    start.convertedLevel(),
                    start.convertedStep(),
                    "2006",
                    "2006",
                    "2006.07 前已转正，按基本信息和 2006 套改标准确定 01 职务套改起点 "
                            + start.positionCode() + " " + start.convertedLevel() + "级" + start.convertedStep() + "档开始回放");
        }
        if (regularization.isBlank()) {
            return AdministrativeReplayStart.ineligible("未找到转正年月，无法按学历转正定级确定起点。");
        }
        RegularizationSalaryPosition resolved = resolveRegularizationSalaryPosition(
                current.organizationCode(),
                current.personCode(),
                regularization,
                candidate.positionCode(),
                regularization);
        EducationRegularizationStandard standard = resolved.standard();
        if (standard == null) {
            return AdministrativeReplayStart.ineligible("2006.07 及以后转正人员未能按学历匹配转正定级标准。");
        }
        boolean institutionRegularization = resolved.institutionRegularization();
        int startIndex = firstHistoryIndexAtOrAfter(chain, regularization);
        String replayPositionCode = resolved.salaryPositionCode();
        String replayNote = institutionRegularization
                ? "2006.07 及以后转正，事业人员按学历转正定级标准确定薪级，岗位取转正时聘任岗位 "
                + replayPositionCode + " " + standard.gradeStep() + "薪级开始回放"
                : resolved.salaryPositionFromStandard()
                        ? "2006.07 及以后转正，未找到转正定级任职记录，按转正定级标准确认执行工资职务 "
                        + replayPositionCode + " " + standard.gradeLevel() + "级" + standard.gradeStep() + "档开始回放"
                        : "2006.07 及以后转正，按学历转正定级标准确定起点 "
                        + replayPositionCode + " " + standard.gradeLevel() + "级" + standard.gradeStep() + "档开始回放";
        return new AdministrativeReplayStart(
                true,
                startIndex,
                replayPositionCode,
                standard.gradeLevel(),
                standard.gradeStep(),
                String.valueOf(yearOf(regularization)),
                String.valueOf(yearOf(regularization)),
                replayNote);
    }

    private int firstHistoryIndexByType(List<PayrollHistorySnapshot> chain, String token) {
        for (int i = 0; i < chain.size(); i++) {
            if (containsAny(chain.get(i).calculationType(), token)) {
                return i;
            }
        }
        return -1;
    }

    private int firstHistoryIndexAtOrAfter(List<PayrollHistorySnapshot> chain, String period) {
        for (int i = 0; i < chain.size(); i++) {
            String rowPeriod = normalizeYearMonth(chain.get(i).calculationYear() + chain.get(i).calculationMonth());
            if (!rowPeriod.isBlank() && rowPeriod.compareTo(period) >= 0) {
                return i;
            }
        }
        return Math.max(0, chain.size() - 1);
    }

    private String administrativeEquivalentPosition(String positionCode) {
        if (positionCode == null || positionCode.length() < 4) {
            return positionCode;
        }
        if (Set.of("21", "22", "23", "24", "25", "26", "27", "28").contains(positionPrefix(positionCode))) {
            return "01" + positionCode.substring(2);
        }
        return positionCode;
    }

    private String normalizeYearMonth(String value) {
        String normalized = value == null ? "" : value.replace(".", "").trim();
        return normalized.length() >= 6 ? normalized.substring(0, 6) : "";
    }

    private String formatYearMonth(String value) {
        String normalized = normalizeYearMonth(value);
        if (normalized.isBlank()) {
            return emptyToDash(value);
        }
        return normalized.substring(0, 4) + "." + normalized.substring(4, 6);
    }

    private String positionDisplay(String code, String name) {
        String normalizedCode = code == null ? "" : code.trim();
        String normalizedName = name == null ? "" : name.trim();
        if (!normalizedCode.isBlank() && !normalizedName.isBlank()) {
            return normalizedCode + " " + normalizedName;
        }
        return emptyToDash(normalizedCode.isBlank() ? normalizedName : normalizedCode);
    }

    private String positionDisplayWithoutCode(String name, String fallbackCode) {
        String normalizedName = name == null ? "" : name.trim();
        return normalizedName.isBlank() ? emptyToDash(fallbackCode) : normalizedName;
    }

    private String positionNameForProjectionStart(Optional<PositionChangeCandidate> position, String fallbackCode) {
        return position
                .filter(candidate -> fallbackCode != null && fallbackCode.equals(candidate.positionCode()))
                .map(PositionChangeCandidate::positionName)
                .orElse("");
    }

    private String positionLabel(String baseSalarySource) {
        return "SALARY_LEVEL".equals(baseSalarySource) ? "岗位" : "职务";
    }

    private String levelStepLabel(String baseSalarySource) {
        return "SALARY_LEVEL".equals(baseSalarySource) ? "薪级" : "级别/档次";
    }

    private String levelStepDisplay(String baseSalarySource, String level, String step) {
        return levelStepDisplay(baseSalarySource, level, step, "0");
    }

    private String levelStepDisplay(String baseSalarySource, String level, String step, String gradeStepDifferenceCount) {
        if ("SALARY_LEVEL".equals(baseSalarySource)) {
            return emptyToDash(step);
        }
        String normalizedLevel = level == null ? "" : level.trim();
        String normalizedStep = step == null ? "" : step.trim();
        int stepDifferenceCount = payrollRepository.intValue(gradeStepDifferenceCount);
        if ("GRADE".equals(baseSalarySource) && stepDifferenceCount > 0) {
            return normalizedLevel + "-" + normalizedStep + "+" + stepDifferenceCount;
        }
        if (!normalizedLevel.isBlank() && !normalizedStep.isBlank()) {
            return normalizedLevel + "-" + normalizedStep;
        }
        return emptyToDash(normalizedLevel.isBlank() ? normalizedStep : normalizedLevel);
    }

    private boolean containsAny(String value, String... tokens) {
        String text = value == null ? "" : value;
        for (String token : tokens) {
            if (text.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private PoliceOfficerConversionResult policeOfficerConversionResult(
            PayrollHistorySnapshot history,
            PositionChangeCandidate candidate,
            PositionLevelRange targetLevelRange,
            int currentLevel,
            String currentStep,
            Integer currentGradeSalary) {
        return policeOfficerConversionResult(
                history.positionCode(),
                candidate.positionCode(),
                targetLevelRange,
                currentLevel,
                currentStep,
                currentGradeSalary,
                history.salaryStandardYearMonth());
    }

    private PoliceOfficerConversionResult policeOfficerConversionResult(
            String currentPositionCode,
            String targetPositionCode,
            PositionLevelRange targetLevelRange,
            int currentLevel,
            String currentStep,
            Integer currentGradeSalary,
            String salaryStandardYearMonth) {
        if (targetLevelRange == null || currentLevel <= 0 || payrollRepository.intValue(currentStep) <= 0) {
            return PoliceOfficerConversionResult.ineligible();
        }
        int sameRankCivilLevel = targetLevelRange.minimumLevel() + 7;
        boolean highPositionPromotion = isPoliceHighPositionPromotion(currentPositionCode, targetPositionCode);
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
            promotedGradeSalary = payrollRepository.gradeSalary(promotedLevel, promotedStep, salaryStandardYearMonth);
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
                || (Set.of("0201", "0171", "0170").contains(currentPositionCode) && Set.of("2102", "2101").contains(targetPositionCode));
    }

    private boolean isHigherPositionLayer(String currentPositionCode, String targetPositionCode) {
        int currentLayer = positionLayer(currentPositionCode);
        int targetLayer = positionLayer(targetPositionCode);
        return currentLayer > 0 && targetLayer > 0 && targetLayer < currentLayer;
    }

    private boolean isLowerPositionLayer(String currentPositionCode, String targetPositionCode) {
        int currentLayer = positionLayer(currentPositionCode);
        int targetLayer = positionLayer(targetPositionCode);
        return currentLayer > 0 && targetLayer > currentLayer;
    }

    private int positionLayer(String positionCode) {
        if (positionCode == null || positionCode.length() < 4) {
            return 0;
        }
        return switch (positionCode) {
            case "0150", "0151", "2101", "2201", "2301", "2401", "2501", "2601", "2701", "2801" -> 1;
            case "0160", "0161", "2102", "2202", "2302", "2402", "2502", "2602", "2702", "2802" -> 2;
            case "0170", "0171", "0201", "2103", "2104", "2203", "2204", "2303", "2304", "2403", "2404", "2503", "2504", "2603", "2604", "2703", "2704", "2803", "2804" -> 3;
            case "0180", "0181", "0202", "2105", "2106", "2205", "2206", "2305", "2306", "2405", "2406", "2505", "2506", "2605", "2606", "2705", "2706", "2805", "2806" -> 4;
            case "0190", "0191", "0203", "2107", "2108", "2207", "2208", "2307", "2308", "2407", "2408", "2507", "2508", "2607", "2608", "2707", "2708", "2807", "2808" -> 5;
            case "01A0", "01A1", "0204", "0205", "2109", "2110", "2209", "2210", "2309", "2310", "2409", "2410", "2509", "2510", "2609", "2610", "2709", "2710", "2809", "2810" -> 6;
            case "01B0", "0206", "2111", "2211", "2311", "2411", "2511", "2611", "2711", "2811" -> 7;
            case "01C0", "0207", "2112", "2212", "2312", "2412", "2512", "2612", "2712", "2812" -> 8;
            default -> 0;
        };
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

    private record RankConversionResult(
            boolean eligible,
            String promotedLevel,
            String promotedStep,
            Integer promotedGradeSalary) {

        static RankConversionResult ineligible() {
            return new RankConversionResult(false, null, null, 0);
        }
    }

    private record InstitutionPositionChangeResult(
            boolean eligible,
            String startSalaryLevel,
            String promotedSalaryLevel,
            String nextStepAssessmentStartYear,
            Integer promotedSalary,
            String note) {

        static InstitutionPositionChangeResult ineligible(String note) {
            return new InstitutionPositionChangeResult(false, null, null, null, 0, note);
        }
    }

    private record InstitutionStart(
            boolean eligible,
            String historyStartPeriod,
            String startSalaryLevel,
            String stepStartYear,
            String note) {

        static InstitutionStart ineligible(String note) {
            return new InstitutionStart(false, null, null, null, note);
        }
    }

    private record AdministrativeReplayResult(
            boolean eligible,
            String replayedLevel,
            String replayedStep,
            String levelStartYear,
            String stepStartYear,
            String promotedLevel,
            String promotedStep,
            Integer promotedGradeSalary,
            String note) {

        static AdministrativeReplayResult ineligible(String note) {
            return new AdministrativeReplayResult(false, null, null, null, null, null, null, 0, note);
        }
    }

    private record AdministrativeReplayStart(
            boolean eligible,
            int historyStartIndex,
            String positionCode,
            String level,
            String step,
            String levelStartYear,
            String stepStartYear,
            String note) {

        static AdministrativeReplayStart ineligible(String note) {
            return new AdministrativeReplayStart(false, 0, null, null, null, null, null, note);
        }
    }

    private record WageReformSelection(
            String positionCode,
            String positionName,
            String level,
            String step,
            String note) {

        static WageReformSelection fromCurrent(WageReformStandard standard, String positionName, String note) {
            return new WageReformSelection(standard.positionCode(), positionName, standard.convertedLevel(), standard.convertedStep(), note);
        }

        WageReformSelection withLevelStep(String level, String step, String extraNote) {
            return new WageReformSelection(positionCode, positionName, level, step, note + extraNote);
        }
    }

    private record WageProjectionStart(
            boolean eligible,
            String period,
            String positionCode,
            String positionName,
            String level,
            String stepOrSalaryLevel,
            String levelStartYear,
            String stepStartYear,
            String positionStartYearMonth,
            int wageReformYears,
            String note) {

        static WageProjectionStart ineligible(String note) {
            return new WageProjectionStart(false, "", "", "", "", "", "", "", "", 0, note);
        }
    }

    private record WageProjectionRun(
            PayrollHistorySnapshot latest,
            WageProjectionStart start,
            String targetPeriod,
            String regularizationYearMonth,
            WageProjectionState state,
            List<String> lines,
            boolean eligible) {
    }

    private String positionChangeType(
            String currentPositionCode,
            String newPositionCode,
            boolean sequenceConversion,
            boolean policeOfficerConversion,
            boolean judicialConversion,
            boolean rankConversion,
            boolean institutionPositionChange) {
        if (newPositionCode == null || newPositionCode.equals(currentPositionCode)) {
            return "未变化";
        }
        if (policeOfficerConversion) {
            return "警员套改";
        }
        if (judicialConversion) {
            return "法检套改";
        }
        if (rankConversion) {
            return "职级套改";
        }
        if (isLegacyPolicePositionConversion(positionPrefix(currentPositionCode), positionPrefix(newPositionCode))) {
            return "警员套改";
        }
        if (institutionPositionChange) {
            return "事业岗位变动";
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
            boolean rankConversion,
            RankConversionResult rankConversionResult,
            InstitutionPositionChangeResult institutionResult,
            String judicialConversionStep,
            AdministrativeReplayResult administrativeReplayResult,
            PoliceOfficerConversionResult policeOfficerResult) {
        if (administrativeReplayResult != null) {
            return administrativeReplayResult.eligible()
                    ? "识别为警员回到综合管理类；" + administrativeReplayResult.note()
                    : "识别为警员回到综合管理类；" + administrativeReplayResult.note();
        }
        if (policeOfficerConversion) {
            if (policeOfficerResult == null || !policeOfficerResult.eligible()) {
                return "识别为警员套改，但未找到套改后职务对应的等级范围，暂不能试算。";
            }
            return policeOfficerResult.highPositionPromotion()
                    ? "识别为警员套改；高套职务按旧系统 jytg 规则先按同级职务平套，再按职务晋升政策晋升到套改后职务。"
                    : "识别为警员套改；按旧系统 jytg 规则先判断是否达到套改后职务最低等级，未达最低进最低，已达最低保持平套等级。";
        }
        if (rankConversion) {
            if (rankConversionResult == null || !rankConversionResult.eligible()) {
                return "识别为职级套改，但未找到目标职级对应的级别范围，暂不能试算。";
            }
            return "识别为职级套改；从 01 前缀职务转为职级类序列，按警员套改同类规则处理。职务工资减少额保留到 PGBC，后续职务晋升时从职务工资增资额中冲减。";
        }
        if (institutionResult != null) {
            return institutionResult.eligible()
                    ? institutionResult.note()
                    : institutionResult.note();
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

    private List<String> positionChangeExplanationLines(
            PayrollHistorySnapshot history,
            PositionChangeCandidate candidate,
            String changeType,
            String currentStep,
            PositionLevelRange levelRange,
            PoliceOfficerConversionResult policeOfficerResult,
            RankConversionResult rankConversionResult,
            InstitutionPositionChangeResult institutionResult,
            String judicialConversionStep,
            AdministrativeReplayResult administrativeReplayResult,
            String promotedLevel,
            String promotedStep,
            String nextLevelAssessmentStartYear,
            String nextStepAssessmentStartYear,
            int positionSalaryIncrease,
            int pgbcRetainedAmount,
            int pgbcOffsetAmount,
            int netPositionSalaryIncrease,
            int gradeSalaryIncrease,
            boolean gradeIncreaseExceedsStepDifference,
            String note) {
        List<String> lines = new ArrayList<>();
        lines.add("识别类型：" + changeType + "。");
        lines.add("当前执行工资：" + history.calculationYear() + history.calculationMonth()
                + "，原职务 " + history.positionCode() + " " + history.positionName()
                + "，原级别/薪级 " + emptyToDash(history.gradeSalaryLevel()) + "/" + emptyToDash(currentStep) + "。");
        lines.add("新任职务：" + emptyToDash(candidate.positionCode()) + " " + emptyToDash(candidate.positionName())
                + "，任职年月 " + emptyToDash(candidate.startYearMonth())
                + "，执行年月 " + emptyToDash(nextMonth(candidate.startYearMonth())) + "。");
        if (levelRange != null) {
            lines.add("新职务级别范围：最低 " + levelRange.minimumLevel() + "，最高 " + levelRange.maximumLevel() + "。");
        }
        if (policeOfficerResult != null) {
            if (policeOfficerResult.eligible()) {
                lines.add("警员套改：平套等级/档次 " + policeOfficerResult.sameRankLevel() + "/" + policeOfficerResult.sameRankStep()
                        + (policeOfficerResult.highPositionPromotion() ? "，高套职务后再晋升一级。" : "。"));
            } else {
                lines.add("警员套改：未能取得目标等级范围，未参与试算。");
            }
        }
        if (rankConversionResult != null) {
            lines.add(rankConversionResult.eligible()
                    ? "职级套改：按最低级别/跨层晋升规则试算为 " + rankConversionResult.promotedLevel() + "级" + rankConversionResult.promotedStep() + "档。"
                    : "职级套改：未找到目标职级级别范围。");
        }
        if (judicialConversionStep != null) {
            lines.add(judicialConversionStep.isBlank()
                    ? "法检套改：未在 bz06_fjtgb 找到对应套改档次。"
                    : "法检套改：按 bz06_fjtgb 取得套改档次 " + judicialConversionStep + "。");
        }
        if (administrativeReplayResult != null) {
            lines.add(administrativeReplayResult.note());
            if (administrativeReplayResult.eligible()) {
                lines.add("回放结果：级别/档次 " + administrativeReplayResult.replayedLevel() + "/" + administrativeReplayResult.replayedStep()
                        + "，xckhndjb/xckhndzw " + emptyToDash(administrativeReplayResult.levelStartYear())
                        + "/" + emptyToDash(administrativeReplayResult.stepStartYear()) + "。");
            }
        }
        if (institutionResult != null) {
            lines.add(institutionResult.note());
            if (institutionResult.eligible()) {
                lines.add("事业岗位变动薪级：起点 " + institutionResult.startSalaryLevel()
                        + "，试算 " + institutionResult.promotedSalaryLevel()
                        + "，更新后 xckhndzw=" + emptyToDash(institutionResult.nextStepAssessmentStartYear()) + "。");
            }
        }
        lines.add("试算结果：级别/薪级 " + emptyToDash(promotedLevel) + "/" + emptyToDash(promotedStep)
                + "，更新后 xckhndjb/xckhndzw " + emptyToDash(nextLevelAssessmentStartYear) + "/" + emptyToDash(nextStepAssessmentStartYear) + "。");
        lines.add("增资分解：职务增资 " + positionSalaryIncrease
                + "，PGBC保留 " + pgbcRetainedAmount
                + "，PGBC冲销 " + pgbcOffsetAmount
                + "，职务净增 " + netPositionSalaryIncrease
                + "，级别/薪级增资 " + gradeSalaryIncrease + "。");
        if (gradeIncreaseExceedsStepDifference) {
            lines.add("级别增资超过下一级别一个档差，xckhndzw 需从本次变动年度重新计算。");
        }
        lines.add("结论：" + note);
        return lines;
    }

    private String emptyToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
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

    private void appendMissingAssessmentPrompt(
            List<String> lines,
            String organizationCode,
            String personCode,
            int startYear,
            int endYear,
            Set<Integer> promptedYears) {
        List<Integer> missingYears = missingAssessmentYears(organizationCode, personCode, startYear, endYear)
                .stream()
                .filter(promptedYears::add)
                .toList();
        if (!missingYears.isEmpty()) {
            lines.add("缺少 " + missingYears.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining("、"))
                    + " 年度考核结果，请先补录后再推算正常晋升。");
        }
    }

    private List<Integer> missingAssessmentYears(
            String organizationCode,
            String personCode,
            int startYear,
            int endYear) {
        if (startYear <= 0 || endYear < startYear) {
            return List.of();
        }
        Set<Integer> existingYears = payrollRepository.assessmentYears(organizationCode, personCode, startYear, endYear);
        List<Integer> missing = new ArrayList<>();
        for (int year = startYear; year <= endYear; year++) {
            if (!existingYears.contains(year)) {
                missing.add(year);
            }
        }
        return missing;
    }

    private boolean isLevelPromotionPosition(String positionCode) {
        return positionCode != null && positionCode.length() >= 2
                && LEVEL_PROMOTION_POSITION_PREFIXES.contains(positionCode.substring(0, 2));
    }

    private boolean isInstitutionPosition(String positionCode) {
        return positionCode != null && positionCode.length() >= 2
                && INSTITUTION_POSITION_PREFIXES.contains(positionCode.substring(0, 2));
    }

    private boolean supportsRuralTeacherYearAllowance(String positionCode) {
        return "SALARY_LEVEL".equals(baseSalarySource(positionCode));
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

    private String approvalCaption(String fieldName, String fallbackCaption) {
        String normalized = fieldName == null ? "" : fieldName.trim().toUpperCase();
        return switch (normalized) {
            case "PGBC" -> "工改保留职务工资";
            case "NJBT" -> "农教补贴";
            default -> fallbackCaption;
        };
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

    private boolean normalizedEquals(String left, String right) {
        String normalizedLeft = emptyToNull(left);
        String normalizedRight = emptyToNull(right);
        if (normalizedLeft == null) {
            return normalizedRight == null;
        }
        return normalizedLeft.equals(normalizedRight);
    }

    private String projectionPeriod(String period) {
        String normalized = period == null ? "" : period.replace(".", "").replace("-", "").trim();
        if (normalized.length() >= 6) {
            return normalized.substring(0, 6);
        }
        YearMonth now = YearMonth.now();
        return "%04d%02d".formatted(now.getYear(), now.getMonthValue());
    }
}
