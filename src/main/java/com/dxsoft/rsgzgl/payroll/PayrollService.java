package com.dxsoft.rsgzgl.payroll;

import com.dxsoft.rsgzgl.common.NotFoundException;
import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.security.AccessControlService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
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
    private static final String LEVEL_PROMOTION_PROCESSED_CHANGE_TYPE = "正常级别";
    private static final Set<String> LEVEL_PROMOTION_ROLLBACK_CHANGE_TYPES = Set.of(LEVEL_PROMOTION_PROCESSED_CHANGE_TYPE);
    private static final Set<String> LEVEL_PROMOTION_LIST_EXCLUDED_CHANGE_TYPES = Set.of("调入定资");
    private static final int REFORM_LEVEL_ROLLING_FIRST_YEAR = 2007;
    private static final int REFORM_LEVEL_ROLLING_LAST_YEAR = 2010;
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

    private static final Set<String> PAYROLL_CHANGE_COMPARISON_FIELDS = Set.of(
            "ZWGZSE2", "JBGZSE2", "JSDJGZ2", "JHLJT", "BLFB2", "JJJY2", "GWJT2", "DFBT2",
            "SDBT", "JXJT", "SIDBT", "TGBLBF", "PGBC", "NJBT", "QTBT", "JSFSZWTG2", "FDGZ2", "HJ2");

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
        if (emptyToNull(period) == null) {
            PayrollCalculationContext context = calculationContext(uid);
            PayrollHistorySnapshot history = context.latestHistory();
            return buildCalculationPreview(uid, history, context, history.calculationYear() + history.calculationMonth());
        }
        String targetPeriod = projectionPeriod(period);
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
        WageProjectionRun projection = runWageProjection(uid, period, null, null, true);
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
                    projection.lines(),
                    List.of());
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
                projection.lines(),
                projection.stepDetails());
    }

    private WageProjectionRun runWageProjection(int uid, String period) {
        return runWageProjection(uid, period, null, null, false);
    }

    private WageProjectionRun runWageProjection(int uid, String period, ProjectionAuditStop auditStop) {
        return runWageProjection(uid, period, auditStop, null, false);
    }

    private WageProjectionRun runWageProjection(
            int uid,
            String period,
            ProjectionAuditStop auditStop,
            PayrollHistorySnapshot standardContext) {
        return runWageProjection(uid, period, auditStop, standardContext, false);
    }

    private WageProjectionRun runWageProjection(
            int uid,
            String period,
            ProjectionAuditStop auditStop,
            PayrollHistorySnapshot standardContext,
            boolean captureStepDetails) {
        String targetPeriod = projectionPeriod(period);
        PayrollHistorySnapshot latest = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        PayrollHistorySnapshot standards = standardContext != null ? standardContext : latest;
        accessControlService.requireOrganization(latest.organizationCode());
        String regularizationYearMonth = payrollRepository.findRegularizationYearMonth(latest.organizationCode(), latest.personCode());
        List<String> lines = new ArrayList<>();
        List<WageProjectionStepDetail> stepDetails = captureStepDetails ? new ArrayList<>() : null;
        lines.add("目标年月：" + targetPeriod + "。");
        WageProjectionStart start = wageProjectionStart(latest);
        if (!start.eligible()) {
            lines.add(start.note());
            return new WageProjectionRun(latest, start, targetPeriod, regularizationYearMonth, null, lines, false, stepDetails);
        }
        RankAllowanceState initialRankAllowance = initialRankAllowanceState(latest, start.period());
        WageProjectionState state = createInitialProjectionState(standards, start, initialRankAllowance);
        lines.add(start.note());
        captureProjectionStep(stepDetails, latest, start.period(), start.note(), state);
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
                targetPeriod,
                regularizationYearMonth,
                latest);
        List<String> allowanceStandardPeriods = payrollRepository.findAllowanceStandardPeriodsBetween(
                latest.organizationCode(), startPeriod, targetPeriod);
        Set<String> appliedAllowanceStandards = new java.util.HashSet<>();
        payrollRepository.findPositionAtOrBefore(latest.organizationCode(), latest.personCode(), targetPeriod)
                .ifPresent(position -> {
                    lines.add("目标年月任职记录：" + position.startYearMonth() + " " + position.positionCode() + " " + position.positionName() + "。");
                });
        int baseYear = yearOf(startPeriod);
        int targetYear = yearOf(targetPeriod);
        Set<Integer> promptedMissingAssessmentYears = new java.util.TreeSet<>();
        int eventIndex = 0;
        boolean auditStopped = false;
        for (int year = Math.max(2007, baseYear + 1); year <= targetYear && !auditStopped; year++) {
            final int projectionYear = year;
            String yearStart = String.format("%04d01", projectionYear);
            while (eventIndex < projectionEvents.size()
                    && projectionEvents.get(eventIndex).period().compareTo(yearStart) < 0) {
                WageProjectionEvent event = projectionEvents.get(eventIndex);
                if (skipStandaloneAllowanceProjectionEvent(event, appliedAllowanceStandards)) {
                    eventIndex++;
                    continue;
                }
                state = applyProjectionEventWithCapture(stepDetails, latest, lines, event.period(), state,
                        current -> applyCapturedWageProjectionChange(
                                current, event, lines, latest,
                                latest.organizationCode(),
                                allowanceStandardPeriods, appliedAllowanceStandards));
                eventIndex++;
                if (auditStopReachedAfterPositionChange(auditStop, event, targetPeriod)) {
                    auditStopped = true;
                    break;
                }
            }
            if (auditStopped) {
                break;
            }
            // 任职/变动次月恰为当年 1 月（如 2011.12 任职→201201）须在年初晋档前试算
            while (eventIndex < projectionEvents.size()
                    && yearStart.equals(projectionEvents.get(eventIndex).period())) {
                WageProjectionEvent event = projectionEvents.get(eventIndex);
                if (!isYearStartPrePromotionProjectionEvent(event)) {
                    break;
                }
                if (skipStandaloneAllowanceProjectionEvent(event, appliedAllowanceStandards)) {
                    eventIndex++;
                    continue;
                }
                state = applyProjectionEventWithCapture(stepDetails, latest, lines, event.period(), state,
                        current -> applyCapturedWageProjectionChange(
                                current, event, lines, latest,
                                latest.organizationCode(),
                                allowanceStandardPeriods, appliedAllowanceStandards));
                eventIndex++;
                if (auditStopReachedAfterPositionChange(auditStop, event, targetPeriod)) {
                    auditStopped = true;
                    break;
                }
            }
            if (auditStopped) {
                break;
            }
            if (supportsGradePromotion(state) && payrollRepository.intValue(state.level()) > 1) {
                int levelStart = assessmentStartYear(state.levelStartYear(), start.positionStartYearMonth(), state.positionCode());
                int stepStart = assessmentStartYear(state.stepStartYear(), start.positionStartYearMonth(), state.positionCode());
                appendMissingAssessmentPrompt(lines, latest.organizationCode(), latest.personCode(), levelStart, projectionYear - 1, promptedMissingAssessmentYears);
                appendMissingAssessmentPrompt(lines, latest.organizationCode(), latest.personCode(), stepStart, projectionYear - 1, promptedMissingAssessmentYears);
                final int qualifiedLevel = payrollRepository.countQualifiedAssessmentYears(latest.organizationCode(), latest.personCode(), levelStart, projectionYear - 1);
                final int qualifiedStep = payrollRepository.countQualifiedAssessmentYears(latest.organizationCode(), latest.personCode(), stepStart, projectionYear - 1);
                boolean specialLevelPromotionDue = specialWageReformLevelPromotionDue(state, start, latest, projectionYear);
                boolean levelDue = !specialLevelPromotionDue && qualifiedLevel >= 5;
                boolean stepDue = qualifiedStep >= 2;
                if (specialLevelPromotionDue) {
                    state = applyProjectionEventWithCapture(
                            stepDetails,
                            latest,
                            lines,
                            yearStart,
                            state,
                            current -> applyMergedAllowanceStandardAtPeriodIfPresent(
                                    applyReformLevelRollingState(current, start, latest, projectionYear, lines),
                                    yearStart,
                                    latest.organizationCode(),
                                    lines,
                                    allowanceStandardPeriods,
                                    appliedAllowanceStandards));
                    if (auditStopReached(auditStop, projectionYear, AuditMilestone.AFTER_REFORM_ROLLING)) {
                        auditStopped = true;
                    }
                }
                if (!auditStopped && levelDue) {
                    state = applyProjectionEventWithCapture(
                            stepDetails,
                            latest,
                            lines,
                            yearStart,
                            state,
                            current -> applyMergedAllowanceStandardAtPeriodIfPresent(
                                    advanceSimulatedNormalLevelPromotion(current, start, latest, projectionYear, qualifiedLevel, lines),
                                    yearStart,
                                    latest.organizationCode(),
                                    lines,
                                    allowanceStandardPeriods,
                                    appliedAllowanceStandards));
                    if (auditStopReached(auditStop, projectionYear, AuditMilestone.AFTER_NORMAL_LEVEL)) {
                        auditStopped = true;
                    }
                }
                if (!auditStopped && stepDue) {
                    state = applyProjectionEventWithCapture(
                            stepDetails,
                            latest,
                            lines,
                            yearStart,
                            state,
                            current -> applyMergedAllowanceStandardAtPeriodIfPresent(
                                    promoteCivilServantGradeStep(
                                            current,
                                            projectionYear,
                                            lines,
                                            "累计 " + qualifiedStep + " 年考核合格"),
                                    yearStart,
                                    latest.organizationCode(),
                                    lines,
                                    allowanceStandardPeriods,
                                    appliedAllowanceStandards));
                    if (auditStopReached(auditStop, projectionYear, AuditMilestone.AFTER_STEP_PROMOTION)) {
                        auditStopped = true;
                    }
                }
            } else if (!auditStopped && "SALARY_LEVEL".equals(state.baseSalarySource())) {
                int stepStart = assessmentStartYear(state.stepStartYear(), start.positionStartYearMonth(), state.positionCode());
                appendMissingAssessmentPrompt(lines, latest.organizationCode(), latest.personCode(), stepStart, projectionYear - 1, promptedMissingAssessmentYears);
                final int qualifiedStep = payrollRepository.countQualifiedAssessmentYears(latest.organizationCode(), latest.personCode(), stepStart, projectionYear - 1);
                if (qualifiedStep >= 1) {
                    final String promotedStep = String.valueOf(payrollRepository.intValue(state.stepOrSalaryLevel()) + 1);
                    state = applyProjectionEventWithCapture(
                            stepDetails,
                            latest,
                            lines,
                            yearStart,
                            state,
                            current -> {
                                WageProjectionState before = current;
                                WageProjectionState next = projectionWithStep(current, promotedStep, String.valueOf(projectionYear));
                                appendProjectionLine(lines, before, next, projectionYear + " 年：事业岗位累计 " + qualifiedStep + " 年考核合格，晋升薪级到 " + promotedStep + "。");
                                return applyMergedAllowanceStandardAtPeriodIfPresent(
                                        next,
                                        yearStart,
                                        latest.organizationCode(),
                                        lines,
                                        allowanceStandardPeriods,
                                        appliedAllowanceStandards);
                            });
                    if (auditStopReached(auditStop, projectionYear, AuditMilestone.AFTER_STEP_PROMOTION)) {
                        auditStopped = true;
                    }
                }
            }
            if (auditStopped) {
                break;
            }
            String nextYearStart = String.format("%04d01", year + 1);
            while (eventIndex < projectionEvents.size()
                    && projectionEvents.get(eventIndex).period().compareTo(nextYearStart) < 0) {
                WageProjectionEvent event = projectionEvents.get(eventIndex);
                if (skipStandaloneAllowanceProjectionEvent(event, appliedAllowanceStandards)) {
                    eventIndex++;
                    continue;
                }
                state = applyProjectionEventWithCapture(stepDetails, latest, lines, event.period(), state,
                        current -> applyCapturedWageProjectionChange(
                                current, event, lines, latest,
                                latest.organizationCode(),
                                allowanceStandardPeriods, appliedAllowanceStandards));
                eventIndex++;
                if (auditStopReachedAfterPositionChange(auditStop, event, targetPeriod)) {
                    auditStopped = true;
                    break;
                }
            }
        }
        if (!auditStopped) {
            while (eventIndex < projectionEvents.size()) {
                WageProjectionEvent event = projectionEvents.get(eventIndex);
                if (skipStandaloneAllowanceProjectionEvent(event, appliedAllowanceStandards)) {
                    eventIndex++;
                    continue;
                }
                state = applyProjectionEventWithCapture(stepDetails, latest, lines, event.period(), state,
                        current -> applyCapturedWageProjectionChange(
                                current, event, lines, latest,
                                latest.organizationCode(),
                                allowanceStandardPeriods, appliedAllowanceStandards));
                eventIndex++;
                if (auditStopReachedAfterPositionChange(auditStop, event, targetPeriod)) {
                    auditStopped = true;
                    break;
                }
            }
        }
        if (captureStepDetails) {
            WageProjectionState finalState = finalizeProjectionState(state, targetPeriod, latest.organizationCode());
            captureProjectionStep(stepDetails, latest, targetPeriod, "目标年月工资明细", finalState);
            state = finalState;
        }
        return new WageProjectionRun(latest, start, targetPeriod, regularizationYearMonth, state, lines, true, stepDetails);
    }

    private boolean auditStopReached(ProjectionAuditStop auditStop, int year, AuditMilestone milestone) {
        return auditStop != null
                && auditStop.stopPeriod() == null
                && auditStop.milestone() == milestone
                && auditStop.year() == year;
    }

    private boolean auditStopReachedAfterPositionChange(
            ProjectionAuditStop auditStop,
            WageProjectionEvent event,
            String targetPeriod) {
        return auditStop != null
                && auditStop.milestone() == AuditMilestone.AFTER_POSITION_CHANGE
                && event.position() != null
                && auditStop.stopPeriod() != null
                && projectionPeriod(event.period()).equals(projectionPeriod(auditStop.stopPeriod()));
    }

    private ProjectionAuditStop projectionAuditStopFor(PayrollHistorySnapshot record) {
        String changeType = emptyToNull(record.calculationType());
        if (changeType == null) {
            return null;
        }
        if (isPositionChangeAuditType(changeType)) {
            return new ProjectionAuditStop(0, AuditMilestone.AFTER_POSITION_CHANGE, historyCalculationPeriod(record));
        }
        AuditMilestone milestone = switch (changeType) {
            case "级别滚动" -> AuditMilestone.AFTER_REFORM_ROLLING;
            case "正常级别" -> AuditMilestone.AFTER_NORMAL_LEVEL;
            default -> containsAny(changeType, "正常档次", "正常薪级") ? AuditMilestone.AFTER_STEP_PROMOTION : null;
        };
        if (milestone == null) {
            return null;
        }
        return new ProjectionAuditStop(yearOf(historyCalculationPeriod(record)), milestone, null);
    }

    private boolean isPositionChangeAuditType(String changeType) {
        return containsAny(
                changeType,
                "职务变化",
                "职级晋升",
                "同序列职务变化",
                "事业岗位变动",
                "警员套改",
                "警务套改",
                "法检套改",
                "职级套改",
                "转换序列");
    }

    private enum AuditMilestone {
        AFTER_REFORM_ROLLING,
        AFTER_NORMAL_LEVEL,
        AFTER_STEP_PROMOTION,
        AFTER_POSITION_CHANGE
    }

    private record ProjectionAuditStop(int year, AuditMilestone milestone, String stopPeriod) {
    }

    private WageProjectionState finalizeProjectionState(
            WageProjectionState state,
            String targetPeriod,
            String organizationCode) {
        return finalizeProjectionSalaryStandard(state, targetPeriod);
    }

    /** 对账金额按该条调资记录的标准年月重算，避免沿用最新调资记录上的晚近标准。 */
    private WageProjectionState bindStandardsForHistoryRecord(
            WageProjectionState state,
            PayrollHistorySnapshot record,
            String period,
            String organizationCode) {
        String salaryStandard = emptyToNull(record.salaryStandardYearMonth());
        if (salaryStandard == null) {
            salaryStandard = payrollRepository.latestBasicSalaryStandardAtOrBefore(period);
        }
        String allowanceStandard = resolveAllowanceStandardYearMonth(
                period,
                organizationCode,
                state.positionCode(),
                record.allowanceStandardYearMonth());
        WageProjectionState bound = state;
        if (emptyToNull(salaryStandard) != null) {
            bound = bound.withSalaryStandard(salaryStandard);
        }
        if (emptyToNull(allowanceStandard) != null) {
            bound = bound.withAllowanceStandard(allowanceStandard);
        }
        if (emptyToNull(record.rankAllowanceStandardYearMonth()) != null) {
            bound = bound.withRankAllowance(
                    record.rankName(),
                    record.rankAllowanceStandardYearMonth(),
                    nullToZero(record.storedRankAllowance()),
                    bound.rankAllowanceCategory());
        }
        return bound;
    }

    /**
     * 对账金额与推算分步一致：优先取本条调资类型对应分步上的工资/津补贴标准，
     * 避免同月后续调标（如 2008.10 津补贴调标）提前套用到 2008.10 职务变化对账。
     */
    private WageProjectionState bindAmountStateForHistoryAudit(
            WageProjectionRun projection,
            PayrollHistorySnapshot record,
            PayrollHistorySnapshot latest,
            String period) {
        Optional<WageProjectionStepDetail> auditStep = findAuditStepDetailForRecord(
                projection.stepDetails(), record, period);
        if (auditStep.isEmpty()) {
            auditStep = findProjectionStepAtPeriod(projection.stepDetails(), period);
        }
        if (auditStep.isPresent()) {
            return amountStateFromAuditStep(projection.state(), auditStep.get(), latest);
        }
        WageProjectionState projectedState = projection.state();
        WageProjectionState bound = bindStandardsForHistoryRecord(
                projectedState, record, period, record.organizationCode());
        String projectedAllowance = emptyToNull(projectedState.allowanceStandardYearMonth());
        if (projectedAllowance != null) {
            bound = bound.withAllowanceStandard(projectedAllowance);
            return clampProjectionSalaryStandard(bound, projectionPeriod(period));
        }
        return bindStandardsForProjectionPeriod(bound, latest, period);
    }

    private Optional<WageProjectionStepDetail> findAuditStepDetailForRecord(
            List<WageProjectionStepDetail> stepDetails,
            PayrollHistorySnapshot record,
            String period) {
        if (stepDetails == null || stepDetails.isEmpty()) {
            return Optional.empty();
        }
        String changeType = record.calculationType();
        String keyword = auditStepKeywordForChangeType(changeType);
        if (keyword == null) {
            return findProjectionStepAtPeriod(stepDetails, period);
        }
        List<WageProjectionStepDetail> matches = new ArrayList<>(stepDetails.stream()
                .filter(step -> period.equals(step.period())
                        && step.description() != null
                        && step.description().contains(keyword))
                .toList());
        if (matches.isEmpty() && changeType != null && changeType.contains("套改")) {
            matches.addAll(stepDetails.stream()
                    .filter(step -> period.equals(step.period())
                            && step.description() != null
                            && (step.description().contains("试用期")
                                    || step.description().contains("见习工资")))
                    .toList());
        }
        if (matches.isEmpty()) {
            return findProjectionStepAtPeriod(stepDetails, period);
        }
        List<WageProjectionStepDetail> matchCandidates = matches;
        return matchCandidates.stream()
                .filter(step -> emptyToNull(record.positionCode()) != null
                        && record.positionCode().equals(step.positionCode()))
                .findFirst()
                .or(() -> matches.stream()
                        .filter(step -> step.description() != null
                                && (step.description().contains("试用期")
                                        || step.description().contains("见习工资")))
                        .findFirst())
                .or(() -> matches.stream().findFirst());
    }

    private Optional<WageProjectionStepDetail> findProjectionStepAtPeriod(
            List<WageProjectionStepDetail> stepDetails,
            String period) {
        if (stepDetails == null || stepDetails.isEmpty() || period == null || period.isBlank()) {
            return Optional.empty();
        }
        WageProjectionStepDetail last = null;
        for (WageProjectionStepDetail step : stepDetails) {
            if (period.equals(step.period())) {
                last = step;
            }
        }
        return Optional.ofNullable(last);
    }

    private String auditStepKeywordForChangeType(String changeType) {
        if (changeType == null || changeType.isBlank()) {
            return null;
        }
        if (changeType.contains("职务")) {
            return "职务变化";
        }
        if (changeType.contains("学历")) {
            return "学历变动";
        }
        if ("级别滚动".equals(changeType)) {
            return "级别滚动";
        }
        if (containsAny(changeType, "正常级别")) {
            return "晋升级别";
        }
        if (containsAny(changeType, "正常档次", "正常薪级")) {
            return "晋升档次";
        }
        if (changeType.contains("津补贴") || changeType.contains("补贴调标")) {
            return "津补贴调标";
        }
        if (changeType.contains("工资调标")) {
            return "工资调标";
        }
        if (changeType.contains("警衔")) {
            return "警衔变化";
        }
        if (changeType.contains("检察")) {
            return "检察变化";
        }
        if (changeType.contains("审判")) {
            return "审判变化";
        }
        if (changeType.contains("监察")) {
            return "监察变化";
        }
        if (changeType.contains("套改")) {
            return "套改";
        }
        if (changeType.contains("事业")) {
            return "事业岗位变动";
        }
        if (changeType.contains("转正")) {
            return "转正定级";
        }
        return null;
    }

    private WageProjectionState amountStateFromAuditStep(
            WageProjectionState projectionState,
            WageProjectionStepDetail step,
            PayrollHistorySnapshot latest) {
        String[] levelStepParts = parseLevelStepParts(step.levelStepDisplay());
        String positionCode = emptyToNull(step.positionCode()) != null
                ? step.positionCode()
                : projectionState.positionCode();
        String positionName = emptyToNull(step.positionName()) != null
                ? step.positionName()
                : projectionState.positionName();
        String level = emptyToNull(levelStepParts[0]) != null ? levelStepParts[0] : projectionState.level();
        String stepOrSalaryLevel = emptyToNull(levelStepParts[1]) != null
                ? levelStepParts[1]
                : projectionState.stepOrSalaryLevel();
        String gradeStepDifference = emptyToNull(levelStepParts[2]) != null
                ? levelStepParts[2]
                : projectionState.gradeStepDifferenceCount();
        WageProjectionState rebuilt = new WageProjectionState(
                positionCode,
                positionName,
                level,
                stepOrSalaryLevel,
                gradeStepDifference,
                projectionState.levelStartYear(),
                projectionState.stepStartYear(),
                baseSalarySource(positionCode, level),
                emptyToNull(step.salaryStandardYearMonth()) != null
                        ? step.salaryStandardYearMonth()
                        : projectionState.salaryStandardYearMonth(),
                emptyToNull(step.allowanceStandardYearMonth()) != null
                        ? step.allowanceStandardYearMonth()
                        : projectionState.allowanceStandardYearMonth(),
                projectionState.rankName(),
                projectionState.rankAllowanceStandardYearMonth(),
                projectionState.rankAllowance(),
                projectionState.rankAllowanceCategory());
        return bindStandardsForProjectionStepCapture(rebuilt, latest, step.period());
    }

    private static String[] parseLevelStepParts(String levelStepDisplay) {
        if (levelStepDisplay == null || levelStepDisplay.isBlank()) {
            return new String[] { null, null, "0" };
        }
        String[] segments = levelStepDisplay.trim().split("-");
        String level = segments.length > 0 ? segments[0].trim() : null;
        String step = segments.length > 1 ? segments[1].trim() : null;
        String gradeDifference = segments.length > 2 ? segments[2].trim() : "0";
        return new String[] { level, step, gradeDifference };
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
            String targetPeriod,
            String regularizationYearMonth,
            PayrollHistorySnapshot latest) {
        List<WageProjectionEvent> events = new ArrayList<>();
        String normalizedRegularization = normalizeYearMonth(regularizationYearMonth);
        boolean institutionPersonnel = isInstitutionPersonnel(latest);
        payrollRepository.findPositionChangesBetween(
                organizationCode,
                personCode,
                startPeriod,
                targetPeriod,
                WAGE_REFORM_POSITION_PREFIXES)
                .stream()
                .filter(position -> institutionPersonnel == isInstitutionPosition(position.positionCode()))
                .filter(position -> !isRegularizationAppointmentPosition(position, normalizedRegularization))
                .map(position -> WageProjectionEvent.position(nextMonth(position.startYearMonth()), position, organizationCode, personCode))
                .filter(event -> !event.period().isBlank())
                .filter(event -> event.period().compareTo(startPeriod) > 0 && event.period().compareTo(targetPeriod) <= 0)
                .forEach(events::add);
        if (!normalizedRegularization.isBlank()
                && normalizedRegularization.compareTo(startPeriod) > 0
                && normalizedRegularization.compareTo(targetPeriod) <= 0) {
            events.add(WageProjectionEvent.regularization(
                    normalizedRegularization, normalizedRegularization, organizationCode, personCode));
        }
        payrollRepository.findRankAllowanceChangesBetween(organizationCode, personCode, startPeriod, targetPeriod)
                .stream()
                .map(change -> WageProjectionEvent.rankChange(
                        nextMonth(normalizeYearMonth(change.startYearMonth())),
                        change,
                        organizationCode,
                        personCode))
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
        payrollRepository.findAllowanceStandardPeriodsBetween(organizationCode, startPeriod, targetPeriod)
                .stream()
                .filter(standardPeriod -> standardPeriod != null && !standardPeriod.isBlank())
                .map(standardPeriod -> WageProjectionEvent.allowanceStandard(standardPeriod, standardPeriod, organizationCode, personCode))
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
                .sorted(Comparator
                        .comparing(WageProjectionEvent::period)
                        .thenComparing(WageProjectionEvent::sortOrder)
                        .thenComparing(event -> wageProjectionEventTieBreakKey(event)))
                .toList();
    }

    private String wageProjectionEventTieBreakKey(WageProjectionEvent event) {
        if (event.position() != null) {
            return projectionPeriod(normalizeYearMonth(event.position().startYearMonth()));
        }
        if (event.rankChange() != null) {
            return projectionPeriod(normalizeYearMonth(event.rankChange().startYearMonth()));
        }
        if (event.educationChange() != null) {
            return projectionPeriod(normalizeYearMonth(event.educationChange().graduationDate()));
        }
        return "";
    }

    private WageProjectionState applyWageProjectionEvent(
            WageProjectionState state,
            WageProjectionEvent event,
            List<String> lines) {
        return applyWageProjectionEvent(state, event, lines, null);
    }

    private WageProjectionState applyWageProjectionEvent(
            WageProjectionState state,
            WageProjectionEvent event,
            List<String> lines,
            PayrollHistorySnapshot latest) {
        if (emptyToNull(event.regularizationYearMonth()) != null) {
            if (latest == null) {
                return state;
            }
            return applyRegularizationProjectionEvent(
                    state, latest, event.period(), event.regularizationYearMonth(), lines);
        }
        if (event.basicSalaryStandardYearMonth() != null) {
            return applyBasicSalaryStandardEvent(
                    state, event.basicSalaryStandardYearMonth(), event.organizationCode(), lines);
        }
        if (event.allowanceStandardYearMonth() != null) {
            return applyAllowanceStandardEvent(
                    state, event.allowanceStandardYearMonth(), event.organizationCode(), lines);
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
        return applyWageProjectionPositionChange(
                state, event.position(), event.organizationCode(), event.personCode(), lines, latest);
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
        WageProjectionState next = new WageProjectionState(
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
        appendProjectionLine(lines, state, next, period + " 学历变动：取得 " + emptyToDash(education.educationName())
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
        return next;
    }

    private WageProjectionState applyBasicSalaryStandardEvent(
            WageProjectionState state,
            String standardYearMonth,
            String organizationCode,
            List<String> lines) {
        if (normalizedEquals(standardYearMonth, state.salaryStandardYearMonth())) {
            return state;
        }
        if (!payrollRepository.hasBasicSalaryStandardForSource(standardYearMonth, resolvedBaseSalarySource(state))) {
            return state;
        }
        int positionSalary = projectedPositionSalary(state, standardYearMonth);
        int baseSalary = projectedBaseSalary(state, standardYearMonth);
        if (lines != null) {
            lines.add(standardYearMonth + " 工资调标：工资标准年月（tbnd）调整为 " + standardYearMonth
                    + "，职务工资 " + positionSalary
                    + "，" + baseSalaryLabel(resolvedBaseSalarySource(state)) + " " + baseSalary + "。");
        }
        return state.withSalaryStandard(standardYearMonth);
    }

    private WageProjectionState applyAllowanceStandardEvent(
            WageProjectionState state,
            String allowanceStandardYearMonth,
            String organizationCode,
            List<String> lines) {
        String effectiveAllowanceStandard = resolveAllowanceStandardYearMonth(
                allowanceStandardYearMonth,
                organizationCode,
                state.positionCode(),
                allowanceStandardYearMonth);
        if (normalizedEquals(effectiveAllowanceStandard, state.allowanceStandardYearMonth())) {
            return state;
        }
        WageProjectionState updated = state.withAllowanceStandard(effectiveAllowanceStandard);
        if (lines == null) {
            return updated;
        }
        if (!payrollRepository.hasAllowanceStandardForPosition(
                effectiveAllowanceStandard, organizationCode, state.positionCode())) {
            lines.add(allowanceStandardYearMonth + " 津补贴调标：津补贴标准年月（jbtbz）调整为 "
                    + effectiveAllowanceStandard + "（该年月无对应职务津补贴标准行）。");
            return updated;
        }
        BigDecimal performanceAllowance = projectedPerformanceAllowance(
                updated, organizationCode, effectiveAllowanceStandard);
        int subsidyAllowance = projectedSubsidyAllowance(
                updated, organizationCode, effectiveAllowanceStandard);
        lines.add(allowanceStandardYearMonth + " 津补贴调标：津补贴标准年月（jbtbz）调整为 " + effectiveAllowanceStandard
                + "，按职务 " + emptyToDash(state.positionCode()) + " 查 bz06_jbt："
                + dfbt2CaptionForPosition(state.positionCode()) + " "
                + performanceAllowance.stripTrailingZeros().toPlainString()
                + "，" + sdbtCaptionForPosition(state.positionCode()) + " " + subsidyAllowance + "。");
        return updated;
    }

    private boolean skipStandaloneAllowanceProjectionEvent(
            WageProjectionEvent event,
            Set<String> appliedAllowanceStandards) {
        return event.allowanceStandardYearMonth() != null
                && appliedAllowanceStandards.contains(event.period());
    }

    /** 任职/学历/警衔变化次月生效，若恰为当年 1 月须在年初晋档前试算；调标类仍留在晋档后。 */
    private static boolean isYearStartPrePromotionProjectionEvent(WageProjectionEvent event) {
        return event.position() != null
                || event.educationChange() != null
                || event.rankChange() != null;
    }

    private WageProjectionState applyCapturedWageProjectionChange(
            WageProjectionState state,
            WageProjectionEvent event,
            List<String> lines,
            PayrollHistorySnapshot latest,
            String organizationCode,
            List<String> allowanceStandardPeriods,
            Set<String> appliedAllowanceStandards) {
        if (event.allowanceStandardYearMonth() != null) {
            WageProjectionState next = applyAllowanceStandardEvent(
                    state, event.allowanceStandardYearMonth(), organizationCode, lines);
            appliedAllowanceStandards.add(event.period());
            return next;
        }
        WageProjectionState next = applyWageProjectionEvent(state, event, lines, latest);
        return applyMergedAllowanceStandardAtPeriodIfPresent(
                next,
                event.period(),
                organizationCode,
                lines,
                allowanceStandardPeriods,
                appliedAllowanceStandards);
    }

    private WageProjectionState applyMergedAllowanceStandardAtPeriodIfPresent(
            WageProjectionState state,
            String period,
            String organizationCode,
            List<String> lines,
            List<String> allowanceStandardPeriods,
            Set<String> appliedAllowanceStandards) {
        String normalizedPeriod = projectionPeriod(period);
        if (normalizedPeriod.compareTo(ALLOWANCE_MERGE_SAME_PERIOD_FROM) < 0
                || !allowanceStandardPeriods.contains(normalizedPeriod)
                || appliedAllowanceStandards.contains(normalizedPeriod)) {
            return state;
        }
        WageProjectionState next = applyAllowanceStandardEvent(state, normalizedPeriod, organizationCode, lines);
        if (!normalizedEquals(next.allowanceStandardYearMonth(), state.allowanceStandardYearMonth())) {
            appliedAllowanceStandards.add(normalizedPeriod);
        }
        return next;
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

    private static final String WAGE_REFORM_SALARY_STANDARD = "200607";

    /** 2014.10 及以后：同月已有其他工资变动时，津补贴调标并入该步；此前同月仍单独分步。 */
    private static final String ALLOWANCE_MERGE_SAME_PERIOD_FROM = "201410";

    private String initialSalaryStandardYearMonth(WageProjectionStart start) {
        String normalizedStart = projectionPeriod(start.period());
        if (WAGE_REFORM_SALARY_STANDARD.equals(normalizedStart)) {
            return WAGE_REFORM_SALARY_STANDARD;
        }
        String resolved = payrollRepository.latestPositionSalaryStandardAtOrBefore(normalizedStart);
        return emptyToNull(resolved) != null ? resolved : normalizedStart;
    }

    private String initialAllowanceStandardYearMonth(WageProjectionStart start, PayrollHistorySnapshot latest) {
        String normalizedStart = projectionPeriod(start.period());
        String resolved = payrollRepository.latestAllowanceStandardAtOrBefore(
                normalizedStart, latest.organizationCode(), start.positionCode());
        return emptyToNull(resolved) != null ? resolved : normalizedStart;
    }

    private WageProjectionState bindStandardsForProjectionPeriod(
            WageProjectionState state,
            PayrollHistorySnapshot latest,
            String period) {
        return clampProjectionStandardsToPeriod(state, latest, projectionPeriod(period));
    }

    private WageProjectionState clampProjectionStandardsToPeriod(
            WageProjectionState state,
            PayrollHistorySnapshot latest,
            String period) {
        WageProjectionState bound = clampProjectionSalaryStandard(state, period);
        String allowanceStandard = resolveAllowanceStandardYearMonth(
                period,
                latest.organizationCode(),
                state.positionCode(),
                state.allowanceStandardYearMonth());
        return emptyToNull(allowanceStandard) != null
                ? bound.withAllowanceStandard(allowanceStandard)
                : bound;
    }

    private WageProjectionState clampProjectionSalaryStandard(WageProjectionState state, String period) {
        String salaryStandard = emptyToNull(state.salaryStandardYearMonth());
        if (salaryStandard == null || salaryStandard.compareTo(period) > 0) {
            salaryStandard = payrollRepository.latestPositionSalaryStandardAtOrBefore(period);
        }
        return emptyToNull(salaryStandard) != null ? state.withSalaryStandard(salaryStandard) : state;
    }

    /** 分步明细：按时间线 jbtbz 计津补贴；标准年月须对应当前职务在 bz06_jbt 有 DFBT2 行。 */
    private WageProjectionState bindStandardsForProjectionStepCapture(
            WageProjectionState state,
            PayrollHistorySnapshot latest,
            String period) {
        String normalizedPeriod = projectionPeriod(period);
        WageProjectionState bound = clampProjectionSalaryStandard(state, normalizedPeriod);
        String allowanceStandard = resolveAllowanceStandardYearMonth(
                normalizedPeriod,
                latest.organizationCode(),
                state.positionCode(),
                state.allowanceStandardYearMonth());
        return emptyToNull(allowanceStandard) != null
                ? bound.withAllowanceStandard(allowanceStandard)
                : bound;
    }

    /**
     * 对齐 jbtbz.prg + dfbt2.prg：优先沿用时间线/记录上的 jbtbz（且当前职务有标准行），
     * 否则取该职务在调资年月及以前最近一条有效津补贴标准。
     */
    private String resolveAllowanceStandardYearMonth(
            String period,
            String organizationCode,
            String positionCode,
            String storedAllowanceStandardYearMonth) {
        String normalizedPeriod = projectionPeriod(period);
        String stored = emptyToNull(storedAllowanceStandardYearMonth);
        if (stored != null
                && payrollRepository.hasAllowanceStandardForPosition(stored, organizationCode, positionCode)) {
            return stored;
        }
        String withPositionRow = payrollRepository.latestAllowanceStandardWithPositionRowAtOrBefore(
                normalizedPeriod, organizationCode, positionCode);
        if (emptyToNull(withPositionRow) != null) {
            return withPositionRow;
        }
        String resolved = payrollRepository.latestAllowanceStandardAtOrBefore(
                normalizedPeriod, organizationCode, positionCode);
        if (emptyToNull(resolved) != null) {
            return resolved;
        }
        return stored;
    }

    private int projectedBaseSalary(WageProjectionState state, String standardYearMonth) {
        return projectedBaseSalary(state, null, standardYearMonth);
    }

    private int projectedBaseSalary(
            WageProjectionState state,
            PayrollHistorySnapshot latest,
            String standardYearMonth) {
        int probationSalary = latest == null
                ? 0
                : internSalaryAmount(state, latest, standardYearMonth);
        if (probationSalary > 0) {
            return probationSalary;
        }
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
            List<String> lines,
            PayrollHistorySnapshot latest) {
        String positionCode = position.positionCode();
        String positionName = position.positionName();
        String salaryStandardYearMonth = state.salaryStandardYearMonth();
        String appointmentPeriod = normalizeYearMonth(position.startYearMonth());
        String period = nextMonth(appointmentPeriod);
        if (latest != null && isInstitutionPosition(positionCode)) {
            return applyInstitutionProjectionPositionChange(
                    state, position, latest, organizationCode, personCode, lines, period, appointmentPeriod);
        }
        if (latest != null && isInstitutionPersonnel(latest) && !isInstitutionPosition(positionCode)) {
            lines.add(period + " 任职记录 " + formatYearMonth(appointmentPeriod) + " 为行政职务 "
                    + positionDisplay(positionCode, positionName) + "，事业人员推算忽略该条任职。");
            return state;
        }
        if (isLowerPositionLayer(state.positionCode(), positionCode)
                && payrollRepository.hasDemotionDisciplinaryRecord(organizationCode, personCode, appointmentPeriod)) {
            WageProjectionState demotionBase = disciplinaryDemotionBaseState(state, positionCode);
            WageProjectionState demoted = projectDisciplinaryDemotion(demotionBase, positionCode, positionName, period, salaryStandardYearMonth);
            int layers = positionLayer(positionCode) - positionLayer(state.positionCode());
            appendProjectionLine(lines, state, demoted, period + " 撤职处分：采用任职记录 " + formatYearMonth(appointmentPeriod) + " 职务 " + positionDisplay(positionCode, positionName)
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
                WageProjectionState next = new WageProjectionState(
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
                appendProjectionLine(lines, state, next, period + " 警员套改：采用任职记录 " + formatYearMonth(appointmentPeriod) + " 职务 " + positionDisplay(positionCode, positionName)
                        + "，按警员套改由 " + levelStepDisplay(state.baseSalarySource(), state.level(), state.stepOrSalaryLevel())
                        + " 试算为 " + levelStepDisplay("GRADE", result.promotedLevel(), result.promotedStep()) + "。");
                return next;
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
            appendProjectionLine(lines, state, promoted, period + " 警员回到其他类：采用任职记录 " + formatYearMonth(appointmentPeriod) + " 职务 " + positionDisplay(positionCode, positionName)
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
            appendProjectionLine(lines, state, promoted, period + " 职务变化：采用任职记录 " + formatYearMonth(appointmentPeriod) + " 职务 " + positionDisplay(positionCode, positionName)
                    + "，按职务晋升政策由 " + levelStepDisplay(state.baseSalarySource(), state.level(), state.stepOrSalaryLevel())
                    + " 试算为 " + levelStepDisplay("GRADE", promoted.level(), promoted.stepOrSalaryLevel()) + "。");
            return promoted;
        }
        lines.add(period + " 职务变化：任职记录 " + formatYearMonth(appointmentPeriod) + " 为 " + positionDisplay(positionCode, positionName)
                + "，当前推算暂只自动试算公务员级别工资职务变化。");
        return state;
    }

    private WageProjectionState applyInstitutionProjectionPositionChange(
            WageProjectionState state,
            PositionChangeCandidate position,
            PayrollHistorySnapshot latest,
            String organizationCode,
            String personCode,
            List<String> lines,
            String period,
            String appointmentPeriod) {
        InstitutionPositionChangeResult result = institutionPositionChangeResult(
                latest, position, state.stepOrSalaryLevel());
        if (!result.eligible()) {
            lines.add(period + " 事业岗位变动：任职记录 " + formatYearMonth(appointmentPeriod) + " 为 "
                    + positionDisplay(position.positionCode(), position.positionName()) + "，" + result.note());
            return state;
        }
        WageProjectionState next = new WageProjectionState(
                position.positionCode(),
                position.positionName(),
                "",
                result.promotedSalaryLevel(),
                "0",
                state.levelStartYear(),
                result.nextStepAssessmentStartYear(),
                "SALARY_LEVEL",
                state.salaryStandardYearMonth(),
                state.allowanceStandardYearMonth(),
                state.rankName(),
                state.rankAllowanceStandardYearMonth(),
                state.rankAllowance(),
                state.rankAllowanceCategory());
        appendProjectionLine(lines, state, next, period + " 事业岗位变动：采用任职记录 "
                + formatYearMonth(appointmentPeriod) + " 职务 " + positionDisplay(position.positionCode(), position.positionName())
                + "，" + result.note());
        return next;
    }

    private boolean isRegularizationAppointmentPosition(
            PositionChangeCandidate position,
            String regularizationYearMonth) {
        if (position == null || regularizationYearMonth == null || regularizationYearMonth.isBlank()) {
            return false;
        }
        return regularizationYearMonth.equals(normalizeYearMonth(position.startYearMonth()));
    }

    private WageProjectionState applyRegularizationProjectionEvent(
            WageProjectionState state,
            PayrollHistorySnapshot latest,
            String period,
            String regularizationYearMonth,
            List<String> lines) {
        String regularization = normalizeYearMonth(regularizationYearMonth);
        RegularizationSalaryPosition resolved = resolveRegularizationSalaryPosition(
                latest.organizationCode(),
                latest.personCode(),
                regularization,
                state.positionCode(),
                regularization);
        EducationRegularizationStandard standard = resolved.standard();
        if (standard == null) {
            lines.add(period + " 转正定级：转正时间 " + formatYearMonth(regularization)
                    + "，未能按学历转正定级标准确定职务级别。");
            return state;
        }
        String positionCode = resolved.salaryPositionCode();
        String positionName = resolved.salaryPositionName();
        boolean institution = resolved.institutionRegularization();
        String level = institution ? "" : standard.gradeLevel();
        String step = standard.gradeStep();
        String regYear = yearOf(regularization) > 0 ? String.valueOf(yearOf(regularization)) : state.levelStartYear();
        String line;
        PositionChangeCandidate appointed = resolved.appointedPosition();
        if (resolved.salaryPositionFromStandard()) {
            line = period + " 转正定级：转正时间 " + formatYearMonth(regularization)
                    + "，未找到转正定级任职记录，按转正定级标准确认执行工资职务 "
                    + positionDisplay(positionCode, positionName)
                    + "，" + levelStepDisplay(baseSalarySource(positionCode, level), level, step) + "。";
        } else if (institution && appointed != null) {
            line = period + " 转正定级：转正时间 " + formatYearMonth(regularization)
                    + "，采用转正任职 " + positionDisplay(appointed.positionCode(), appointed.positionName())
                    + "，薪级 " + step + " 级。";
        } else if (appointed != null) {
            line = period + " 转正定级：转正时间 " + formatYearMonth(regularization)
                    + "，采用转正任职 " + positionDisplay(appointed.positionCode(), appointed.positionName())
                    + "，" + levelStepDisplay("GRADE", level, step) + "。";
        } else {
            line = period + " 转正定级：转正时间 " + formatYearMonth(regularization)
                    + "，按转正定级标准确定起点 "
                    + positionDisplay(positionCode, positionName)
                    + "，" + levelStepDisplay(baseSalarySource(positionCode, level), level, step) + "。";
        }
        WageProjectionState next = new WageProjectionState(
                positionCode,
                positionName,
                level,
                step,
                "0",
                regYear,
                regYear,
                baseSalarySource(positionCode, level),
                state.salaryStandardYearMonth(),
                state.allowanceStandardYearMonth(),
                state.rankName(),
                state.rankAllowanceStandardYearMonth(),
                state.rankAllowance(),
                state.rankAllowanceCategory());
        appendProjectionLine(lines, state, next, line);
        return next;
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

    private WageProjectionState createInitialProjectionState(
            PayrollHistorySnapshot latest,
            WageProjectionStart start,
            RankAllowanceState initialRankAllowance) {
        return new WageProjectionState(
                start.positionCode(),
                start.positionName(),
                start.level(),
                start.stepOrSalaryLevel(),
                "0",
                start.levelStartYear(),
                start.stepStartYear(),
                baseSalarySource(start.positionCode(), start.level()),
                initialSalaryStandardYearMonth(start),
                initialAllowanceStandardYearMonth(start, latest),
                initialRankAllowance.rankName(),
                initialRankAllowance.standardYearMonth(),
                initialRankAllowance.amount(),
                initialRankAllowance.category());
    }

    private WageProjectionState applyReformLevelRollingState(
            WageProjectionState state,
            WageProjectionStart start,
            PayrollHistorySnapshot latest,
            int year,
            List<String> lines) {
        String previousLevel = state.level();
        String previousStep = state.stepOrSalaryLevel();
        int currentSalary = gradeSalaryAmount(state, previousLevel, previousStep, state.salaryStandardYearMonth());
        String nextLevel = cappedPromotedLevel(
                state.positionCode(),
                String.valueOf(Math.max(1, payrollRepository.intValue(previousLevel) - 1)));
        String nextStep = firstHigherGradeStep(nextLevel, currentSalary, state.salaryStandardYearMonth());
        String nextStepStartYear = gradeIncreaseExceedsStepDifference(previousLevel, previousStep, nextLevel, state.salaryStandardYearMonth())
                ? String.valueOf(year)
                : state.stepStartYear();
        WageProjectionState next = projectionWithLevelStep(state, nextLevel, nextStep, String.valueOf(year), nextStepStartYear);
        if (lines != null) {
            int reformYears = start.wageReformYears() + (year - 2006);
            ReformRollingPositionMode positionMode = resolveReformRollingPositionMode(latest, start, state.positionCode(), year);
            String ruleNote = resolveReformLevelRollingRulePath(
                    state,
                    start,
                    latest,
                    year,
                    reformYears,
                    payrollRepository.intValue(previousLevel),
                    positionMode)
                    .map(this::reformLevelRollingRuleNote)
                    .orElse("");
            appendProjectionLine(lines, state, next, year + " 年：2007-2010 套改后级别滚动"
                    + (ruleNote.isBlank() ? "" : "（" + ruleNote + "）")
                    + "，上一年度考核称职及以上且达到套改表规定年限，晋升级别 "
                    + levelStepDisplay(resolvedBaseSalarySource(state), previousLevel, previousStep) + " -> "
                    + levelStepDisplay(baseSalarySource(state.positionCode(), nextLevel), nextLevel, nextStep) + "。");
        }
        return next;
    }

    private WageProjectionState advanceSimulatedNormalLevelPromotion(
            WageProjectionState state,
            WageProjectionStart start,
            PayrollHistorySnapshot latest,
            int year,
            int qualifiedLevel,
            List<String> lines) {
        String previousLevel = state.level();
        if (atHighestPositionLevel(state.positionCode(), previousLevel)) {
            return promoteCivilServantGradeStep(
                    state,
                    year,
                    lines,
                    "累计 " + qualifiedLevel + " 年考核合格，已达到所任职务最高级别，按级别晋升口径级别不变",
                    true);
        }
        String previousStep = state.stepOrSalaryLevel();
        int currentSalary = payrollRepository.gradeSalary(previousLevel, previousStep, state.salaryStandardYearMonth());
        String nextLevel = String.valueOf(Math.max(1, payrollRepository.intValue(previousLevel) - 1));
        String nextStep = firstHigherGradeStep(nextLevel, currentSalary, state.salaryStandardYearMonth());
        String nextStepStartYear = gradeIncreaseExceedsStepDifference(previousLevel, previousStep, nextLevel, state.salaryStandardYearMonth())
                ? String.valueOf(year)
                : state.stepStartYear();
        WageProjectionState next = projectionWithLevelStep(state, nextLevel, nextStep, String.valueOf(year), nextStepStartYear);
        appendProjectionLine(lines, state, next, year + " 年：累计 " + qualifiedLevel + " 年考核合格，晋升级别 "
                + levelStepDisplay(state.baseSalarySource(), previousLevel, previousStep) + " -> "
                + levelStepDisplay(state.baseSalarySource(), nextLevel, nextStep) + "。");
        return next;
    }

    private record ReformRollingSimulation(OptionalInt pendingYear, WageProjectionState stateBeforePendingRolling) {
    }

    private Set<Integer> appliedReformLevelRollingYears(PayrollHistorySnapshot history) {
        return payrollRepository.findHistoryChain(history.organizationCode(), history.personCode())
                .stream()
                .filter(record -> "级别滚动".equals(record.calculationType()))
                .map(record -> yearOf(record.calculationYear()))
                .collect(Collectors.toCollection(java.util.TreeSet::new));
    }

    private ReformRollingSimulation simulateReformLevelRolling(
            PayrollHistorySnapshot history,
            WageProjectionStart start,
            int limitYear,
            Set<Integer> appliedRollingYears,
            int payrollPeriod) {
        if (!start.eligible() || start.wageReformYears() <= 0 || limitYear < 2007) {
            return new ReformRollingSimulation(OptionalInt.empty(), null);
        }
        WageProjectionState state = createInitialProjectionState(
                history,
                start,
                initialRankAllowanceState(history, start.period()));
        String endPeriod = String.format("%04d12", limitYear);
        String regularizationYearMonth = payrollRepository.findRegularizationYearMonth(
                history.organizationCode(), history.personCode());
        List<WageProjectionEvent> projectionEvents = wageProjectionEvents(
                history.organizationCode(),
                history.personCode(),
                start.period(),
                endPeriod,
                regularizationYearMonth,
                history);
        List<String> ignoredLines = new ArrayList<>();
        int eventIndex = 0;
        int baseYear = yearOf(start.period());
        for (int year = Math.max(2007, baseYear + 1); year <= limitYear; year++) {
            String yearStart = String.format("%04d01", year);
            while (eventIndex < projectionEvents.size()
                    && projectionEvents.get(eventIndex).period().compareTo(yearStart) < 0) {
                state = applyWageProjectionEvent(state, projectionEvents.get(eventIndex), ignoredLines, history);
                eventIndex++;
            }
            if (supportsGradePromotion(state) && payrollRepository.intValue(state.level()) > 1) {
                int levelStart = assessmentStartYear(state.levelStartYear(), start.positionStartYearMonth(), state.positionCode());
                int stepStart = assessmentStartYear(state.stepStartYear(), start.positionStartYearMonth(), state.positionCode());
                int qualifiedLevel = payrollRepository.countQualifiedAssessmentYears(
                        history.organizationCode(), history.personCode(), levelStart, year - 1);
                int qualifiedStep = payrollRepository.countQualifiedAssessmentYears(
                        history.organizationCode(), history.personCode(), stepStart, year - 1);
                boolean specialLevelPromotionDue = specialWageReformLevelPromotionDue(state, start, history, year);
                if (specialLevelPromotionDue && payrollPeriod >= year * 100 + 1 && !appliedRollingYears.contains(year)) {
                    return new ReformRollingSimulation(OptionalInt.of(year), state);
                }
                if (specialLevelPromotionDue && appliedRollingYears.contains(year)) {
                    state = applyReformLevelRollingState(state, start, history, year, null);
                } else if (!specialLevelPromotionDue && qualifiedLevel >= 5) {
                    state = advanceSimulatedNormalLevelPromotion(state, start, history, year, qualifiedLevel, null);
                }
                if (qualifiedStep >= 2) {
                    state = promoteCivilServantGradeStep(
                            state,
                            year,
                            null,
                            "累计 " + qualifiedStep + " 年考核合格");
                }
            }
            String nextYearStart = String.format("%04d01", year + 1);
            while (eventIndex < projectionEvents.size()
                    && projectionEvents.get(eventIndex).period().compareTo(nextYearStart) < 0) {
                state = applyWageProjectionEvent(state, projectionEvents.get(eventIndex), ignoredLines, history);
                eventIndex++;
            }
        }
        return new ReformRollingSimulation(OptionalInt.empty(), state);
    }

    private enum ReformRollingPositionMode {
        UNCHANGED,
        SINGLE_LEVEL_PROMOTION,
        UNSUPPORTED
    }

    private ReformRollingPositionMode reformRollingPositionMode(String reformPositionCode, String currentPositionCode) {
        if (reformPositionCode == null || reformPositionCode.isBlank()
                || currentPositionCode == null || currentPositionCode.isBlank()) {
            return ReformRollingPositionMode.UNSUPPORTED;
        }
        int reformLayer = positionLayer(reformPositionCode);
        int currentLayer = positionLayer(currentPositionCode);
        if (reformLayer <= 0 || currentLayer <= 0) {
            return ReformRollingPositionMode.UNSUPPORTED;
        }
        if (reformLayer == currentLayer) {
            return ReformRollingPositionMode.UNCHANGED;
        }
        if (reformLayer - currentLayer == 1) {
            return ReformRollingPositionMode.SINGLE_LEVEL_PROMOTION;
        }
        return ReformRollingPositionMode.UNSUPPORTED;
    }

    private ReformRollingPositionMode resolveReformRollingPositionMode(
            PayrollHistorySnapshot latest,
            WageProjectionStart start,
            String replayPositionCode,
            int year) {
        String reformPositionCode = payrollRepository
                .findPositionAtOrBefore(latest.organizationCode(), latest.personCode(), "200607")
                .map(PositionChangeCandidate::positionCode)
                .orElse(start.positionCode());
        String comparisonPositionCode = payrollRepository
                .findPositionAtOrBefore(
                        latest.organizationCode(),
                        latest.personCode(),
                        String.format("%04d12", year - 1))
                .map(PositionChangeCandidate::positionCode)
                .orElse(replayPositionCode);
        return reformRollingPositionMode(reformPositionCode, comparisonPositionCode);
    }

    private PositionChangeCandidate reformAppointmentAt200607(PayrollHistorySnapshot latest, WageProjectionStart start) {
        return payrollRepository
                .findPositionAtOrBefore(latest.organizationCode(), latest.personCode(), "200607")
                .orElse(new PositionChangeCandidate(
                        start.positionCode(),
                        start.positionName(),
                        start.positionStartYearMonth()));
    }

    private enum ReformLevelRollingRulePath {
        CURRENT_POSITION_PREVIOUS_LEVEL,
        LOWER_POSITION_REFORM_LEVEL,
        ORIGINAL_POSITION_AFTER_SINGLE_PROMOTION
    }

    private Optional<ReformLevelRollingRulePath> resolveReformLevelRollingRulePath(
            WageProjectionState state,
            WageProjectionStart start,
            PayrollHistorySnapshot latest,
            int year,
            int reformYears,
            int currentLevel,
            ReformRollingPositionMode mode) {
        return switch (mode) {
            case UNCHANGED -> {
                String currentPositionStart = payrollRepository
                        .findPositionAtOrBefore(
                                latest.organizationCode(),
                                latest.personCode(),
                                String.format("%04d12", year - 1))
                        .map(PositionChangeCandidate::startYearMonth)
                        .orElse(start.positionStartYearMonth());
                int currentAppointmentYears = reformLevelRollingAppointmentYears(currentPositionStart, year);
                if (reformLevelRollingTableDue(state.positionCode(), currentAppointmentYears, reformYears, currentLevel)) {
                    yield Optional.of(ReformLevelRollingRulePath.CURRENT_POSITION_PREVIOUS_LEVEL);
                }
                String regularization = normalizeYearMonth(payrollRepository.findRegularizationYearMonth(
                        latest.organizationCode(), latest.personCode()));
                List<WageReformPosition> positions = resolveWageReformPositionsForSelection(latest, regularization);
                if (positions.size() < 2) {
                    yield Optional.empty();
                }
                WageReformPosition currentPosition = positions.getFirst();
                WageReformPosition lowerPosition = positions.get(1);
                int mergedLowerAppointmentYears = wageReformAppointmentYears(lowerPosition)
                        + wageReformAppointmentYears(currentPosition)
                        + (year - 2006);
                if (reformLevelRollingTableDueForReformLevel(
                        lowerPosition.positionCode(),
                        mergedLowerAppointmentYears,
                        reformYears,
                        start.level(),
                        currentLevel)) {
                    yield Optional.of(ReformLevelRollingRulePath.LOWER_POSITION_REFORM_LEVEL);
                }
                yield Optional.empty();
            }
            case SINGLE_LEVEL_PROMOTION -> reformLevelRollingDueWhenSinglePromotion(
                    start, latest, year, reformYears, currentLevel)
                    ? Optional.of(ReformLevelRollingRulePath.ORIGINAL_POSITION_AFTER_SINGLE_PROMOTION)
                    : Optional.empty();
            case UNSUPPORTED -> Optional.empty();
        };
    }

    private String reformLevelRollingRuleNote(ReformLevelRollingRulePath path) {
        return switch (path) {
            case CURRENT_POSITION_PREVIOUS_LEVEL -> "职务层次未变，按现任职务达到上一级别规定年限";
            case LOWER_POSITION_REFORM_LEVEL -> "职务层次未变，按原任低一职务达到套改确定级别规定年限";
            case ORIGINAL_POSITION_AFTER_SINGLE_PROMOTION -> "晋升职务只晋升一个级别，按原任职务达到晋升后级别规定年限";
        };
    }

    private int reformLevelRollingAppointmentYears(String positionStartYearMonth, int year) {
        return Math.max(1, 2006 - yearOf(positionStartYearMonth) + 1) + (year - 2006);
    }

    private boolean reformLevelRollingTableDue(
            String positionCode,
            int appointmentYears,
            int reformYears,
            int currentLevel) {
        if (currentLevel <= 1) {
            return false;
        }
        return lookupWageReformStandard(positionCode, appointmentYears, reformYears)
                .map(standard -> payrollRepository.intValue(standard.convertedLevel()) < currentLevel)
                .orElse(false);
    }

    private boolean reformLevelRollingTableDueForReformLevel(
            String positionCode,
            int appointmentYears,
            int reformYears,
            String reformDeterminedLevel,
            int currentLevel) {
        if (currentLevel <= 1) {
            return false;
        }
        int reformLevel = payrollRepository.intValue(reformDeterminedLevel);
        if (reformLevel <= 0) {
            return false;
        }
        return lookupWageReformStandard(positionCode, appointmentYears, reformYears)
                .map(standard -> {
                    int tableLevel = payrollRepository.intValue(standard.convertedLevel());
                    return tableLevel <= reformLevel && tableLevel < currentLevel;
                })
                .orElse(false);
    }

    private boolean reformLevelRollingDueWhenUnchanged(
            WageProjectionState state,
            WageProjectionStart start,
            PayrollHistorySnapshot latest,
            int year,
            int reformYears,
            int currentLevel) {
        return resolveReformLevelRollingRulePath(
                state, start, latest, year, reformYears, currentLevel, ReformRollingPositionMode.UNCHANGED)
                .isPresent();
    }

    private boolean reformLevelRollingDueWhenSinglePromotion(
            WageProjectionStart start,
            PayrollHistorySnapshot latest,
            int year,
            int reformYears,
            int currentLevel) {
        int reformLevel = payrollRepository.intValue(start.level());
        if (currentLevel <= 0 || reformLevel <= 0 || currentLevel >= reformLevel) {
            return false;
        }
        PositionChangeCandidate reformAppointment = reformAppointmentAt200607(latest, start);
        int appointmentYears = reformLevelRollingAppointmentYears(reformAppointment.startYearMonth(), year);
        return reformLevelRollingTableDueAfterSingleLayerPromotion(
                reformAppointment.positionCode(), appointmentYears, reformYears, currentLevel);
    }

    /**
     * 套改后只晋升一个职务层次：按 2006.07 原任职务查表，达到晋升后现任级别的规定年限即可滚动
     * （表列级别等于现任级别时也应认定期满，不同于职务未变路径须表列级别严格低于现任级别）。
     */
    private boolean reformLevelRollingTableDueAfterSingleLayerPromotion(
            String positionCode,
            int appointmentYears,
            int reformYears,
            int currentLevel) {
        if (currentLevel <= 1) {
            return false;
        }
        return lookupWageReformStandard(positionCode, appointmentYears, reformYears)
                .map(standard -> {
                    int tableLevel = payrollRepository.intValue(standard.convertedLevel());
                    return tableLevel > 0 && tableLevel <= currentLevel;
                })
                .orElse(false);
    }

    private boolean specialWageReformLevelPromotionDue(
            WageProjectionState state,
            WageProjectionStart start,
            PayrollHistorySnapshot latest,
            int year) {
        if (year < 2007 || year > 2010 || start.wageReformYears() <= 0 || yearOf(state.levelStartYear()) > 2006) {
            return false;
        }
        if (payrollRepository.countQualifiedAssessmentYears(latest.organizationCode(), latest.personCode(), year - 1, year - 1) <= 0) {
            return false;
        }
        int reformYears = start.wageReformYears() + (year - 2006);
        int currentLevel = payrollRepository.intValue(state.level());
        if (currentLevel <= 1) {
            return false;
        }
        ReformRollingPositionMode positionMode = resolveReformRollingPositionMode(latest, start, state.positionCode(), year);
        return resolveReformLevelRollingRulePath(
                state, start, latest, year, reformYears, currentLevel, positionMode)
                .isPresent();
    }

    private OptionalInt findPendingReformLevelRollingYear(
            PayrollHistorySnapshot history,
            WageProjectionStart start) {
        int limitYear = Math.min(2010, yearOf(history.calculationYear()));
        int payrollPeriod = payrollRepository.intValue(normalizeYearMonth(history.calculationYear() + history.calculationMonth()));
        return simulateReformLevelRolling(
                history,
                start,
                limitYear,
                appliedReformLevelRollingYears(history),
                payrollPeriod).pendingYear();
    }

    private WageProjectionState projectionStateFromHistory(PayrollHistorySnapshot history) {
        return new WageProjectionState(
                history.positionCode(),
                history.positionName(),
                history.gradeSalaryLevel(),
                history.positionSalaryGrade(),
                history.gradeSalaryStep() == null ? "0" : history.gradeSalaryStep(),
                history.levelAssessmentStartYear(),
                history.stepAssessmentStartYear(),
                baseSalarySource(history.positionCode(), history.gradeSalaryLevel()),
                history.salaryStandardYearMonth(),
                history.allowanceStandardYearMonth(),
                history.rankName(),
                history.rankAllowanceStandardYearMonth(),
                history.storedRankAllowance(),
                history.postAllowanceCategory());
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

    private String formatAssessmentYearChanges(WageProjectionState before, WageProjectionState after) {
        if (before == null || after == null) {
            return "";
        }
        List<String> parts = new ArrayList<>(2);
        if (!assessmentYearsEqual(before.levelStartYear(), after.levelStartYear())) {
            parts.add("级别考核起算年（xckhndjb）更新为 " + displayAssessmentYear(after.levelStartYear()));
        }
        if (!assessmentYearsEqual(before.stepStartYear(), after.stepStartYear())) {
            parts.add("档次考核起算年（xckhndzw）更新为 " + displayAssessmentYear(after.stepStartYear()));
        }
        return parts.isEmpty() ? "" : "，" + String.join("，", parts);
    }

    private boolean assessmentYearsEqual(String left, String right) {
        return Objects.equals(displayAssessmentYear(left), displayAssessmentYear(right));
    }

    private String displayAssessmentYear(String year) {
        if (year == null || year.isBlank()) {
            return "-";
        }
        return year.trim();
    }

    private void appendProjectionLine(
            List<String> lines,
            WageProjectionState before,
            WageProjectionState after,
            String message) {
        if (lines == null) {
            return;
        }
        lines.add(message + formatAssessmentYearChanges(before, after));
    }

    private WageProjectionState applyProjectionEventWithCapture(
            List<WageProjectionStepDetail> stepDetails,
            PayrollHistorySnapshot latest,
            List<String> lines,
            String period,
            WageProjectionState state,
            java.util.function.UnaryOperator<WageProjectionState> action) {
        if (stepDetails == null) {
            return action.apply(state);
        }
        int lineStart = lines.size();
        WageProjectionState next = action.apply(state);
        if (next == state && lines.size() == lineStart) {
            return next;
        }
        String description = lines.size() > lineStart
                ? String.join(" ", lines.subList(lineStart, lines.size()))
                : "工资状态更新";
        captureProjectionStep(stepDetails, latest, period, description, next);
        return next;
    }

    private void captureProjectionStep(
            List<WageProjectionStepDetail> stepDetails,
            PayrollHistorySnapshot latest,
            String period,
            String description,
            WageProjectionState state) {
        if (stepDetails == null) {
            return;
        }
        WageProjectionState amountState = bindStandardsForProjectionStepCapture(state, latest, period);
        List<PayrollPreviewComponent> components = projectionStepComponents(latest, amountState, period);
        WageProjectionStepDetail detail = new WageProjectionStepDetail(
                period,
                description,
                amountState.positionCode(),
                amountState.positionName(),
                levelStepDisplay(
                        resolvedBaseSalarySource(amountState),
                        amountState.level(),
                        amountState.stepOrSalaryLevel(),
                        amountState.gradeStepDifferenceCount()),
                amountState.salaryStandardYearMonth(),
                amountState.allowanceStandardYearMonth(),
                components,
                projectionStepTotal(components));
        stepDetails.add(detail);
    }

    private List<PayrollPreviewComponent> projectionStepComponents(
            PayrollHistorySnapshot latest,
            WageProjectionState state) {
        return projectionStepComponents(latest, state, state.salaryStandardYearMonth());
    }

    private List<PayrollPreviewComponent> projectionStepComponents(
            PayrollHistorySnapshot latest,
            WageProjectionState state,
            String lookupPeriod) {
        BasicPayrollCalculation basic = basicCalculation(state, latest, lookupPeriod);
        AllowanceCalculation allowance = allowanceCalculation(state, latest);
        AdditionalPayrollCalculation additional = additionalCalculation(state, latest);
        Integer teachingAllowance = teachingAllowance(latest);
        Integer salaryIncrease = salaryIncrease(latest, basic);
        String gradeCaption = internSalaryAmount(state, latest, lookupPeriod) > 0
                ? "试用期工资"
                : baseSalaryLabel(resolvedBaseSalarySource(state));
        return List.of(
                preview("ZWGZSE2", "职务工资", basic.positionSalary(), "AUTO"),
                preview("JBGZSE2", gradeCaption, basic.selectedBaseSalary(), "AUTO"),
                preview("JSDJGZ2", "技术等级工资", basic.technicalGradeSalary(), "AUTO"),
                preview("DFBT2", dfbt2Caption(latest), allowance.performanceAllowance(), "AUTO"),
                preview("SDBT", sdbtCaption(latest), allowance.subsidyAllowance(), "AUTO"),
                preview("BLFB2", "保留福补", allowance.retainedAllowance(), "AUTO"),
                preview("NJBT", "农教补贴", allowance.yearAllowance(), "AUTO"),
                preview("JXJT", RANK_ALLOWANCE_COMPONENT_CAPTION, additional.rankAllowance(), "AUTO"),
                preview("FDGZ2", "浮动工资", additional.floatingSalary(), "AUTO"),
                preview("JJJY2", "奖金结余", additional.bonusBalance(), "AUTO_OR_PRESERVE"),
                preview("TGBLBF", "套改/特岗保留", additional.retainedSpecialPostAllowance(), "AUTO_OR_PRESERVE"),
                preview("JHLJT", "教护龄津贴", teachingAllowance, "AUTO"),
                preview("JSFSZWTG2", "提高工资", salaryIncrease, "AUTO"));
    }

    private BigDecimal projectionStepTotal(List<PayrollPreviewComponent> components) {
        return components.stream()
                .map(PayrollPreviewComponent::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
            WageProjectionState next = projectionWithStep(state, promotedStep, String.valueOf(year));
            appendProjectionLine(lines, state, next, year + " 年：" + reasonPrefix + "，晋升档次到 "
                    + levelStepDisplay(state.baseSalarySource(), state.level(), promotedStep) + "。");
            return next;
        }
        String previousStep = state.stepOrSalaryLevel();
        int currentStep = payrollRepository.intValue(previousStep);
        int highestStep = payrollRepository.highestGradeStepForLevel(state.level());
        if (currentStep >= highestStep) {
            String promotedDifference = String.valueOf(normalizedGradeStepDifferenceCount(state) + 1);
            WageProjectionState next = projectionWithStepDifference(state, promotedDifference, String.valueOf(year));
            appendProjectionLine(lines, state, next, year + " 年：" + reasonPrefix + "，已达到本级别最高档次，增加档差工资，档差个数 "
                    + promotedDifference + "（"
                    + levelStepDisplay(state.baseSalarySource(), state.level(), previousStep, promotedDifference) + "）。");
            return next;
        }
        String promotedStep = String.valueOf(currentStep + 1);
        if (resetLevelAssessmentYear) {
            WageProjectionState next = new WageProjectionState(
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
            appendProjectionLine(lines, state, next, year + " 年：" + reasonPrefix + "，晋升档次到 "
                    + levelStepDisplay(state.baseSalarySource(), state.level(), promotedStep) + "。");
            return next;
        }
        WageProjectionState next = projectionWithStep(state, promotedStep, String.valueOf(year));
        appendProjectionLine(lines, state, next, year + " 年：" + reasonPrefix + "，晋升档次到 "
                + levelStepDisplay(state.baseSalarySource(), state.level(), promotedStep) + "。");
        return next;
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
            String allowanceStandardYearMonth,
            String rankStandardYearMonth,
            EducationPromotionSource educationChange,
            String regularizationYearMonth,
            String organizationCode,
            String personCode,
            int sortOrder) {

        /**
         * 同月事件顺序：转正定级 → 职务、学历、警衔变化（均任职/授衔次月生效）→ 工资/津补贴调标（当月）→ 警衔津贴标准调标（当月）。
         */
        static final int SORT_REGULARIZATION = -1;
        static final int SORT_POSITION = 0;
        static final int SORT_EDUCATION = 1;
        static final int SORT_BASIC_SALARY_STANDARD = 2;
        static final int SORT_ALLOWANCE_STANDARD = 3;
        static final int SORT_RANK_ALLOWANCE_STANDARD = 4;
        static final int SORT_RANK_ALLOWANCE_CHANGE = 5;

        static WageProjectionEvent position(String period, PositionChangeCandidate position, String organizationCode, String personCode) {
            return new WageProjectionEvent(
                    period, position, null, null, null, null, null, null, organizationCode, personCode, SORT_POSITION);
        }

        static WageProjectionEvent regularization(
                String period,
                String regularizationYearMonth,
                String organizationCode,
                String personCode) {
            return new WageProjectionEvent(
                    period, null, null, null, null, null, null, regularizationYearMonth, organizationCode, personCode, SORT_REGULARIZATION);
        }

        static WageProjectionEvent rankStandard(String period, String standardYearMonth, String organizationCode, String personCode) {
            return new WageProjectionEvent(
                    period, null, null, null, null, standardYearMonth, null, null, organizationCode, personCode, SORT_RANK_ALLOWANCE_STANDARD);
        }

        static WageProjectionEvent rankChange(String period, RankAllowanceChange change, String organizationCode, String personCode) {
            return new WageProjectionEvent(
                    period, null, change, null, null, null, null, null, organizationCode, personCode, SORT_RANK_ALLOWANCE_CHANGE);
        }

        static WageProjectionEvent basicSalaryStandard(
                String period,
                String standardYearMonth,
                String organizationCode,
                String personCode) {
            return new WageProjectionEvent(
                    period, null, null, standardYearMonth, null, null, null, null, organizationCode, personCode, SORT_BASIC_SALARY_STANDARD);
        }

        static WageProjectionEvent allowanceStandard(
                String period,
                String standardYearMonth,
                String organizationCode,
                String personCode) {
            return new WageProjectionEvent(
                    period, null, null, null, standardYearMonth, null, null, null, organizationCode, personCode, SORT_ALLOWANCE_STANDARD);
        }

        static WageProjectionEvent education(
                String period,
                EducationPromotionSource education,
                String organizationCode,
                String personCode) {
            return new WageProjectionEvent(
                    period, null, null, null, null, null, education, null, organizationCode, personCode, SORT_EDUCATION);
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
        return buildPayrollChangeComparison(payrollHistoryId, afterValues, beforeValues);
    }

    public List<PayrollChangeComparison> payrollChangeComparisons(List<String> payrollHistoryIds) {
        List<String> ids = payrollHistoryIds == null
                ? List.of()
                : payrollHistoryIds.stream()
                        .filter(id -> id != null && !id.isBlank())
                        .map(String::trim)
                        .distinct()
                        .toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<String, Map<String, Object>> afterById = payrollRepository.findHistoryValuesByIds(ids);
        Map<String, Map<String, Object>> beforeById = payrollRepository.findPredecessorHistoryValuesByIds(ids);
        List<PayrollChangeComparison> comparisons = new ArrayList<>(ids.size());
        for (String id : ids) {
            Map<String, Object> afterValues = resolveHistoryValues(afterById, id);
            if (afterValues == null) {
                throw new NotFoundException("Payroll history not found: " + id);
            }
            accessControlService.requireOrganization(textValue(afterValues, "dwbm"));
            comparisons.add(buildPayrollChangeComparison(
                    id,
                    afterValues,
                    Optional.ofNullable(resolveHistoryValues(beforeById, id))));
        }
        return comparisons;
    }

    private Map<String, Object> resolveHistoryValues(Map<String, Map<String, Object>> valuesById, String id) {
        Map<String, Object> values = valuesById.get(id);
        if (values != null) {
            return values;
        }
        return valuesById.entrySet().stream()
                .filter(entry -> id.equals(String.valueOf(entry.getKey()).trim()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private PayrollChangeComparison buildPayrollChangeComparison(
            String payrollHistoryId,
            Map<String, Object> afterValues,
            Optional<Map<String, Object>> beforeValues) {
        List<PayrollChangeComponentComparison> components = PAYROLL_CHANGE_COMPARISON_FIELDS.stream()
                .sorted()
                .map(fieldName -> componentComparison(
                        fieldName,
                        approvalCaption(fieldName, fieldName),
                        beforeValues.orElse(null),
                        afterValues))
                .collect(Collectors.toCollection(ArrayList::new));
        return new PayrollChangeComparison(
                payrollHistoryId,
                beforeValues.map(values -> textValue(values, "id")).orElse(null),
                textValue(afterValues, "dwbm"),
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
                textValue(afterValues, "xckhndjb"),
                textValue(afterValues, "xckhndzw"),
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
            String year,
            PageRequest pageRequest) {
        int promotionYear = resolveNormalPromotionYear(year);
        var scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        if (Boolean.TRUE.equals(dueOnly)) {
            List<NormalPromotionPreview> eligiblePreviews = payrollRepository
                    .findPersonnelUidsWithCurrentPayroll(scope, emptyToNull(organizationCode), keyword)
                    .stream()
                    .map(uid -> normalPromotionPreview(uid, promotionYear))
                    .filter(preview -> Boolean.TRUE.equals(preview.eligible()))
                    .toList();
            int fromIndex = pageRequest.offset();
            if (fromIndex >= eligiblePreviews.size()) {
                return PageResponse.of(List.of(), pageRequest, eligiblePreviews.size());
            }
            int toIndex = Math.min(fromIndex + pageRequest.size(), eligiblePreviews.size());
            return PageResponse.of(eligiblePreviews.subList(fromIndex, toIndex), pageRequest, eligiblePreviews.size());
        }
        List<NormalPromotionPreview> previews = payrollRepository
                .findPersonnelUidsWithCurrentPayroll(scope, emptyToNull(organizationCode), keyword, pageRequest)
                .stream()
                .map(uid -> normalPromotionPreview(uid, promotionYear))
                .toList();
        return PageResponse.of(
                previews,
                pageRequest,
                payrollRepository.countPersonnelWithCurrentPayroll(scope, emptyToNull(organizationCode), keyword));
    }

    @Transactional
    public PromotionActionResult applyNormalPromotion(String payrollHistoryId, String year) {
        int uid = requireCurrentHistoryUid(payrollHistoryId);
        NormalPromotionPreview preview = normalPromotionPreview(uid, resolveNormalPromotionYear(year));
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
            String year,
            Boolean includeApply,
            Boolean includeProcessed,
            PageRequest pageRequest) {
        int promotionYear = resolveNormalPromotionYear(year);
        boolean showApply = !Boolean.FALSE.equals(includeApply);
        boolean showProcessed = !Boolean.FALSE.equals(includeProcessed);
        var scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        List<LevelPromotionCandidateRow> candidateRows = payrollRepository
                .findLevelPromotionCandidateRows(scope, emptyToNull(organizationCode), keyword, promotionYear);
        if (candidateRows.isEmpty()) {
            return PageResponse.of(List.of(), pageRequest, 0);
        }
        Map<Integer, PayrollHistorySnapshot> histories = payrollRepository.findLatestHistoriesByUids(
                candidateRows.stream().map(LevelPromotionCandidateRow::uid).distinct().toList());
        LevelPromotionAssessmentCache assessmentCache = loadLevelPromotionAssessmentCache(histories, promotionYear);
        List<LevelPromotionPreview> previews = new ArrayList<>();
        for (LevelPromotionCandidateRow row : candidateRows) {
            LevelPromotionPreview preview = levelPromotionListPreview(
                    row, promotionYear, histories.get(row.uid()), assessmentCache);
            if (preview != null
                    && (Boolean.TRUE.equals(preview.applyEligible()) || Boolean.TRUE.equals(preview.rollbackEligible()))
                    && matchesLevelPromotionStatusFilter(preview, showApply, showProcessed)) {
                previews.add(preview);
            }
        }
        int fromIndex = pageRequest.offset();
        if (fromIndex >= previews.size()) {
            return PageResponse.of(List.of(), pageRequest, previews.size());
        }
        int toIndex = Math.min(fromIndex + pageRequest.size(), previews.size());
        return PageResponse.of(previews.subList(fromIndex, toIndex), pageRequest, previews.size());
    }

    private LevelPromotionPreview levelPromotionListPreview(
            LevelPromotionCandidateRow candidateRow,
            int promotionYear,
            PayrollHistorySnapshot history,
            LevelPromotionAssessmentCache assessmentCache) {
        if (history == null) {
            return null;
        }
        if (!candidateRow.processed() && isLevelPromotionListExcludedChangeType(history.calculationType())) {
            return null;
        }
        if (candidateRow.processed()) {
            if (!isLevelPromotionProcessedInYear(history, promotionYear)) {
                return null;
            }
            return levelPromotionPreview(candidateRow.uid(), promotionYear, history, assessmentCache);
        }
        return levelPromotionPreview(candidateRow.uid(), promotionYear, history, assessmentCache);
    }

    private boolean matchesLevelPromotionStatusFilter(
            LevelPromotionPreview preview,
            boolean showApply,
            boolean showProcessed) {
        boolean apply = Boolean.TRUE.equals(preview.applyEligible());
        boolean processed = Boolean.TRUE.equals(preview.rollbackEligible());
        if (!apply && !processed) {
            return false;
        }
        if (apply && showApply) {
            return true;
        }
        return processed && showProcessed;
    }

    private LevelPromotionAssessmentCache loadLevelPromotionAssessmentCache(
            Map<Integer, PayrollHistorySnapshot> histories,
            int promotionYear) {
        if (histories.isEmpty()) {
            return LevelPromotionAssessmentCache.empty();
        }
        int assessmentEndYear = promotionYear - 1;
        int minStartYear = histories.values().stream()
                .mapToInt(history -> Math.min(
                        assessmentStartYear(
                                history.levelAssessmentStartYear(),
                                history.positionStartYearMonth(),
                                history.positionCode()),
                        assessmentStartYear(
                                history.stepAssessmentStartYear(),
                                history.positionStartYearMonth(),
                                history.positionCode())))
                .filter(year -> year > 0)
                .min()
                .orElse(assessmentEndYear);
        Map<Integer, List<PersonnelAssessmentYear>> assessments = payrollRepository.findAssessmentYearsByUids(
                histories.keySet().stream().toList(),
                minStartYear,
                assessmentEndYear);
        return new LevelPromotionAssessmentCache(assessments);
    }

    private record LevelPromotionAssessmentCache(Map<Integer, List<PersonnelAssessmentYear>> byUid) {
        private static final Set<String> QUALIFIED_RESULTS = Set.of("优秀", "称职", "合格");

        static LevelPromotionAssessmentCache empty() {
            return new LevelPromotionAssessmentCache(Map.of());
        }

        int countQualified(int uid, int startYear, int endYear) {
            if (startYear <= 0 || endYear < startYear) {
                return 0;
            }
            Set<Integer> qualifiedYears = new LinkedHashSet<>();
            for (PersonnelAssessmentYear assessment : byUid.getOrDefault(uid, List.of())) {
                if (assessment.year() >= startYear
                        && assessment.year() <= endYear
                        && QUALIFIED_RESULTS.contains(assessment.result())) {
                    qualifiedYears.add(assessment.year());
                }
            }
            return qualifiedYears.size();
        }

        List<Integer> missingYears(int uid, int startYear, int endYear) {
            if (startYear <= 0 || endYear < startYear) {
                return List.of();
            }
            Set<Integer> existingYears = byUid.getOrDefault(uid, List.of()).stream()
                    .map(PersonnelAssessmentYear::year)
                    .filter(year -> year >= startYear && year <= endYear)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            List<Integer> missing = new ArrayList<>();
            for (int year = startYear; year <= endYear; year++) {
                if (!existingYears.contains(year)) {
                    missing.add(year);
                }
            }
            return missing;
        }
    }

    @Transactional
    public PromotionActionResult applyLevelPromotion(String payrollHistoryId, String year) {
        int uid = requireCurrentHistoryUid(payrollHistoryId);
        LevelPromotionPreview preview = levelPromotionPreview(uid, resolveNormalPromotionYear(year));
        if (Boolean.TRUE.equals(preview.rollbackEligible())) {
            throw new IllegalArgumentException("当前记录已办理级别晋升，只能执行还原，不能再次办理。");
        }
        if (!Boolean.TRUE.equals(preview.applyEligible())) {
            throw new IllegalArgumentException("当前工资记录不满足级别晋升处理条件。");
        }
        if (!Boolean.TRUE.equals(preview.eligible()) || !Boolean.TRUE.equals(preview.levelPromotionDue())) {
            throw new IllegalArgumentException("当前工资记录不满足级别晋升处理条件。");
        }
        String changeType = Boolean.TRUE.equals(preview.reformLevelRollingDue()) ? "级别滚动" : "正常级别";
        int promotedStepValue = payrollRepository.intValue(preview.promotedStep());
        String positionSalaryGrade = String.valueOf(promotedStepValue);
        String gradeSalaryStep = "0";
        String promotionYear = Boolean.TRUE.equals(preview.reformLevelRollingDue())
                ? preview.nextLevelAssessmentStartYear()
                : preview.calculationPeriod().substring(0, 4);
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
        int uid = requireCurrentHistoryUid(payrollHistoryId);
        PayrollHistorySnapshot history = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        LevelPromotionPreview preview = levelPromotionPreview(uid, yearOf(history.calculationYear()));
        if (Boolean.TRUE.equals(preview.applyEligible())) {
            throw new IllegalArgumentException("当前记录存在待处理级别晋升，只能办理，不能执行还原。");
        }
        if (!Boolean.TRUE.equals(preview.rollbackEligible())) {
            throw new IllegalArgumentException("当前工资记录不满足级别晋升还原条件。");
        }
        return rollbackPromotion(payrollHistoryId, LEVEL_PROMOTION_ROLLBACK_CHANGE_TYPES, "级别晋升已还原。");
    }

    public PageResponse<PositionChangePromotionListItem> positionChangePromotionPreviews(
            String organizationCode,
            String keyword,
            PageRequest pageRequest) {
        var scope = accessControlService.organizationScope(Optional.ofNullable(emptyToNull(organizationCode)));
        String scopedOrganizationCode = emptyToNull(organizationCode);
        PositionChangePromotionPage page = payrollRepository
                .findPositionChangePromotionPage(scope, scopedOrganizationCode, keyword, pageRequest);
        PositionChangePreviewCache cache = loadPositionChangePreviewCache(page.rows());
        List<PositionChangePromotionListItem> previews = page.rows().stream()
                .map(row -> positionChangePromotionListItem(row.uid(), cache))
                .toList();
        return PageResponse.of(previews, pageRequest, page.total());
    }

    public PositionChangePromotionPreview positionChangePromotionDetail(String payrollHistoryId) {
        int uid = requireCurrentHistoryUid(payrollHistoryId);
        return positionChangePromotionPreview(uid, loadPositionChangePreviewCacheForUids(List.of(uid)), true, true);
    }

    @Transactional
    public PromotionActionResult applyPositionChangePromotion(String payrollHistoryId) {
        int uid = requireCurrentHistoryUid(payrollHistoryId);
        PayrollHistorySnapshot latest = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        PositionChangePromotionPreview preview = positionChangePromotionPreview(uid, loadPositionChangePreviewCacheForUids(List.of(uid)), true, true);
        if (Boolean.TRUE.equals(preview.rollbackEligible())) {
            throw new IllegalArgumentException("当前记录已办理职务变化，只能执行还原，不能再次办理。");
        }
        if (!Boolean.TRUE.equals(preview.applyEligible()) || preview.totalIncrease() == null) {
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
                latest.storedTechnicalGradeSalary(),
                null,
                null,
                null,
                preview.totalIncrease(),
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
        int uid = requireCurrentHistoryUid(payrollHistoryId);
        PositionChangePromotionPreview preview = positionChangePromotionPreview(uid, loadPositionChangePreviewCacheForUids(List.of(uid)), true, true);
        if (Boolean.TRUE.equals(preview.applyEligible())) {
            throw new IllegalArgumentException("当前记录存在待处理职务变化，只能办理，不能执行还原。");
        }
        if (!Boolean.TRUE.equals(preview.rollbackEligible())) {
            throw new IllegalArgumentException("当前工资记录不满足职务变化还原条件。");
        }
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
                Set.of("同序列职务变化", "职务变化", "职级晋升", "警员套改", "警务套改", "法检套改", "职级套改", "事业岗位变动", "转换序列"),
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


    private PromotionActionResult unsupportedAction(String operation) {
        throw new UnsupportedOperationException("Not implemented: " + operation);
    }

    private <T> PageResponse<T> unsupportedPage() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public PromotionActionResult applyPositionChangePromotion(String payrollHistoryId, PositionChangeApplyRequest request) {
        return applyPositionChangePromotion(payrollHistoryId);
    }

    @Transactional
    public PageResponse<AllowanceRecalculationPreview> allowanceRecalculations(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public PromotionActionResult applyAllowanceRecalculation(String payrollHistoryId) { return unsupportedAction("applyAllowanceRecalculation"); }

    @Transactional
    public PromotionActionResult applyBasicSalaryStandardAdjustment(String payrollHistoryId, String targetStandardYearMonth) { return unsupportedAction("applyBasicSalaryStandardAdjustment"); }

    @Transactional
    public PromotionActionResult applyCivilAllowanceStandardAdjustment(String payrollHistoryId, String targetStandardYearMonth) { return unsupportedAction("applyCivilAllowanceStandardAdjustment"); }

    @Transactional
    public PromotionActionResult applyEducationPromotion(String payrollHistoryId) { return unsupportedAction("applyEducationPromotion"); }

    @Transactional
    public PromotionActionResult applyFloatingToFixedConversion(String payrollHistoryId) { return unsupportedAction("applyFloatingToFixedConversion"); }

    @Transactional
    public PromotionActionResult applyInternSalaryChange(String payrollHistoryId) { return unsupportedAction("applyInternSalaryChange"); }

    @Transactional
    public PromotionActionResult applyJudicialAllowanceAdjustment(String payrollHistoryId) { return unsupportedAction("applyJudicialAllowanceAdjustment"); }

    @Transactional
    public PromotionActionResult applyJudicialRankChangePromotion(String payrollHistoryId) { return unsupportedAction("applyJudicialRankChangePromotion"); }

    @Transactional
    public PromotionActionResult applyMonthlyAverageSalary(String payrollHistoryId, String year) { return unsupportedAction("applyMonthlyAverageSalary"); }

    @Transactional
    public PromotionActionResult applyNewPersonnelSalaryDetermination(int uid) { return unsupportedAction("applyNewPersonnelSalaryDetermination"); }

    @Transactional
    public PromotionActionResult applyOtherPayrollChange(String payrollHistoryId, PayrollHistoryMaintenanceRequest request) { return unsupportedAction("applyOtherPayrollChange"); }

    @Transactional
    public PromotionActionResult applyPerformanceRatioAdjustment(String payrollHistoryId) { return unsupportedAction("applyPerformanceRatioAdjustment"); }

    @Transactional
    public PromotionActionResult applyPerformanceStandardAdjustment(String payrollHistoryId, String targetStandardYearMonth) { return unsupportedAction("applyPerformanceStandardAdjustment"); }

    @Transactional
    public PromotionActionResult applyPoliceAllowanceAdjustment(String payrollHistoryId) { return unsupportedAction("applyPoliceAllowanceAdjustment"); }

    @Transactional
    public PromotionActionResult applyPoliceRankChangePromotion(String payrollHistoryId) { return unsupportedAction("applyPoliceRankChangePromotion"); }

    @Transactional
    public PromotionActionResult applyProsecutionAllowanceAdjustment(String payrollHistoryId) { return unsupportedAction("applyProsecutionAllowanceAdjustment"); }

    @Transactional
    public PromotionActionResult applyProsecutionRankChangePromotion(String payrollHistoryId) { return unsupportedAction("applyProsecutionRankChangePromotion"); }

    @Transactional
    public PromotionActionResult applyReformLevelRolling(String payrollHistoryId, String year) { return unsupportedAction("applyReformLevelRolling"); }

    @Transactional
    public PromotionActionResult applyRegularization(String payrollHistoryId) { return unsupportedAction("applyRegularization"); }

    @Transactional
    public PromotionActionResult applyRegularizationHighGrade(String payrollHistoryId) { return unsupportedAction("applyRegularizationHighGrade"); }

    @Transactional
    public PromotionActionResult applySalaryStandardAdjustment(String payrollHistoryId, String targetStandardYearMonth) { return unsupportedAction("applySalaryStandardAdjustment"); }

    @Transactional
    public PromotionActionResult applySupervisionAllowanceAdjustment(String payrollHistoryId) { return unsupportedAction("applySupervisionAllowanceAdjustment"); }

    @Transactional
    public PromotionActionResult applySupervisionRankChangePromotion(String payrollHistoryId) { return unsupportedAction("applySupervisionRankChangePromotion"); }

    @Transactional
    public PromotionActionResult applyTeachingAllowanceAdjustment(String payrollHistoryId) { return unsupportedAction("applyTeachingAllowanceAdjustment"); }

    @Transactional
    public PromotionActionResult applyWageReform2006(int uid) { return unsupportedAction("applyWageReform2006"); }

    @Transactional
    public PromotionActionResult backfillWageReform2006Dtgxx(int uid) { return unsupportedAction("backfillWageReform2006Dtgxx"); }

    @Transactional
    public PageResponse<SalaryStandardAdjustmentPreview> basicSalaryStandardAdjustments(String organizationCode, String keyword, String targetStandardYearMonth, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public PageResponse<SalaryStandardAdjustmentPreview> civilAllowanceStandardAdjustments(String organizationCode, String keyword, String targetStandardYearMonth, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public AllowanceStandard createAllowanceStandard(AllowanceStandardRequest request) { requireStandardWritePermission(); int id = payrollRepository.insertAllowanceStandard(request); return payrollRepository.findAllowanceStandardById(id); }

    @Transactional
    public GradeSalaryStandard createGradeSalaryStandard(GradeSalaryStandardRequest request) { requireStandardWritePermission(); payrollRepository.insertGradeSalaryStandard(request); return payrollRepository.findGradeSalaryStandard(request.standardYearMonth(), request.gradeLevel()); }

    @Transactional
    public InternSalaryStandard createInternSalaryStandard(InternSalaryStandardRequest request) { requireStandardWritePermission(); payrollRepository.insertInternSalaryStandard(request); return payrollRepository.findInternSalaryStandardByKey(request.standardYearMonth(), request.educationCode(), request.regularPositionCode()); }

    @Transactional
    public OtherAllowanceStandard createOtherAllowanceStandard(OtherAllowanceStandardRequest request) { requireStandardWritePermission(); payrollRepository.insertOtherAllowanceStandard(request); return payrollRepository.findOtherAllowanceStandardByKey(request.standardType(), request.standardYearMonth(), request.code()); }

    @Transactional
    public PositionGradeSalaryStandard createPositionGradeSalaryStandard(PositionGradeSalaryStandardRequest request) { requireStandardWritePermission(); payrollRepository.insertPositionGradeSalaryStandard(request); return payrollRepository.findPositionGradeSalaryStandard(request.standardYearMonth(), request.positionCode()); }

    @Transactional
    public PositionSalaryStandard createPositionSalaryStandard(PositionSalaryStandardRequest request) { requireStandardWritePermission(); payrollRepository.insertPositionSalaryStandard(request); return payrollRepository.findPositionSalaryStandard(request.standardYearMonth(), request.positionCode()); }

    @Transactional
    public RankAllowanceStandard createRankAllowanceStandard(RankAllowanceStandardRequest request) { requireStandardWritePermission(); int id = payrollRepository.insertRankAllowanceStandard(request); return payrollRepository.findRankAllowanceStandardById(id); }

    @Transactional
    public RetainedAllowanceStandard createRetainedAllowanceStandard(RetainedAllowanceStandardRequest request) { requireStandardWritePermission(); payrollRepository.insertRetainedAllowanceStandard(request); return payrollRepository.findRetainedAllowanceStandardByPositionCode(request.positionCode()); }

    @Transactional
    public SalaryLevelStandard createSalaryLevelStandard(SalaryLevelStandardRequest request) { requireStandardWritePermission(); payrollRepository.insertSalaryLevelStandard(request); return payrollRepository.findSalaryLevelStandard(request.standardYearMonth(), request.jobCategoryCode(), request.salaryLevel()); }

    @Transactional
    public WageReformStandard createWageReformStandard(WageReformStandardRequest request) { requireStandardWritePermission(); payrollRepository.insertWageReformStandard(request); return payrollRepository.findWageReformStandardByKey(request.positionCode(), request.appointmentYearsLower(), request.appointmentYearsUpper(), request.reformYearsLower(), request.reformYearsUpper()); }

    @Transactional
    public YearAllowanceStandard createYearAllowanceStandard(YearAllowanceStandardRequest request) { requireStandardWritePermission(); payrollRepository.insertYearAllowanceStandard(request); return payrollRepository.findYearAllowanceStandardByYearMonth(request.standardYearMonth()); }

    @Transactional
    public void deleteAllowanceStandard(int id) { requireStandardWritePermission(); payrollRepository.deleteAllowanceStandard(id); }

    @Transactional
    public void deleteGradeSalaryStandard(String standardYearMonth, String gradeLevel) { requireStandardWritePermission(); payrollRepository.deleteGradeSalaryStandard(standardYearMonth, gradeLevel); }

    @Transactional
    public void deleteInternSalaryStandard(String standardYearMonth, String educationCode, String regularPositionCode) { requireStandardWritePermission(); payrollRepository.deleteInternSalaryStandard(standardYearMonth, educationCode, regularPositionCode); }

    @Transactional
    public void deleteOtherAllowanceStandard(String standardType, String standardYearMonth, String code) { requireStandardWritePermission(); payrollRepository.deleteOtherAllowanceStandard(standardType, standardYearMonth, code); }

    @Transactional
    public void deletePositionGradeSalaryStandard(String standardYearMonth, String positionCode) { requireStandardWritePermission(); payrollRepository.deletePositionGradeSalaryStandard(standardYearMonth, positionCode); }

    @Transactional
    public void deletePositionSalaryStandard(String standardYearMonth, String positionCode) { requireStandardWritePermission(); payrollRepository.deletePositionSalaryStandard(standardYearMonth, positionCode); }

    @Transactional
    public void deleteRankAllowanceStandard(int id) { requireStandardWritePermission(); payrollRepository.deleteRankAllowanceStandard(id); }

    @Transactional
    public void deleteRetainedAllowanceStandard(String positionCode) { requireStandardWritePermission(); payrollRepository.deleteRetainedAllowanceStandard(positionCode); }

    @Transactional
    public void deleteSalaryLevelStandard(String standardYearMonth, String jobCategoryCode, String salaryLevel) { requireStandardWritePermission(); payrollRepository.deleteSalaryLevelStandard(standardYearMonth, jobCategoryCode, salaryLevel); }

    @Transactional
    public void deleteWageReformStandard(String positionCode, int appointmentYearsLower, int appointmentYearsUpper, int reformYearsLower, int reformYearsUpper) { requireStandardWritePermission(); payrollRepository.deleteWageReformStandard(positionCode, appointmentYearsLower, appointmentYearsUpper, reformYearsLower, reformYearsUpper); }

    @Transactional
    public void deleteYearAllowanceStandard(String standardYearMonth) { requireStandardWritePermission(); payrollRepository.deleteYearAllowanceStandard(standardYearMonth); }

    @Transactional
    public PageResponse<FloatingToFixedPreview> floatingToFixedPreviews(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public PageResponse<InternSalaryChangePreview> internSalaryChanges(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public PageResponse<RankAllowanceStandardAdjustment> judicialAllowanceAdjustments(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public PageResponse<RankAllowanceChangePromotion> judicialRankChangePromotions(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public PageResponse<MonthlyAverageSalaryPreview> monthlyAverageSalaryPreviews(String organizationCode, String keyword, String year, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public PageResponse<NewPersonnelSalaryPreview> newPersonnelSalaryDeterminations(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public PageResponse<OtherPayrollChangePreview> otherPayrollChanges(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public PageResponse<PerformanceRatioAdjustmentPreview> performanceRatioAdjustments(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public PageResponse<SalaryStandardAdjustmentPreview> performanceStandardAdjustments(String organizationCode, String keyword, String targetStandardYearMonth, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public PageResponse<RankAllowanceStandardAdjustment> policeAllowanceAdjustments(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public PageResponse<RankAllowanceChangePromotion> policeRankChangePromotions(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public PageResponse<RankAllowanceStandardAdjustment> prosecutionAllowanceAdjustments(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public PageResponse<RankAllowanceChangePromotion> prosecutionRankChangePromotions(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public PageResponse<ReformLevelRollingPreview> reformLevelRollingPreviews(String organizationCode, String keyword, String year, Boolean includeApply, Boolean includeProcessed, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public PageResponse<RegularizationHighGradePreview> regularizationHighGradePreviews(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public PromotionActionResult rollbackAllowanceRecalculation(String payrollHistoryId) { return unsupportedAction("rollbackAllowanceRecalculation"); }

    @Transactional
    public PromotionActionResult rollbackBasicSalaryStandardAdjustment(String payrollHistoryId) { return unsupportedAction("rollbackBasicSalaryStandardAdjustment"); }

    @Transactional
    public PromotionActionResult rollbackCivilAllowanceStandardAdjustment(String payrollHistoryId) { return unsupportedAction("rollbackCivilAllowanceStandardAdjustment"); }

    @Transactional
    public PromotionActionResult rollbackEducationPromotion(String payrollHistoryId) { return unsupportedAction("rollbackEducationPromotion"); }

    @Transactional
    public PromotionActionResult rollbackFloatingToFixedConversion(String payrollHistoryId) { return unsupportedAction("rollbackFloatingToFixedConversion"); }

    @Transactional
    public PromotionActionResult rollbackInternSalaryChange(String payrollHistoryId) { return unsupportedAction("rollbackInternSalaryChange"); }

    @Transactional
    public PromotionActionResult rollbackJudicialAllowanceAdjustment(String payrollHistoryId) { return unsupportedAction("rollbackJudicialAllowanceAdjustment"); }

    @Transactional
    public PromotionActionResult rollbackJudicialRankChangePromotion(String payrollHistoryId) { return unsupportedAction("rollbackJudicialRankChangePromotion"); }

    @Transactional
    public PromotionActionResult rollbackMonthlyAverageSalary(String payrollHistoryId, String year) { return unsupportedAction("rollbackMonthlyAverageSalary"); }

    @Transactional
    public PromotionActionResult rollbackNewPersonnelSalaryDetermination(String payrollHistoryId) { return unsupportedAction("rollbackNewPersonnelSalaryDetermination"); }

    @Transactional
    public PromotionActionResult rollbackOtherPayrollChange(String payrollHistoryId) { return unsupportedAction("rollbackOtherPayrollChange"); }

    @Transactional
    public PromotionActionResult rollbackPerformanceRatioAdjustment(String payrollHistoryId) { return unsupportedAction("rollbackPerformanceRatioAdjustment"); }

    @Transactional
    public PromotionActionResult rollbackPerformanceStandardAdjustment(String payrollHistoryId) { return unsupportedAction("rollbackPerformanceStandardAdjustment"); }

    @Transactional
    public PromotionActionResult rollbackPoliceAllowanceAdjustment(String payrollHistoryId) { return unsupportedAction("rollbackPoliceAllowanceAdjustment"); }

    @Transactional
    public PromotionActionResult rollbackPoliceRankChangePromotion(String payrollHistoryId) { return unsupportedAction("rollbackPoliceRankChangePromotion"); }

    @Transactional
    public PromotionActionResult rollbackProsecutionAllowanceAdjustment(String payrollHistoryId) { return unsupportedAction("rollbackProsecutionAllowanceAdjustment"); }

    @Transactional
    public PromotionActionResult rollbackProsecutionRankChangePromotion(String payrollHistoryId) { return unsupportedAction("rollbackProsecutionRankChangePromotion"); }

    @Transactional
    public PromotionActionResult rollbackReformLevelRolling(String payrollHistoryId) { return unsupportedAction("rollbackReformLevelRolling"); }

    @Transactional
    public PromotionActionResult rollbackRegularization(String payrollHistoryId) { return unsupportedAction("rollbackRegularization"); }

    @Transactional
    public PromotionActionResult rollbackRegularizationHighGrade(String payrollHistoryId) { return unsupportedAction("rollbackRegularizationHighGrade"); }

    @Transactional
    public PromotionActionResult rollbackSalaryStandardAdjustment(String payrollHistoryId) { return unsupportedAction("rollbackSalaryStandardAdjustment"); }

    @Transactional
    public PromotionActionResult rollbackSupervisionAllowanceAdjustment(String payrollHistoryId) { return unsupportedAction("rollbackSupervisionAllowanceAdjustment"); }

    @Transactional
    public PromotionActionResult rollbackSupervisionRankChangePromotion(String payrollHistoryId) { return unsupportedAction("rollbackSupervisionRankChangePromotion"); }

    @Transactional
    public PromotionActionResult rollbackTeachingAllowanceAdjustment(String payrollHistoryId) { return unsupportedAction("rollbackTeachingAllowanceAdjustment"); }

    @Transactional
    public PromotionActionResult rollbackWageReform2006(String payrollHistoryId) { return unsupportedAction("rollbackWageReform2006"); }

    @Transactional
    public PromotionActionResult rollbackWageReform2006Dtgxx(int uid) { return unsupportedAction("rollbackWageReform2006Dtgxx"); }

    @Transactional
    public PageResponse<SalaryStandardAdjustmentPreview> salaryStandardAdjustments(String organizationCode, String keyword, String targetStandardYearMonth, String scope, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public PageResponse<RankAllowanceStandardAdjustment> supervisionAllowanceAdjustments(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public PageResponse<RankAllowanceChangePromotion> supervisionRankChangePromotions(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }

    @Transactional
    public AllowanceStandard updateAllowanceStandard(int id, AllowanceStandardRequest request) { requireStandardWritePermission(); payrollRepository.updateAllowanceStandard(id, request); return payrollRepository.findAllowanceStandardById(id); }

    @Transactional
    public GradeSalaryStandard updateGradeSalaryStandard(String standardYearMonth, String gradeLevel, GradeSalaryStandardRequest request) { requireStandardWritePermission(); payrollRepository.updateGradeSalaryStandard(standardYearMonth, gradeLevel, request); return payrollRepository.findGradeSalaryStandard(standardYearMonth, gradeLevel); }

    @Transactional
    public InternSalaryStandard updateInternSalaryStandard(String standardYearMonth, String educationCode, String regularPositionCode, InternSalaryStandardRequest request) { requireStandardWritePermission(); payrollRepository.updateInternSalaryStandard(standardYearMonth, educationCode, regularPositionCode, request); return payrollRepository.findInternSalaryStandardByKey(standardYearMonth, educationCode, regularPositionCode); }

    @Transactional
    public OtherAllowanceStandard updateOtherAllowanceStandard(String standardType, String standardYearMonth, String code, OtherAllowanceStandardRequest request) { requireStandardWritePermission(); OtherAllowanceStandardRequest merged = new OtherAllowanceStandardRequest(standardType, standardYearMonth != null ? standardYearMonth : request.standardYearMonth(), code != null ? code : request.code(), request.name(), request.amount(), request.averageAmount(), request.multiplier()); payrollRepository.updateOtherAllowanceStandard(standardType, merged); return payrollRepository.findOtherAllowanceStandardByKey(standardType, merged.standardYearMonth(), merged.code()); }

    @Transactional
    public PositionGradeSalaryStandard updatePositionGradeSalaryStandard(String standardYearMonth, String positionCode, PositionGradeSalaryStandardRequest request) { requireStandardWritePermission(); payrollRepository.updatePositionGradeSalaryStandard(standardYearMonth, positionCode, request); return payrollRepository.findPositionGradeSalaryStandard(standardYearMonth, positionCode); }

    @Transactional
    public PositionSalaryStandard updatePositionSalaryStandard(String standardYearMonth, String positionCode, PositionSalaryStandardRequest request) { requireStandardWritePermission(); payrollRepository.updatePositionSalaryStandard(standardYearMonth, positionCode, request); return payrollRepository.findPositionSalaryStandard(standardYearMonth, positionCode); }

    @Transactional
    public RankAllowanceStandard updateRankAllowanceStandard(int id, RankAllowanceStandardRequest request) { requireStandardWritePermission(); payrollRepository.updateRankAllowanceStandard(id, request); return payrollRepository.findRankAllowanceStandardById(id); }

    @Transactional
    public RetainedAllowanceStandard updateRetainedAllowanceStandard(String positionCode, RetainedAllowanceStandardRequest request) { requireStandardWritePermission(); payrollRepository.updateRetainedAllowanceStandard(positionCode, request); return payrollRepository.findRetainedAllowanceStandardByPositionCode(positionCode); }

    @Transactional
    public SalaryLevelStandard updateSalaryLevelStandard(String standardYearMonth, String jobCategoryCode, String salaryLevel, SalaryLevelStandardRequest request) { requireStandardWritePermission(); payrollRepository.updateSalaryLevelStandard(standardYearMonth, jobCategoryCode, salaryLevel, request); return payrollRepository.findSalaryLevelStandard(standardYearMonth, jobCategoryCode, salaryLevel); }

    @Transactional
    public WageReformStandard updateWageReformStandard(String positionCode, int appointmentYearsLower, int appointmentYearsUpper, int reformYearsLower, int reformYearsUpper, WageReformStandardRequest request) { requireStandardWritePermission(); payrollRepository.updateWageReformStandard(positionCode, appointmentYearsLower, appointmentYearsUpper, reformYearsLower, reformYearsUpper, request); return payrollRepository.findWageReformStandardByKey(positionCode, appointmentYearsLower, appointmentYearsUpper, reformYearsLower, reformYearsUpper); }

    @Transactional
    public YearAllowanceStandard updateYearAllowanceStandard(String standardYearMonth, YearAllowanceStandardRequest request) { requireStandardWritePermission(); payrollRepository.updateYearAllowanceStandard(standardYearMonth, request); return payrollRepository.findYearAllowanceStandardByYearMonth(standardYearMonth); }

    @Transactional
    public PageResponse<WageReform2006Preview> wageReform2006Previews(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }

    private boolean isNormalPromotionProcessedInYear(int uid, int promotionYear) {
        PayrollHistorySnapshot latest = payrollRepository.findLatestHistory(uid).orElse(null);
        if (latest == null) {
            return false;
        }
        String period = latest.calculationYear() + latest.calculationMonth();
        return period.startsWith(String.valueOf(promotionYear))
                && ("正常档次".equals(latest.calculationType()) || "正常薪级".equals(latest.calculationType()));
    }

    private BasicPayrollCalculation basicCalculation(WageProjectionState state, PayrollHistorySnapshot latest) {
        return basicCalculation(state, latest, state.salaryStandardYearMonth());
    }

    private BasicPayrollCalculation basicCalculation(
            WageProjectionState state,
            PayrollHistorySnapshot latest,
            String lookupPeriod) {
        String standardYearMonth = state.salaryStandardYearMonth();
        String positionCode = state.positionCode();
        String salarySource = resolvedBaseSalarySource(state);
        int probationSalary = internSalaryAmount(state, latest, lookupPeriod);
        Integer gradeSalary = probationSalary > 0
                ? probationSalary
                : switch (salarySource) {
            case "GRADE" -> payrollRepository.civilServantGradeSalary(
                    state.level(), state.stepOrSalaryLevel(), state.gradeStepDifferenceCount(), standardYearMonth);
            case "POLICE_GRADE" -> payrollRepository.policeOfficerGradeSalary(
                    state.level(),
                    policeGradeStep(state.stepOrSalaryLevel(), state.gradeStepDifferenceCount()),
                    standardYearMonth);
            default -> null;
        };
        Integer salaryLevelSalary = "SALARY_LEVEL".equals(salarySource)
                ? payrollRepository.salaryLevelSalary(state.stepOrSalaryLevel(), "0", standardYearMonth, positionCode)
                : payrollRepository.salaryLevelSalary(
                latest.positionSalaryGrade(),
                latest.gradeSalaryStep(),
                standardYearMonth,
                positionCode);
        Integer technicalGradeSalary = payrollRepository.technicalGradeSalary(positionCode, standardYearMonth);
        int positionSalary = probationSalary > 0
                ? 0
                : payrollRepository.positionSalary(positionCode, standardYearMonth)
                        + payrollRepository.positionGradeSalary(
                        positionCode,
                        state.stepOrSalaryLevel(),
                        "0",
                        standardYearMonth);
        Integer selectedBaseSalary = switch (salarySource) {
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
                salarySource,
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
                preview("SDBT", sdbtCaption(context.latestHistory()), allowance.subsidyAllowance(), "AUTO"),
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
        PayrollHistorySnapshot latest = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        WageProjectionRun projection = runWageProjection(
                uid, period, projectionAuditStopFor(record), record, true);
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
                    List.of(),
                    projection.stepDetails() == null ? List.of() : projection.stepDetails());
        }
        WageProjectionState amountState = bindAmountStateForHistoryAudit(
                projection, record, latest, period);
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
                    List.of(),
                    projection.stepDetails() == null ? List.of() : projection.stepDetails());
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
        BasicPayrollCalculation basicCalculation = basicCalculation(amountState, latest, period);
        AllowanceCalculation allowanceCalculation = allowanceCalculation(amountState, latest);
        AdditionalPayrollCalculation additionalCalculation = additionalCalculation(amountState, latest);
        PayrollTotalComparison total = totalComparison(
                record,
                latest,
                components,
                basicCalculation,
                allowanceCalculation,
                additionalCalculation);
        List<String> structureMismatches = projectionStructureMismatches(amountState, record);
        BigDecimal projectedTotal = resolveProjectedTotalForHistoryAudit(projection, record, period, amountState, latest);
        BigDecimal totalDifference = projectedTotal.subtract(BigDecimal.valueOf(nullToZero(record.storedTotal())));
        boolean matched = totalDifference.compareTo(BigDecimal.ZERO) == 0 && structureMismatches.isEmpty();
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
                projectedTotal,
                totalDifference,
                structureMismatches,
                componentDifferences,
                projection.stepDetails() == null ? List.of() : projection.stepDetails());
    }

    /**
     * 对账汇总合计与分步明细一致：优先取同调资年月的分步合计（工资项直接相加），
     * 避免 recalculatedKnownTotal 在存量合计上局部替换后与分步口径不一致。
     */
    private BigDecimal resolveProjectedTotalForHistoryAudit(
            WageProjectionRun projection,
            PayrollHistorySnapshot record,
            String period,
            WageProjectionState amountState,
            PayrollHistorySnapshot latest) {
        Optional<WageProjectionStepDetail> auditStep = findAuditStepDetailForRecord(
                projection.stepDetails(), record, period);
        if (auditStep.isPresent()) {
            WageProjectionState stepState = amountStateFromAuditStep(projection.state(), auditStep.get(), latest);
            return projectionStepTotal(projectionStepComponents(latest, stepState, period));
        }
        return projectionStepTotal(projectionStepComponents(latest, amountState, period));
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
        if (!structureValuesEqual(label, projected, stored)) {
            mismatches.add(label + "：推算=" + displayText(projected) + "，调资=" + displayText(stored));
        }
    }

    private boolean structureValuesEqual(String label, String projected, String stored) {
        if ("级差".equals(label)) {
            return payrollRepository.intValue(projected) == payrollRepository.intValue(stored);
        }
        return normalizedEquals(projected, stored);
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
        String period = historyCalculationPeriod(history);
        String allowanceStandard = resolveAllowanceStandardYearMonth(
                period,
                history.organizationCode(),
                history.positionCode(),
                history.allowanceStandardYearMonth());
        return allowanceCalculation(history, history.positionCode(), allowanceStandard);
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
        return totalComparison(history, history, components, basic, allowance, additional);
    }

    private PayrollTotalComparison totalComparison(
            PayrollHistorySnapshot history,
            PayrollHistorySnapshot calculationContext,
            List<PayrollComponentValue> components,
            BasicPayrollCalculation basic,
            AllowanceCalculation allowance,
            AdditionalPayrollCalculation additional) {
        Integer teachingAllowance = teachingAllowance(calculationContext);
        Integer salaryIncrease = salaryIncrease(calculationContext, basic);
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
        addDifference(differences, "SDBT", sdbtCaption(history), history.storedSubsidyAllowance(), allowance.subsidyAllowance());
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
        return dfbt2CaptionForPosition(history.positionCode(), history.organizationType());
    }

    private String sdbtCaption(PayrollHistorySnapshot history) {
        return sdbtCaptionForPosition(history.positionCode(), history.organizationType());
    }

    private String dfbt2CaptionForPosition(String positionCode) {
        return dfbt2CaptionForPosition(positionCode, null);
    }

    private String dfbt2CaptionForPosition(String positionCode, String organizationType) {
        if (isCivilServantPayroll(positionCode, organizationType)) {
            return "生活性补贴";
        }
        return "基础性绩效工资";
    }

    private String sdbtCaptionForPosition(String positionCode) {
        return sdbtCaptionForPosition(positionCode, null);
    }

    private String sdbtCaptionForPosition(String positionCode, String organizationType) {
        if (isCivilServantPayroll(positionCode, organizationType)) {
            return "工作性津贴";
        }
        return "工作性补贴";
    }

    private boolean isCivilServantPayroll(String positionCode, String organizationType) {
        if (organizationType != null && organizationType.compareTo("07") < 0) {
            return true;
        }
        if (emptyToNull(positionCode) == null || positionCode.length() < 2) {
            return false;
        }
        return List.of("01", "02", "03", "04", "05", "06", "21", "22", "23", "24", "25", "26", "27", "28", "29")
                .contains(positionCode.substring(0, 2));
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

    private NormalPromotionPreview normalPromotionPreview(int uid, int promotionYear) {
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
        int calculationYear = promotionYear;
        int stepStartYear = assessmentStartYear(
                history.stepAssessmentStartYear(),
                history.positionStartYearMonth(),
                history.positionCode());
        int qualifiedYears = payrollRepository.countQualifiedAssessmentYears(
                history.organizationCode(), history.personCode(), stepStartYear, calculationYear - 1);
        int requiredYears = normalPromotionRequiredYears(history);
        int assessmentEndYear = calculationYear - 1;
        String assessmentPeriod = formatAssessmentPeriod(stepStartYear, assessmentEndYear);
        boolean eligible = requiredYears > 0 && qualifiedYears >= requiredYears && calculationYear >= 2007
                && !"POLICE_GRADE".equals(baseSalarySource);
        boolean rollbackEligible = isNormalPromotionProcessedInYear(uid, calculationYear);
        boolean applyEligible = eligible && !rollbackEligible;
        return new NormalPromotionPreview(
                uid,
                history.id(),
                history.organizationCode(),
                history.personCode(),
                history.name(),
                String.format("%04d01", calculationYear),
                history.calculationType(),
                history.positionCode(),
                history.positionName(),
                history.salaryStandardYearMonth(),
                history.positionSalaryGrade(),
                promotedGradeOrLevel,
                history.gradeSalaryLevel(),
                history.levelAssessmentStartYear(),
                history.stepAssessmentStartYear(),
                assessmentPeriod,
                qualifiedYears,
                requiredYears,
                eligible,
                applyEligible,
                false,
                false,
                rollbackEligible,
                "",
                currentBaseSalary,
                promotedBaseSalary,
                nullToZero(promotedBaseSalary) - nullToZero(currentBaseSalary),
                baseSalarySource);
    }

    private String formatAssessmentPeriod(int startYear, int endYear) {
        if (startYear <= 0 || endYear < startYear) {
            return "";
        }
        if (startYear == endYear) {
            return String.valueOf(startYear);
        }
        return startYear + "～" + endYear;
    }

    private int resolveNormalPromotionYear(String year) {
        String normalized = emptyToNull(year);
        if (normalized == null) {
            return LocalDate.now().getYear();
        }
        if (!normalized.matches("\\d{4}")) {
            throw new IllegalArgumentException("晋升年度必须为四位年份。");
        }
        return Integer.parseInt(normalized);
    }

    private NormalPromotionPreview normalPromotionPreview(int uid) {
        PayrollHistorySnapshot history = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        return normalPromotionPreview(uid, yearOf(history.calculationYear()));
    }

    private LevelPromotionPreview levelPromotionPreview(int uid, int promotionYear) {
        PayrollHistorySnapshot history = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        return levelPromotionPreview(uid, promotionYear, history);
    }

    private LevelPromotionPreview levelPromotionPreview(int uid, int promotionYear, PayrollHistorySnapshot history) {
        return levelPromotionPreview(uid, promotionYear, history, null);
    }

    private LevelPromotionPreview levelPromotionPreview(
            int uid,
            int promotionYear,
            PayrollHistorySnapshot history,
            LevelPromotionAssessmentCache assessmentCache) {
        Integer currentGradeSalary = payrollRepository.civilServantGradeSalary(
                history.gradeSalaryLevel(),
                history.positionSalaryGrade(),
                history.gradeSalaryStep(),
                history.salaryStandardYearMonth());
        int calculationYear = promotionYear;
        int levelStartYear = assessmentStartYear(
                history.levelAssessmentStartYear(),
                history.positionStartYearMonth(),
                history.positionCode());
        int stepStartYear = assessmentStartYear(
                history.stepAssessmentStartYear(),
                history.positionStartYearMonth(),
                history.positionCode());
        int assessmentEndYear = calculationYear - 1;
        int qualifiedYearsForLevel = assessmentCache == null
                ? payrollRepository.countQualifiedAssessmentYears(
                        history.organizationCode(), history.personCode(), levelStartYear, assessmentEndYear)
                : assessmentCache.countQualified(uid, levelStartYear, assessmentEndYear);
        int qualifiedYearsForStep = assessmentCache == null
                ? payrollRepository.countQualifiedAssessmentYears(
                        history.organizationCode(), history.personCode(), stepStartYear, assessmentEndYear)
                : assessmentCache.countQualified(uid, stepStartYear, assessmentEndYear);
        List<Integer> missingLevelAssessmentYears = assessmentCache == null
                ? missingAssessmentYears(
                        history.organizationCode(), history.personCode(), levelStartYear, assessmentEndYear)
                : assessmentCache.missingYears(uid, levelStartYear, assessmentEndYear);
        boolean levelSpanExceedsFiveYears = calculationYear - levelStartYear > 5;
        WageProjectionStart projectionStart = wageProjectionStart(history);
        WageProjectionState projectionState = projectionStateFromHistory(history);
        int payrollPeriod = payrollRepository.intValue(normalizeYearMonth(history.calculationYear() + history.calculationMonth()));
        Set<Integer> appliedRollingYears = appliedReformLevelRollingYears(history);
        int rollingLimitYear = Math.min(2010, calculationYear);
        ReformRollingSimulation rollingSimulation = simulateReformLevelRolling(
                history,
                projectionStart,
                rollingLimitYear,
                appliedRollingYears,
                payrollPeriod);
        OptionalInt reformRollingYear = rollingSimulation.pendingYear();
        boolean eligible = isLevelPromotionPosition(history.positionCode())
                && supportsGradePromotion(projectionState)
                && payrollRepository.intValue(history.gradeSalaryLevel()) > 1
                && calculationYear >= 2007;
        boolean reformLevelRollingDue = eligible && projectionStart.eligible() && reformRollingYear.isPresent();
        boolean normalLevelDue = eligible
                && !reformLevelRollingDue
                && levelSpanExceedsFiveYears
                && qualifiedYearsForLevel == 5
                && missingLevelAssessmentYears.isEmpty();
        boolean levelPromotionDue = normalLevelDue || reformLevelRollingDue;
        boolean stepPromotionDue = eligible && qualifiedYearsForStep >= 2;
        String currentStep = history.positionSalaryGrade();
        String currentStepDifference = history.gradeSalaryStep();
        String promotedLevel = history.gradeSalaryLevel();
        String promotedStep = currentStep;
        String promotedStepDifference = currentStepDifference;
        Integer promotedGradeSalary = currentGradeSalary;
        if (eligible && reformLevelRollingDue) {
            WageProjectionState stateBeforeRolling = rollingSimulation.stateBeforePendingRolling();
            String previousLevel = stateBeforeRolling.level();
            String previousStep = stateBeforeRolling.stepOrSalaryLevel();
            int currentSalary = gradeSalaryAmount(
                    stateBeforeRolling, previousLevel, previousStep, stateBeforeRolling.salaryStandardYearMonth());
            promotedLevel = cappedPromotedLevel(
                    stateBeforeRolling.positionCode(),
                    String.valueOf(Math.max(1, payrollRepository.intValue(previousLevel) - 1)));
            promotedStep = firstHigherGradeStep(promotedLevel, currentSalary, stateBeforeRolling.salaryStandardYearMonth());
            promotedStepDifference = "0";
            promotedGradeSalary = payrollRepository.civilServantGradeSalary(
                    promotedLevel, promotedStep, promotedStepDifference, stateBeforeRolling.salaryStandardYearMonth());
        } else if (eligible && normalLevelDue) {
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
        String gradeIncreasePreviousLevel = reformLevelRollingDue && rollingSimulation.stateBeforePendingRolling() != null
                ? rollingSimulation.stateBeforePendingRolling().level()
                : history.gradeSalaryLevel();
        String gradeIncreasePreviousStep = reformLevelRollingDue && rollingSimulation.stateBeforePendingRolling() != null
                ? rollingSimulation.stateBeforePendingRolling().stepOrSalaryLevel()
                : currentStep;
        String gradeIncreaseStandardYearMonth = reformLevelRollingDue && rollingSimulation.stateBeforePendingRolling() != null
                ? rollingSimulation.stateBeforePendingRolling().salaryStandardYearMonth()
                : history.salaryStandardYearMonth();
        boolean gradeIncreaseExceedsStepDifference = eligible && levelPromotionDue
                && gradeIncreaseExceedsStepDifference(
                gradeIncreasePreviousLevel,
                gradeIncreasePreviousStep,
                promotedLevel,
                gradeIncreaseStandardYearMonth);
        String promotionEffectYear = reformLevelRollingDue
                ? String.valueOf(reformRollingYear.getAsInt())
                : String.valueOf(promotionYear);
        String nextLevelAssessmentStartYear = levelPromotionDue ? promotionEffectYear : String.valueOf(levelStartYear);
        String nextStepAssessmentStartYear = stepPromotionDue || gradeIncreaseExceedsStepDifference
                ? promotionEffectYear
                : String.valueOf(stepStartYear);
        boolean rollbackEligible = isLevelPromotionProcessedInYear(history, promotionYear);
        boolean applyEligible = eligible && levelPromotionDue && !rollbackEligible;
        boolean levelPromotionHintOnly = eligible
                && !rollbackEligible
                && !applyEligible
                && levelSpanExceedsFiveYears
                && (qualifiedYearsForLevel > 5
                        || (qualifiedYearsForLevel == 5 && !missingLevelAssessmentYears.isEmpty()));
        String note = composeLevelPromotionNote(
                levelPromotionNote(
                        eligible,
                        reformLevelRollingDue,
                        normalLevelDue,
                        stepPromotionDue,
                        gradeIncreaseExceedsStepDifference,
                        levelSpanExceedsFiveYears,
                        qualifiedYearsForLevel),
                missingLevelAssessmentYears);
        if (rollbackEligible) {
            String conditionNote = rollbackEligibleLevelPromotionNote(
                    eligible,
                    reformLevelRollingDue,
                    normalLevelDue,
                    stepPromotionDue,
                    gradeIncreaseExceedsStepDifference,
                    levelSpanExceedsFiveYears,
                    qualifiedYearsForLevel,
                    missingLevelAssessmentYears);
            note = "当前最近工资变动为「" + history.calculationType() + "」，可执行还原"
                    + (conditionNote.isBlank() ? "。" : "；" + conditionNote);
        }
        return new LevelPromotionPreview(
                uid,
                history.id(),
                history.organizationCode(),
                history.personCode(),
                history.name(),
                String.format("%04d01", calculationYear),
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
                reformLevelRollingDue,
                gradeIncreaseExceedsStepDifference,
                currentGradeSalary,
                promotedGradeSalary,
                nullToZero(promotedGradeSalary) - nullToZero(currentGradeSalary),
                eligible,
                rollbackEligible,
                applyEligible,
                false,
                levelPromotionHintOnly,
                note);
    }

    private LevelPromotionPreview levelPromotionPreview(int uid) {
        PayrollHistorySnapshot history = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        return levelPromotionPreview(uid, yearOf(history.calculationYear()));
    }

    private PositionChangePreviewCache loadPositionChangePreviewCache(List<PositionChangePromotionCandidateRow> candidateRows) {
        if (candidateRows == null || candidateRows.isEmpty()) {
            return PositionChangePreviewCache.empty(payrollRepository);
        }
        List<Integer> uids = candidateRows.stream().map(PositionChangePromotionCandidateRow::uid).distinct().toList();
        Map<Integer, PositionChangePromotionCandidateRow> rowsByUid = candidateRows.stream()
                .collect(Collectors.toMap(
                        PositionChangePromotionCandidateRow::uid,
                        row -> row,
                        (left, right) -> left,
                        LinkedHashMap::new));
        Map<Integer, PayrollHistorySnapshot> histories = payrollRepository.findLatestHistoriesByUids(uids);
        Map<Integer, PositionChangeDisplayPair> processedDisplays = new LinkedHashMap<>();
        rowsByUid.forEach((uid, row) -> {
            if (row.beforePositionName() != null && !row.beforePositionName().isBlank()) {
                processedDisplays.put(uid, new PositionChangeDisplayPair(
                        row.beforePositionCode(),
                        row.beforePositionName(),
                        row.afterPositionCode(),
                        row.afterPositionName()));
            }
        });
        List<Integer> missingDisplayUids = uids.stream()
                .filter(uid -> {
                    PositionChangeDisplayPair pair = processedDisplays.get(uid);
                    return pair == null || pair.beforePositionName() == null || pair.beforePositionName().isBlank();
                })
                .toList();
        if (!missingDisplayUids.isEmpty()) {
            processedDisplays.putAll(payrollRepository.findProcessedPositionChangeDisplaysByUids(missingDisplayUids));
            List<String> missingDisplayHistoryIds = histories.entrySet().stream()
                    .filter(entry -> missingDisplayUids.contains(entry.getKey()))
                    .filter(entry -> isPositionChangeAuditType(entry.getValue().calculationType()))
                    .filter(entry -> {
                        PositionChangeDisplayPair pair = processedDisplays.get(entry.getKey());
                        return pair == null || pair.beforePositionName() == null || pair.beforePositionName().isBlank();
                    })
                    .map(entry -> entry.getValue().id())
                    .toList();
            if (!missingDisplayHistoryIds.isEmpty()) {
                Map<String, PositionChangeDisplayPair> byHistoryId =
                        payrollRepository.findProcessedPositionChangeDisplaysByHistoryIds(missingDisplayHistoryIds);
                histories.forEach((uid, history) -> {
                    PositionChangeDisplayPair pair = byHistoryId.get(history.id());
                    if (pair != null) {
                        processedDisplays.putIfAbsent(uid, pair);
                    }
                });
            }
        }
        Map<Integer, PositionChangeCandidate> candidates =
                payrollRepository.findCurrentPositionChangeCandidatesByUids(uids);
        Set<String> positionCodes = new LinkedHashSet<>();
        Set<String> yearMonths = new LinkedHashSet<>();
        histories.values().forEach(history -> {
            yearMonths.add(history.salaryStandardYearMonth());
            positionCodes.add(history.positionCode());
        });
        rowsByUid.values().forEach(row -> {
            positionCodes.add(row.beforePositionCode());
            positionCodes.add(row.afterPositionCode());
        });
        processedDisplays.values().forEach(pair -> {
            positionCodes.add(pair.beforePositionCode());
            positionCodes.add(pair.afterPositionCode());
        });
        candidates.values().forEach(candidate -> positionCodes.add(candidate.positionCode()));
        Map<String, PositionLevelRange> levelRanges = payrollRepository.findPositionLevelRanges(positionCodes);
        Map<String, Integer> positionSalaries = new HashMap<>();
        for (String yearMonth : yearMonths) {
            if (yearMonth == null || yearMonth.isBlank()) {
                continue;
            }
            payrollRepository.findPositionSalaries(yearMonth, positionCodes).forEach((code, amount) ->
                    positionSalaries.put(PositionChangePreviewCache.positionSalaryKey(code, yearMonth), amount));
        }
        return new PositionChangePreviewCache(
                payrollRepository,
                histories,
                processedDisplays,
                candidates,
                levelRanges,
                positionSalaries,
                rowsByUid);
    }

    private PositionChangePromotionListItem positionChangePromotionListItem(int uid, PositionChangePreviewCache cache) {
        PayrollHistorySnapshot history = cache.requireHistory(uid);
        PositionChangeTrialContext trialContext = resolvePositionChangeTrialContext(history, uid, cache);
        if (trialContext.processed()) {
            return processedPositionChangeListItem(history, trialContext, cache);
        }
        return PositionChangePromotionListItem.fromPreview(
                positionChangePromotionPreview(uid, cache, false, false));
    }

    private PositionChangePromotionListItem processedPositionChangeListItem(
            PayrollHistorySnapshot history,
            PositionChangeTrialContext trialContext,
            PositionChangePreviewCache cache) {
        String beforePositionCode = trialContext.beforePositionCode();
        String afterPositionCode = trialContext.afterPositionCode();
        String currentStep = String.valueOf(
                payrollRepository.intValue(history.positionSalaryGrade())
                        + payrollRepository.intValue(history.gradeSalaryStep()));
        Integer currentPositionSalary = cache.positionSalary(beforePositionCode, history.salaryStandardYearMonth());
        Integer newPositionSalary = nullToZero(history.storedPositionSalary()) > 0
                ? history.storedPositionSalary()
                : cache.positionSalary(afterPositionCode, history.salaryStandardYearMonth());
        Integer currentGradeSalary = cache.gradeSalary(history.gradeSalaryLevel(), currentStep, history.salaryStandardYearMonth());
        Integer promotedGradeSalary = nullToZero(history.storedGradeSalary()) > 0
                ? history.storedGradeSalary()
                : currentGradeSalary;
        int positionSalaryIncrease = nullToZero(newPositionSalary) - nullToZero(currentPositionSalary);
        int gradeSalaryIncrease = nullToZero(promotedGradeSalary) - nullToZero(currentGradeSalary);
        int netPositionSalaryIncrease = positionSalaryIncrease;
        return new PositionChangePromotionListItem(
                history.id(),
                history.organizationCode(),
                history.personCode(),
                history.name(),
                beforePositionCode,
                trialContext.beforePositionName(),
                afterPositionCode,
                trialContext.afterPositionName(),
                history.calculationType(),
                history.positionStartYearMonth(),
                nextMonth(history.positionStartYearMonth()),
                history.gradeSalaryLevel(),
                currentStep,
                history.gradeSalaryLevel(),
                currentStep,
                positionSalaryIncrease,
                netPositionSalaryIncrease,
                gradeSalaryIncrease,
                netPositionSalaryIncrease + gradeSalaryIncrease,
                history.calculationType(),
                true,
                false,
                false);
    }

    private PositionChangePreviewCache loadPositionChangePreviewCacheForUids(List<Integer> uids) {
        if (uids == null || uids.isEmpty()) {
            return PositionChangePreviewCache.empty(payrollRepository);
        }
        Map<Integer, PayrollHistorySnapshot> histories = payrollRepository.findLatestHistoriesByUids(uids);
        Map<Integer, PositionChangeDisplayPair> processedDisplays =
                new LinkedHashMap<>(payrollRepository.findProcessedPositionChangeDisplaysByUids(uids));
        List<String> missingDisplayHistoryIds = histories.entrySet().stream()
                .filter(entry -> isPositionChangeAuditType(entry.getValue().calculationType()))
                .filter(entry -> {
                    PositionChangeDisplayPair pair = processedDisplays.get(entry.getKey());
                    return pair == null || pair.beforePositionName() == null || pair.beforePositionName().isBlank();
                })
                .map(entry -> entry.getValue().id())
                .toList();
        if (!missingDisplayHistoryIds.isEmpty()) {
            Map<String, PositionChangeDisplayPair> byHistoryId =
                    payrollRepository.findProcessedPositionChangeDisplaysByHistoryIds(missingDisplayHistoryIds);
            histories.forEach((uid, history) -> {
                PositionChangeDisplayPair pair = byHistoryId.get(history.id());
                if (pair != null) {
                    processedDisplays.putIfAbsent(uid, pair);
                }
            });
        }
        Map<Integer, PositionChangeCandidate> candidates =
                payrollRepository.findCurrentPositionChangeCandidatesByUids(uids);
        Set<String> positionCodes = new LinkedHashSet<>();
        Set<String> yearMonths = new LinkedHashSet<>();
        histories.values().forEach(history -> {
            yearMonths.add(history.salaryStandardYearMonth());
            positionCodes.add(history.positionCode());
        });
        processedDisplays.values().forEach(pair -> {
            positionCodes.add(pair.beforePositionCode());
            positionCodes.add(pair.afterPositionCode());
        });
        candidates.values().forEach(candidate -> positionCodes.add(candidate.positionCode()));
        Map<String, PositionLevelRange> levelRanges = payrollRepository.findPositionLevelRanges(positionCodes);
        Map<String, Integer> positionSalaries = new HashMap<>();
        for (String yearMonth : yearMonths) {
            if (yearMonth == null || yearMonth.isBlank()) {
                continue;
            }
            payrollRepository.findPositionSalaries(yearMonth, positionCodes).forEach((code, amount) ->
                    positionSalaries.put(PositionChangePreviewCache.positionSalaryKey(code, yearMonth), amount));
        }
        return new PositionChangePreviewCache(
                payrollRepository,
                histories,
                processedDisplays,
                candidates,
                levelRanges,
                positionSalaries,
                Map.of());
    }

    private static final class PositionChangePreviewCache {
        private final PayrollRepository repository;
        private final Map<Integer, PayrollHistorySnapshot> histories;
        private final Map<Integer, PositionChangeDisplayPair> processedDisplays;
        private final Map<Integer, PositionChangeCandidate> candidates;
        private final Map<String, PositionLevelRange> levelRanges;
        private final Map<String, Integer> positionSalaries;
        private final Map<Integer, PositionChangePromotionCandidateRow> candidateRows;
        private final Map<String, Integer> gradeSalaries = new HashMap<>();

        private PositionChangePreviewCache(
                PayrollRepository repository,
                Map<Integer, PayrollHistorySnapshot> histories,
                Map<Integer, PositionChangeDisplayPair> processedDisplays,
                Map<Integer, PositionChangeCandidate> candidates,
                Map<String, PositionLevelRange> levelRanges,
                Map<String, Integer> positionSalaries,
                Map<Integer, PositionChangePromotionCandidateRow> candidateRows) {
            this.repository = repository;
            this.histories = histories == null ? Map.of() : histories;
            this.processedDisplays = processedDisplays == null ? Map.of() : processedDisplays;
            this.candidates = candidates == null ? Map.of() : candidates;
            this.levelRanges = levelRanges == null ? Map.of() : levelRanges;
            this.positionSalaries = positionSalaries == null ? Map.of() : positionSalaries;
            this.candidateRows = candidateRows == null ? Map.of() : candidateRows;
        }

        static PositionChangePreviewCache empty(PayrollRepository repository) {
            return new PositionChangePreviewCache(repository, Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
        }

        static String positionSalaryKey(String positionCode, String standardYearMonth) {
            return positionCode + "\u0001" + standardYearMonth;
        }

        private static String gradeSalaryKey(String gradeLevel, String gradeStep, String standardYearMonth) {
            return gradeLevel + "\u0001" + gradeStep + "\u0001" + standardYearMonth;
        }

        PayrollHistorySnapshot requireHistory(int uid) {
            PayrollHistorySnapshot history = histories.get(uid);
            if (history != null) {
                return history;
            }
            return repository.findLatestHistory(uid)
                    .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
        }

        Optional<PositionChangePromotionCandidateRow> candidateRow(int uid) {
            return Optional.ofNullable(candidateRows.get(uid));
        }

        Optional<PositionChangeDisplayPair> processedDisplay(int uid, PayrollHistorySnapshot history) {
            PositionChangeDisplayPair display = processedDisplays.get(uid);
            if (display != null) {
                return Optional.of(display);
            }
            return repository.findProcessedPositionChangeDisplay(history.organizationCode(), history.personCode());
        }

        Optional<PositionChangeDisplayPair> processedDisplayById(int uid, String historyId) {
            PositionChangeDisplayPair display = processedDisplays.get(uid);
            if (display != null && display.beforePositionName() != null && !display.beforePositionName().isBlank()) {
                return Optional.of(display);
            }
            return repository.findProcessedPositionChangeDisplayById(historyId);
        }

        PositionChangeCandidate appointmentCandidate(int uid, PayrollHistorySnapshot history) {
            PositionChangeCandidate candidate = candidates.get(uid);
            if (candidate != null) {
                return candidate;
            }
            return repository.findCurrentPositionChangeCandidate(history.organizationCode(), history.personCode())
                    .orElse(new PositionChangeCandidate(
                            history.positionCode(),
                            history.positionName(),
                            history.positionStartYearMonth()));
        }

        Optional<PositionLevelRange> levelRange(String positionCode) {
            if (positionCode == null || positionCode.isBlank()) {
                return Optional.empty();
            }
            PositionLevelRange cached = levelRanges.get(positionCode);
            if (cached != null) {
                return Optional.of(cached);
            }
            return repository.findPositionLevelRange(positionCode);
        }

        int positionSalary(String positionCode, String standardYearMonth) {
            if (positionCode == null || positionCode.isBlank()) {
                return 0;
            }
            Integer cached = positionSalaries.get(positionSalaryKey(positionCode, standardYearMonth));
            if (cached != null) {
                return cached;
            }
            return repository.positionSalary(positionCode, standardYearMonth);
        }

        int gradeSalary(String gradeLevel, String gradeStep, String standardYearMonth) {
            return gradeSalaries.computeIfAbsent(
                    gradeSalaryKey(gradeLevel, gradeStep, standardYearMonth),
                    ignored -> repository.gradeSalary(gradeLevel, gradeStep, standardYearMonth));
        }
    }

    private record PositionChangeTrialContext(
            String beforePositionCode,
            String beforePositionName,
            String afterPositionCode,
            String afterPositionName,
            PositionChangeCandidate appointmentCandidate,
            boolean processed) {
    }

    private PositionChangeTrialContext resolvePositionChangeTrialContext(
            PayrollHistorySnapshot history,
            int uid,
            PositionChangePreviewCache cache) {
        PositionChangeCandidate appointment = cache.appointmentCandidate(uid, history);
        Optional<PositionChangePromotionCandidateRow> sqlRow = cache.candidateRow(uid);
        if (sqlRow.isPresent()) {
            PositionChangePromotionCandidateRow row = sqlRow.get();
            if (row.beforePositionName() != null && !row.beforePositionName().isBlank()) {
                return new PositionChangeTrialContext(
                        row.beforePositionCode(),
                        row.beforePositionName(),
                        row.afterPositionCode(),
                        row.afterPositionName(),
                        appointment,
                        row.processed());
            }
        }
        if (!isPositionChangeAuditType(history.calculationType())) {
            return new PositionChangeTrialContext(
                    history.positionCode(),
                    history.positionName(),
                    appointment.positionCode(),
                    appointment.positionName(),
                    appointment,
                    false);
        }
        Optional<PositionChangeDisplayPair> processedDisplay = cache.processedDisplay(uid, history);
        if (processedDisplay.isEmpty()) {
            processedDisplay = cache.processedDisplayById(uid, history.id());
        }
        if (processedDisplay.isPresent()
                && processedDisplay.get().beforePositionName() != null
                && !processedDisplay.get().beforePositionName().isBlank()) {
            PositionChangeDisplayPair pair = processedDisplay.get();
            return new PositionChangeTrialContext(
                    pair.beforePositionCode(),
                    pair.beforePositionName(),
                    pair.afterPositionCode(),
                    pair.afterPositionName(),
                    appointment,
                    true);
        }
        String beforeCode = history.positionCode();
        String beforeName = history.positionName();
        Optional<PayrollHistorySnapshot> predecessor = payrollRepository.findPredecessorHistoryId(history.id())
                .flatMap(payrollRepository::findPayrollHistoryById);
        if (predecessor.isEmpty()) {
            predecessor = payrollRepository.findPositionChangePredecessor(history.id());
        }
        if (predecessor.isPresent()) {
            beforeCode = predecessor.get().positionCode();
            beforeName = predecessor.get().positionName();
        } else {
            Optional<Map<String, Object>> predecessorValues = payrollRepository.findPredecessorHistoryValues(history.id());
            if (predecessorValues.isPresent()) {
                beforeCode = textValue(predecessorValues.get(), "zwbm2");
                beforeName = textValue(predecessorValues.get(), "zwgw2");
            } else {
                Optional<PositionChangeCandidate> priorAppointment = payrollRepository.findPreviousDistinctAppointment(
                        history.organizationCode(),
                        history.personCode(),
                        history.positionCode(),
                        history.calculationYear() + history.calculationMonth());
                if (priorAppointment.isPresent()) {
                    beforeCode = priorAppointment.get().positionCode();
                    beforeName = priorAppointment.get().positionName();
                }
            }
        }
        String afterCode = history.positionCode();
        String afterName = history.positionName();
        if (Objects.equals(beforeCode, afterCode)
                && !Objects.equals(appointment.positionCode(), history.positionCode())) {
            return new PositionChangeTrialContext(
                    history.positionCode(),
                    history.positionName(),
                    appointment.positionCode(),
                    appointment.positionName(),
                    appointment,
                    false);
        }
        return new PositionChangeTrialContext(
                beforeCode,
                beforeName,
                afterCode,
                afterName,
                appointment,
                true);
    }

    private PositionChangePromotionPreview processedPositionChangePromotionPreview(
            PayrollHistorySnapshot history,
            PositionChangeTrialContext trialContext,
            PositionChangePreviewCache cache,
            boolean includeExplanation,
            boolean includeNote) {
        String beforePositionCode = trialContext.beforePositionCode();
        String beforePositionName = trialContext.beforePositionName();
        String afterPositionCode = trialContext.afterPositionCode();
        String afterPositionName = trialContext.afterPositionName();
        PositionChangeCandidate candidate = new PositionChangeCandidate(
                afterPositionCode,
                afterPositionName,
                history.positionStartYearMonth());
        PositionLevelRange levelRange = cache.levelRange(afterPositionCode).orElse(null);
        String currentPositionPrefix = positionPrefix(beforePositionCode);
        String newPositionPrefix = positionPrefix(afterPositionCode);
        boolean sequenceConversion = isSequenceConversion(currentPositionPrefix, newPositionPrefix);
        boolean policeOfficerConversion = isPoliceOfficerConversion(currentPositionPrefix, newPositionPrefix);
        boolean judicialConversion = isJudicialConversion(currentPositionPrefix, newPositionPrefix);
        boolean rankConversion = isRankConversion(currentPositionPrefix, newPositionPrefix);
        boolean rankHighPositionPromotion = rankConversion && isHigherPositionLayer(beforePositionCode, afterPositionCode);
        boolean institutionPositionChange = isInstitutionPosition(currentPositionPrefix) && isInstitutionPosition(newPositionPrefix);
        boolean policeToAdministrativeConversion = isPoliceToAdministrativeConversion(currentPositionPrefix, newPositionPrefix);
        String lastPayrollChangeType = history.calculationType();
        String changeType = lastPayrollChangeType;
        String currentStep = String.valueOf(
                payrollRepository.intValue(history.positionSalaryGrade())
                        + payrollRepository.intValue(history.gradeSalaryStep()));
        Integer currentPositionSalary = cache.positionSalary(beforePositionCode, history.salaryStandardYearMonth());
        Integer newPositionSalary = nullToZero(history.storedPositionSalary()) > 0
                ? history.storedPositionSalary()
                : cache.positionSalary(afterPositionCode, history.salaryStandardYearMonth());
        Integer currentGradeSalary = cache.gradeSalary(history.gradeSalaryLevel(), currentStep, history.salaryStandardYearMonth());
        String promotedLevel = history.gradeSalaryLevel();
        String promotedStep = currentStep;
        Integer promotedGradeSalary = nullToZero(history.storedGradeSalary()) > 0
                ? history.storedGradeSalary()
                : currentGradeSalary;
        int positionSalaryIncrease = nullToZero(newPositionSalary) - nullToZero(currentPositionSalary);
        int pgbcRetainedAmount = rankConversion && positionSalaryIncrease < 0 ? Math.abs(positionSalaryIncrease) : 0;
        int pgbcOffsetAmount = 0;
        int netPositionSalaryIncrease = positionSalaryIncrease + pgbcRetainedAmount - pgbcOffsetAmount;
        int gradeSalaryIncrease = nullToZero(promotedGradeSalary) - nullToZero(currentGradeSalary);
        boolean rollbackEligible = true;
        boolean applyEligible = false;
        boolean eligible = false;
        String note = includeNote
                ? "当前最近工资变动为「" + lastPayrollChangeType + "」，可执行还原；"
                + positionChangePromotionNote(
                        history,
                        candidate,
                        levelRange,
                        eligible,
                        0,
                        false,
                        sequenceConversion,
                        policeOfficerConversion,
                        judicialConversion,
                        rankConversion,
                        null,
                        null,
                        null,
                        null,
                        null,
                        true)
                : "";
        List<String> explanationLines = includeExplanation
                ? positionChangeExplanationLines(
                history,
                beforePositionCode,
                beforePositionName,
                afterPositionCode,
                afterPositionName,
                candidate.startYearMonth(),
                changeType,
                currentStep,
                levelRange,
                null,
                null,
                null,
                null,
                null,
                promotedLevel,
                promotedStep,
                history.levelAssessmentStartYear(),
                history.stepAssessmentStartYear(),
                positionSalaryIncrease,
                pgbcRetainedAmount,
                pgbcOffsetAmount,
                netPositionSalaryIncrease,
                gradeSalaryIncrease,
                false,
                note)
                : List.of();
        return new PositionChangePromotionPreview(
                history.id(),
                history.organizationCode(),
                history.personCode(),
                history.name(),
                beforePositionCode,
                beforePositionName,
                afterPositionCode,
                afterPositionName,
                currentPositionPrefix,
                newPositionPrefix,
                sequenceConversion,
                policeOfficerConversion,
                judicialConversion,
                rankConversion,
                rankHighPositionPromotion,
                institutionPositionChange,
                null,
                null,
                null,
                changeType,
                candidate.startYearMonth(),
                nextMonth(candidate.startYearMonth()),
                history.salaryStandardYearMonth(),
                history.gradeSalaryLevel(),
                currentStep,
                levelRange == null ? null : String.valueOf(levelRange.minimumLevel()),
                levelRange == null ? null : String.valueOf(levelRange.maximumLevel()),
                null,
                null,
                null,
                null,
                policeToAdministrativeConversion,
                null,
                null,
                null,
                null,
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
                history.levelAssessmentStartYear(),
                history.stepAssessmentStartYear(),
                history.levelAssessmentStartYear(),
                history.stepAssessmentStartYear(),
                false,
                lastPayrollChangeType,
                rollbackEligible,
                applyEligible,
                eligible,
                note,
                explanationLines);
    }

    private PositionChangePromotionPreview positionChangePromotionPreview(int uid) {
        return positionChangePromotionPreview(uid, loadPositionChangePreviewCacheForUids(List.of(uid)), true, true);
    }

    private PositionChangePromotionPreview positionChangePromotionPreview(
            int uid,
            PositionChangePreviewCache cache,
            boolean includeExplanation,
            boolean includeNote) {
        PayrollHistorySnapshot history = cache.requireHistory(uid);
        PositionChangeTrialContext trialContext = resolvePositionChangeTrialContext(history, uid, cache);
        if (trialContext.processed()) {
            return processedPositionChangePromotionPreview(history, trialContext, cache, includeExplanation, includeNote);
        }
        String beforePositionCode = trialContext.beforePositionCode();
        String beforePositionName = trialContext.beforePositionName();
        String afterPositionCode = trialContext.afterPositionCode();
        String afterPositionName = trialContext.afterPositionName();
        PositionChangeCandidate candidate = trialContext.appointmentCandidate();
        PositionLevelRange levelRange = cache.levelRange(afterPositionCode).orElse(null);
        String currentPositionPrefix = positionPrefix(beforePositionCode);
        String newPositionPrefix = positionPrefix(afterPositionCode);
        boolean sequenceConversion = isSequenceConversion(currentPositionPrefix, newPositionPrefix);
        boolean policeOfficerConversion = isPoliceOfficerConversion(currentPositionPrefix, newPositionPrefix);
        boolean judicialConversion = isJudicialConversion(currentPositionPrefix, newPositionPrefix);
        boolean rankConversion = isRankConversion(currentPositionPrefix, newPositionPrefix);
        boolean rankHighPositionPromotion = rankConversion && isHigherPositionLayer(beforePositionCode, afterPositionCode);
        boolean institutionPositionChange = isInstitutionPosition(currentPositionPrefix) && isInstitutionPosition(newPositionPrefix);
        String lastPayrollChangeType = history.calculationType();
        String changeType = positionChangeType(
                beforePositionCode,
                afterPositionCode,
                sequenceConversion,
                policeOfficerConversion,
                judicialConversion,
                rankConversion,
                institutionPositionChange);
        int currentLevel = payrollRepository.intValue(history.gradeSalaryLevel());
        String currentStep = String.valueOf(
                payrollRepository.intValue(history.positionSalaryGrade())
                        + payrollRepository.intValue(history.gradeSalaryStep()));
        Integer currentPositionSalary = cache.positionSalary(beforePositionCode, history.salaryStandardYearMonth());
        Integer newPositionSalary = cache.positionSalary(afterPositionCode, history.salaryStandardYearMonth());
        Integer currentGradeSalary = cache.gradeSalary(history.gradeSalaryLevel(), currentStep, history.salaryStandardYearMonth());
        PoliceOfficerConversionResult policeOfficerResult = policeOfficerConversion
                ? policeOfficerConversionResult(history, candidate, levelRange, currentLevel, currentStep, currentGradeSalary)
                : null;
        RankConversionResult rankConversionResult = rankConversion
                ? rankConversionResult(history, candidate, levelRange, currentLevel, currentStep, currentGradeSalary, rankHighPositionPromotion, cache)
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
        boolean pendingPositionChange = !trialContext.processed()
                && !Objects.equals(trialContext.beforePositionCode(), trialContext.afterPositionCode());
        boolean sameSequenceEligible = pendingPositionChange
                && !sequenceConversion
                && isCivilServantForPositionChange(beforePositionCode)
                && isCivilServantForPositionChange(afterPositionCode)
                && levelRange != null
                && currentLevel > 0;
        boolean eligible = pendingPositionChange && (
                (policeOfficerResult != null && policeOfficerResult.eligible())
                || (rankConversionResult != null && rankConversionResult.eligible())
                || (institutionResult != null && institutionResult.eligible())
                || (judicialConversion && judicialConversionStep != null && !judicialConversionStep.isBlank())
                || (administrativeReplayResult != null && administrativeReplayResult.eligible())
                || sameSequenceEligible);
        String promotedLevel = history.gradeSalaryLevel();
        if (sameSequenceEligible) {
            if (currentLevel > levelRange.minimumLevel()) {
                promotedLevel = String.valueOf(levelRange.minimumLevel());
            } else if (isHigherPositionLayer(beforePositionCode, afterPositionCode) && currentLevel >= levelRange.maximumLevel()) {
                promotedLevel = String.valueOf(Math.max(1, currentLevel - 1));
            }
        }
        String promotedStep = currentStep;
        Integer promotedGradeSalary = currentGradeSalary;
        if (sameSequenceEligible && !promotedLevel.equals(history.gradeSalaryLevel())) {
            promotedStep = firstHigherGradeStep(promotedLevel, currentGradeSalary, history.salaryStandardYearMonth(), cache);
            promotedGradeSalary = cache.gradeSalary(promotedLevel, promotedStep, history.salaryStandardYearMonth());
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
            promotedGradeSalary = cache.gradeSalary(promotedLevel, promotedStep, history.salaryStandardYearMonth());
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
        boolean rollbackEligible = false;
        boolean applyEligible = pendingPositionChange && eligible;
        String note = includeNote
                ? buildPendingPositionChangeNote(
                history,
                lastPayrollChangeType,
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
                policeOfficerResult,
                pendingPositionChange,
                applyEligible)
                : "";
        List<String> explanationLines = includeExplanation
                ? positionChangeExplanationLines(
                history,
                beforePositionCode,
                beforePositionName,
                afterPositionCode,
                afterPositionName,
                candidate.startYearMonth(),
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
                note)
                : List.of();
        return new PositionChangePromotionPreview(
                history.id(),
                history.organizationCode(),
                history.personCode(),
                history.name(),
                beforePositionCode,
                beforePositionName,
                afterPositionCode,
                afterPositionName,
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
                history.levelAssessmentStartYear(),
                history.stepAssessmentStartYear(),
                nextLevelAssessmentStartYear,
                nextStepAssessmentStartYear,
                gradeIncreaseExceedsStepDifference,
                lastPayrollChangeType,
                rollbackEligible,
                applyEligible,
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
                resolution.note(),
                eligible,
                false);
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
                regularizationNote(history, education, standard, institutionRegularization, resolved.salaryPositionFromStandard()),
                eligible,
                false);
    }

    private Optional<PositionChangeCandidate> findRegularizationAppointedPosition(
            String organizationCode,
            String personCode,
            String regularizationPeriod) {
        return findRegularizationAppointedPosition(organizationCode, personCode, regularizationPeriod, false);
    }

    /**
     * @param exactPeriodOnly true 时仅取转正当月任职（无则视为无转正任职，不用更早任职回填）
     */
    private Optional<PositionChangeCandidate> findRegularizationAppointedPosition(
            String organizationCode,
            String personCode,
            String regularizationPeriod,
            boolean exactPeriodOnly) {
        String period = normalizeYearMonth(regularizationPeriod);
        if (period.isBlank()) {
            return Optional.empty();
        }
        Optional<PositionChangeCandidate> exact = payrollRepository.findPositionAtPeriod(organizationCode, personCode, period);
        if (exact.isPresent() || exactPeriodOnly) {
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
        PositionChangeCandidate appointed = findRegularizationAppointedPosition(
                organizationCode, personCode, regularizationPeriod, true)
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
        return wageReformSelection(latest, currentStandard, reformYears, "", null);
    }

    private WageReformSelection wageReformSelection(
            PayrollHistorySnapshot latest,
            WageReformStandard currentStandard,
            int reformYears,
            RegularizationSalaryPosition regularizationPositionForFloor) {
        return wageReformSelection(latest, currentStandard, reformYears, "", regularizationPositionForFloor);
    }

    private List<WageReformPosition> resolveWageReformPositionsForSelection(
            PayrollHistorySnapshot latest,
            String regularizationYearMonth) {
        ArrayList<WageReformPosition> positions = new ArrayList<>(payrollRepository.findWageReformPositionsBefore(
                latest.organizationCode(),
                latest.personCode(),
                "200607",
                WAGE_REFORM_POSITION_PREFIXES));
        if (positions.size() < 2) {
            payrollRepository.findStoredWageReformSnapshot(latest.organizationCode(), latest.personCode())
                    .flatMap(StoredWageReformSnapshot::lowerPosition)
                    .filter(lower -> hasUsablePreReformPositionCode(lower.positionCode()))
                    .filter(lower -> positions.isEmpty() || !lower.positionCode().equals(positions.getFirst().positionCode()))
                    .ifPresent(positions::add);
        }
        if (positions.size() < 2
                && !regularizationYearMonth.isBlank()
                && regularizationYearMonth.compareTo("200607") < 0) {
            WageReformPosition current = positions.isEmpty() ? null : positions.getFirst();
            findRegularizationAppointedPosition(
                            latest.organizationCode(), latest.personCode(), regularizationYearMonth)
                    .filter(appointed -> current == null || !appointed.positionCode().equals(current.positionCode()))
                    .filter(appointed -> hasUsablePreReformPositionCode(appointed.positionCode()))
                    .ifPresent(appointed -> positions.add(new WageReformPosition(
                            appointed.positionCode(),
                            appointed.positionName(),
                            appointed.startYearMonth(),
                            0)));
        }
        return positions;
    }

    private WageReformSelection wageReformSelection(
            PayrollHistorySnapshot latest,
            WageReformStandard currentStandard,
            int reformYears,
            String regularizationYearMonth,
            RegularizationSalaryPosition regularizationPositionForFloor) {
        List<WageReformPosition> positions = resolveWageReformPositionsForSelection(latest, regularizationYearMonth);
        String currentPositionName = positionNameForProjectionStart(positions.stream()
                .findFirst()
                .map(position -> new PositionChangeCandidate(position.positionCode(), position.positionName(), position.startYearMonth())),
                currentStandard.positionCode());
        if (positions.size() < 2 || !"GRADE".equals(baseSalarySource(currentStandard.positionCode()))) {
            WageReformSelection selection = WageReformSelection.fromCurrent(currentStandard, currentPositionName, "");
            return applyEducationRegularizationFloor(
                    latest, selection, regularizationYearMonth, regularizationPositionForFloor);
        }
        WageReformPosition currentPosition = positions.get(0);
        WageReformPosition lowerPosition = positions.get(1);
        int lowerAppointmentYears = wageReformAppointmentYears(lowerPosition);
        Optional<WageReformStandard> lowerStandard = lookupWageReformStandard(
                lowerPosition.positionCode(), lowerAppointmentYears, reformYears);
        if (lowerStandard.isEmpty()) {
            WageReformSelection selection = WageReformSelection.fromCurrent(
                    currentStandard,
                    currentPosition.positionName(),
                    "；原任低一职务 "
                            + positionDisplay(lowerPosition.positionCode(), lowerPosition.positionName())
                            + "（任职 " + formatYearMonth(lowerPosition.startYearMonth()) + "）未找到 2006 套改标准");
            return applyEducationRegularizationFloor(
                    latest, selection, regularizationYearMonth, regularizationPositionForFloor);
        }
        int currentLevel = payrollRepository.intValue(currentStandard.convertedLevel());
        int lowerLevel = payrollRepository.intValue(lowerStandard.get().convertedLevel());
        int currentSalary = payrollRepository.gradeSalary(currentStandard.convertedLevel(), currentStandard.convertedStep(), "200607");
        int lowerSalary = payrollRepository.gradeSalary(lowerStandard.get().convertedLevel(), lowerStandard.get().convertedStep(), "200607");
        if (currentLevel >= lowerLevel && lowerLevel > 1) {
            String promotedLevel = String.valueOf(lowerLevel - 1);
            String promotedStep = firstHigherGradeStep(promotedLevel, lowerSalary, "200607");
            WageReformSelection selection = new WageReformSelection(
                    currentStandard.positionCode(),
                    currentPosition.positionName(),
                    promotedLevel,
                    promotedStep,
                    "；现任职务套改级别低于或等于原任低一职务 "
                            + positionDisplay(lowerPosition.positionCode(), lowerPosition.positionName())
                            + "（任职 " + formatYearMonth(lowerPosition.startYearMonth())
                            + "，套改为 " + levelStepDisplay("GRADE", lowerStandard.get().convertedLevel(), lowerStandard.get().convertedStep())
                            + "），按原任低一职务合并任职年限套改后高套一级");
            return applyEducationRegularizationFloor(
                    latest, selection, regularizationYearMonth, regularizationPositionForFloor);
        }
        if (currentLevel < lowerLevel
                && payrollRepository.intValue(currentStandard.convertedStep()) < payrollRepository.intValue(lowerStandard.get().convertedStep())
                && currentSalary < lowerSalary) {
            String promotedStep = firstHigherGradeStep(currentStandard.convertedLevel(), lowerSalary, "200607");
            WageReformSelection selection = new WageReformSelection(
                    currentStandard.positionCode(),
                    currentPosition.positionName(),
                    currentStandard.convertedLevel(),
                    promotedStep,
                    "；现任职务级别较高但工资额低于原任低一职务 "
                            + positionDisplay(lowerPosition.positionCode(), lowerPosition.positionName())
                            + "（任职 " + formatYearMonth(lowerPosition.startYearMonth())
                            + "，套改为 " + levelStepDisplay("GRADE", lowerStandard.get().convertedLevel(), lowerStandard.get().convertedStep())
                            + "），按原任低一职务工资额就近就高套入现任职务级别");
            return applyEducationRegularizationFloor(
                    latest, selection, regularizationYearMonth, regularizationPositionForFloor);
        }
        WageReformSelection selection = WageReformSelection.fromCurrent(
                currentStandard,
                currentPosition.positionName(),
                lowerPositionComparisonNote(lowerPosition, lowerStandard.get(), currentStandard));
        return applyEducationRegularizationFloor(
                latest, selection, regularizationYearMonth, regularizationPositionForFloor);
    }

    private String lowerPositionComparisonNote(
            WageReformPosition lowerPosition,
            WageReformStandard lowerStandard,
            WageReformStandard currentStandard) {
        return "；已比照原任低一职务 "
                + positionDisplay(lowerPosition.positionCode(), lowerPosition.positionName())
                + "（任职 " + formatYearMonth(lowerPosition.startYearMonth())
                + "，套改为 " + levelStepDisplay("GRADE", lowerStandard.convertedLevel(), lowerStandard.convertedStep())
                + "），现任职务套改为 "
                + levelStepDisplay("GRADE", currentStandard.convertedLevel(), currentStandard.convertedStep())
                + "，不作低一职务套改调整";
    }

    private WageReformSelection applyEducationRegularizationFloor(PayrollHistorySnapshot latest, WageReformSelection selection) {
        return applyEducationRegularizationFloor(latest, selection, "", null);
    }

    private WageReformSelection applyEducationRegularizationFloor(
            PayrollHistorySnapshot latest,
            WageReformSelection selection,
            RegularizationSalaryPosition regularizationPositionForFloor) {
        return applyEducationRegularizationFloor(latest, selection, "", regularizationPositionForFloor);
    }

    private WageReformSelection applyEducationRegularizationFloor(
            PayrollHistorySnapshot latest,
            WageReformSelection selection,
            String regularizationYearMonth,
            RegularizationSalaryPosition regularizationPositionForFloor) {
        if (!"GRADE".equals(baseSalarySource(selection.positionCode()))) {
            return selection;
        }
        EducationRegularizationStandard standard = null;
        EducationPromotionSource education = null;
        if (regularizationPositionForFloor != null
                && regularizationPositionForFloor.standard() != null
                && !regularizationPositionForFloor.institutionRegularization()) {
            standard = regularizationPositionForFloor.standard();
        }
        if (standard == null) {
            education = findEducationForRegularization(
                    latest.organizationCode(),
                    latest.personCode(),
                    regularizationYearMonth,
                    regularizationYearMonth);
            if (education == null) {
                education = payrollRepository.findStoredWageReformSnapshot(
                                latest.organizationCode(), latest.personCode())
                        .flatMap(StoredWageReformSnapshot::education)
                        .orElse(null);
            }
            standard = findEducationRegularizationStandard(selection.positionCode(), education);
        }
        if (standard == null) {
            if (education != null) {
                return selection.withNoteSuffix("；套改时学历为 "
                        + emptyToDash(education.educationName())
                        + "，未找到对应转正定级标准");
            }
            return selection;
        }
        if (education == null && regularizationPositionForFloor != null && regularizationPositionForFloor.standard() != null) {
            education = new EducationPromotionSource(
                    regularizationPositionForFloor.standard().educationCode(),
                    regularizationPositionForFloor.standard().educationName(),
                    regularizationYearMonth);
        }
        int reformLevel = payrollRepository.intValue(selection.level());
        int regularizationLevel = payrollRepository.intValue(standard.gradeLevel());
        int reformSalary = payrollRepository.civilServantGradeSalary(selection.level(), selection.step(), "0", "200607");
        int regularizationSalary = payrollRepository.civilServantGradeSalary(
                standard.gradeLevel(), standard.gradeStep(), "0", "200607");
        if (reformLevel <= 0 || regularizationLevel <= 0 || reformSalary <= 0 || regularizationSalary <= 0) {
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
            if (reformSalary >= regularizationSalary) {
                if (education != null) {
                    return selection.withNoteSuffix(educationComparisonNote(education, standard));
                }
                return selection;
            }
            String adjustedStep = firstHigherGradeStepAtOrAbove(
                    selection.level(), selection.step(), regularizationSalary, "200607");
            return selection.withLevelStep(
                    selection.level(),
                    adjustedStep,
                    "；套改级别高于相同学历新参加工作人员定级级别但级别工资额较低，按定级工资额就近就高套入套改级别");
        }
        if (reformSalary < regularizationSalary) {
            String adjustedStep = firstHigherGradeStepAtOrAbove(
                    selection.level(), selection.step(), regularizationSalary, "200607");
            return selection.withLevelStep(
                    selection.level(),
                    adjustedStep,
                    "；套改级别与相同学历新参加工作人员定级级别相同但级别工资额较低，按定级工资额就近就高套入套改级别");
        }
        if (education != null) {
            return selection.withNoteSuffix(educationComparisonNote(education, standard));
        }
        return selection;
    }

    private String educationComparisonNote(
            EducationPromotionSource education,
            EducationRegularizationStandard standard) {
        return "；已比照学历 "
                + emptyToDash(education.educationName())
                + " 转正定级标准 "
                + levelStepDisplay("GRADE", standard.gradeLevel(), standard.gradeStep())
                + "，套改结果不低于该标准";
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

    private boolean isInstitutionPersonnel(PayrollHistorySnapshot latest) {
        return resolveInstitutionPersonnelPosition(latest, "").isPresent();
    }

    /**
     * 事业人员以任职表为准：当前工资历史 zwbm2 可能仍保留旧行政职务，需按目标/当前年月任职判断。
     */
    private Optional<PositionChangeCandidate> resolveInstitutionPersonnelPosition(
            PayrollHistorySnapshot latest,
            String referencePeriod) {
        if (latest == null) {
            return Optional.empty();
        }
        String period = referencePeriod == null || referencePeriod.isBlank()
                ? projectionPeriod(latest.calculationYear() + latest.calculationMonth())
                : projectionPeriod(referencePeriod);
        if (!period.isBlank()) {
            Optional<PositionChangeCandidate> appointed = payrollRepository.findPositionAtOrBefore(
                    latest.organizationCode(), latest.personCode(), period);
            if (appointed.isPresent() && isInstitutionPosition(appointed.get().positionCode())) {
                return appointed;
            }
        }
        if (isInstitutionPosition(latest.positionCode())) {
            return Optional.of(new PositionChangeCandidate(
                    latest.positionCode(),
                    latest.positionName(),
                    latest.positionStartYearMonth()));
        }
        String lookupPeriod = period.isBlank() ? "999912" : period;
        return payrollRepository.findLatestPositionBefore(
                latest.organizationCode(),
                latest.personCode(),
                lookupPeriod,
                INSTITUTION_POSITION_PREFIXES)
                .filter(position -> isInstitutionPosition(position.positionCode()));
    }

    private WageProjectionStart resolveInstitutionWageProjectionStart(
            PayrollHistorySnapshot latest,
            String regularization,
            Optional<PositionChangeCandidate> probationAtReform) {
        Optional<PositionChangeCandidate> institutionPosition = resolveInstitutionPersonnelPosition(latest, regularization);
        if (probationAtReform.isPresent()) {
            return wageProjectionStartForProbationAtReform(latest, probationAtReform.get(), regularization);
        }
        if (!regularization.isBlank() && regularization.compareTo("200607") < 0) {
            Optional<PositionChangeCandidate> institutionAtReform = resolveInstitutionPersonnelPosition(latest, "200607");
            if (institutionAtReform.isPresent()) {
                String positionCode = institutionAtReform.get().positionCode();
                String positionName = institutionAtReform.get().positionName();
                int appointmentYears = Math.max(1, 2006 - yearOf(institutionAtReform.get().startYearMonth()) + 1);
                int reformYears = payrollRepository.calculatedWageReformYears(latest.organizationCode(), latest.personCode());
                if (reformYears <= 0) {
                    reformYears = Math.max(1, 2006 - yearOf(latest.workStartYearMonth()) + 1 - nullToZero(latest.interruptedSalaryYears()));
                }
                Optional<WageReformStandard> standard = lookupWageReformStandard(positionCode, appointmentYears, reformYears);
                if (standard.isEmpty()) {
                    return WageProjectionStart.ineligible("2006.07 前已转正事业人员，但未能按事业岗位匹配 2006 套改薪级。");
                }
                String positionStartYearMonth = institutionAtReform.get().startYearMonth();
                String note = "2006.07 前已转正事业人员，按 200607 前最近事业岗位 "
                        + positionDisplay(positionCode, positionName)
                        + " 及套改年限 " + reformYears + " 年确定起点薪级 "
                        + standard.get().convertedStep() + " 级（忽略行政职务任职）。";
                return new WageProjectionStart(
                        true,
                        "200607",
                        positionCode,
                        positionName,
                        "",
                        standard.get().convertedStep(),
                        "2006",
                        "2006",
                        positionStartYearMonth,
                        reformYears,
                        note);
            }
            Optional<PositionChangeCandidate> currentInstitution = resolveInstitutionPersonnelPosition(latest, "");
            if (currentInstitution.isPresent()) {
                String institutionStart = normalizeYearMonth(currentInstitution.get().startYearMonth());
                if (!institutionStart.isBlank() && institutionStart.compareTo("200607") > 0) {
                    return resolveInstitutionStartFromFirstAppointment(latest, currentInstitution.get());
                }
            }
            return WageProjectionStart.ineligible("2006.07 前已转正事业人员，但 200607 前未找到事业岗位任职，无法确定套改起点（忽略行政职务）。");
        }
        if (regularization.isBlank()) {
            return WageProjectionStart.ineligible("未找到转正年月，无法从转正定级规则确定事业人员起点。");
        }
        String fallbackPositionCode = institutionPosition
                .map(PositionChangeCandidate::positionCode)
                .orElse("");
        RegularizationSalaryPosition resolved = resolveRegularizationSalaryPosition(
                latest.organizationCode(),
                latest.personCode(),
                regularization,
                fallbackPositionCode,
                regularization);
        EducationRegularizationStandard standard = resolved.standard();
        if (standard == null) {
            return WageProjectionStart.ineligible("2006.07 及以后转正事业人员，但未能按学历转正定级标准确定起点。");
        }
        String positionCode = resolved.salaryPositionCode();
        String positionName = resolved.salaryPositionName();
        PositionChangeCandidate appointed = findRegularizationAppointedPosition(
                latest.organizationCode(), latest.personCode(), regularization, true)
                .filter(position -> isInstitutionPosition(position.positionCode()))
                .orElse(null);
        String positionStartYearMonth = appointed == null || !isRegularizationAppointedPosition(appointed)
                ? regularization
                : appointed.startYearMonth();
        String note = resolved.salaryPositionFromStandard()
                ? "2006.07 及以后转正事业人员，转正时间 " + formatYearMonth(regularization)
                + "，未找到事业岗位转正任职记录，按转正定级标准确认岗位 "
                + positionDisplay(positionCode, positionName)
                + "，薪级 " + standard.gradeStep() + " 级。"
                : "2006.07 及以后转正事业人员，转正时间 " + formatYearMonth(regularization)
                + "，按学历转正定级标准确定起点：岗位 " + positionDisplay(positionCode, positionName)
                + "，薪级 " + standard.gradeStep() + " 级。";
        return new WageProjectionStart(
                true,
                regularization,
                positionCode,
                positionName,
                "",
                standard.gradeStep(),
                String.valueOf(yearOf(regularization)),
                String.valueOf(yearOf(regularization)),
                positionStartYearMonth,
                0,
                note);
    }

    private WageProjectionStart resolveInstitutionStartFromFirstAppointment(
            PayrollHistorySnapshot latest,
            PositionChangeCandidate institutionAppointment) {
        String positionCode = institutionAppointment.positionCode();
        String positionName = institutionAppointment.positionName();
        String startPeriod = normalizeYearMonth(institutionAppointment.startYearMonth());
        if (startPeriod.isBlank()) {
            return WageProjectionStart.ineligible("事业人员进入事业岗位时间无效，无法确定推算起点。");
        }
        String salaryLevel = emptyToNull(latest.positionSalaryGrade()) == null
                ? "0"
                : latest.positionSalaryGrade();
        String note = "事业人员，" + formatYearMonth(startPeriod) + " 进入事业岗位 "
                + positionDisplay(positionCode, positionName)
                + "，以岗位薪级 " + salaryLevel + " 级为推算起点（忽略此前行政职务及任职）。";
        return new WageProjectionStart(
                true,
                startPeriod,
                positionCode,
                positionName,
                "",
                salaryLevel,
                String.valueOf(yearOf(startPeriod)),
                String.valueOf(yearOf(startPeriod)),
                startPeriod,
                0,
                note);
    }

    private WageProjectionStart wageProjectionStart(PayrollHistorySnapshot latest) {
        String regularization = normalizeYearMonth(payrollRepository.findRegularizationYearMonth(latest.organizationCode(), latest.personCode()));
        Optional<PositionChangeCandidate> probationAtReform = resolveProbationPositionForWageReform(latest, regularization);
        if (isInstitutionPersonnel(latest)) {
            return resolveInstitutionWageProjectionStart(latest, regularization, probationAtReform);
        }
        if (probationAtReform.isPresent()) {
            return wageProjectionStartForProbationAtReform(latest, probationAtReform.get(), regularization);
        }
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
                    latest, standard.get(), reformYears, regularization, regularizationPositionForFloor);
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
        PositionChangeCandidate appointed = findRegularizationAppointedPosition(
                latest.organizationCode(), latest.personCode(), regularization, true)
                .orElse(null);
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

    /**
     * 转正在套改之后且不晚于该月：套改月仍可按试用期工资（如 200609、200707 转正）。
     * 须参加工作早于 200607 且套改前无公务员正式任职。
     */
    private static final String PROBATION_REGULARIZATION_AFTER_REFORM_UNTIL = "200801";

    /**
     * 转正在套改之后：取 200607 前最近见习任职套改（如 200609 任职不参与套改）。
     * 任职表无见习行时：若转正晚于套改且无套改前正式任职，仍按试用期套改（如转正 200609、200707）。
     * 转正不晚于套改月：仅当 200607 当月仍为见习岗时按试用期工资套改。
     */
    private Optional<PositionChangeCandidate> resolveProbationPositionForWageReform(
            PayrollHistorySnapshot latest,
            String regularization) {
        String organizationCode = latest.organizationCode();
        String personCode = latest.personCode();
        if (!regularization.isBlank() && regularization.compareTo(WAGE_REFORM_SALARY_STANDARD) > 0) {
            boolean noAppointmentAtRegularization = findRegularizationAppointedPosition(
                    organizationCode, personCode, regularization, true)
                    .filter(this::isRegularizationAppointedPosition)
                    .isEmpty();
            Optional<PositionChangeCandidate> internBeforeReform = payrollRepository.findLatestInternPositionBefore(
                    organizationCode, personCode, WAGE_REFORM_SALARY_STANDARD);
            if (internBeforeReform.isPresent()) {
                return internBeforeReform;
            }
            if (noAppointmentAtRegularization && wasProbationAtWageReform(latest)) {
                return Optional.of(probationFallbackPositionAtWageReform(latest));
            }
            if (shouldUseProbationSalaryAtWageReform(latest, regularization)) {
                return Optional.of(probationFallbackPositionAtWageReform(latest));
            }
            return Optional.empty();
        }
        return payrollRepository.findPositionAtOrBefore(organizationCode, personCode, WAGE_REFORM_SALARY_STANDARD)
                .filter(position -> isProbationAppointment(position.positionCode(), position.positionName()));
    }

    private boolean isProbationAppointment(String positionCode, String positionName) {
        if (isInternSalaryPosition(positionCode)) {
            return true;
        }
        if (positionName == null || positionName.isBlank()) {
            return false;
        }
        return positionName.contains("见习") || positionName.contains("试用");
    }

    private boolean shouldUseProbationSalaryAtWageReform(PayrollHistorySnapshot latest, String regularization) {
        if (regularization.isBlank()
                || regularization.compareTo(WAGE_REFORM_SALARY_STANDARD) <= 0
                || regularization.compareTo(PROBATION_REGULARIZATION_AFTER_REFORM_UNTIL) > 0) {
            return false;
        }
        String workStart = normalizeYearMonth(latest.workStartYearMonth());
        if (workStart.isBlank() || workStart.compareTo(WAGE_REFORM_SALARY_STANDARD) >= 0) {
            return false;
        }
        return !hasUsablePreReformCivilPositionBeforeReform(
                latest.organizationCode(), latest.personCode(), regularization);
    }

    private boolean hasUsablePreReformCivilPositionBeforeReform(String organizationCode, String personCode) {
        return hasUsablePreReformCivilPositionBeforeReform(
                organizationCode, personCode, WAGE_REFORM_SALARY_STANDARD);
    }

    private boolean hasUsablePreReformCivilPositionBeforeReform(
            String organizationCode,
            String personCode,
            String beforePeriod) {
        String boundary = normalizeYearMonth(beforePeriod);
        if (boundary.isBlank()) {
            boundary = WAGE_REFORM_SALARY_STANDARD;
        }
        return payrollRepository.findLatestPositionBefore(
                        organizationCode,
                        personCode,
                        boundary,
                        WAGE_REFORM_POSITION_PREFIXES)
                .map(PositionChangeCandidate::positionCode)
                .filter(code -> !isProbationAppointment(code, null))
                .isPresent();
    }

    /** 200607 套改当月工资历史或任职仍为见习/试用。 */
    private boolean wasProbationAtWageReform(PayrollHistorySnapshot latest) {
        String organizationCode = latest.organizationCode();
        String personCode = latest.personCode();
        boolean probationReformHistory = payrollRepository.findHistoryChain(organizationCode, personCode).stream()
                .anyMatch(record -> "套改".equals(emptyToNull(record.calculationType()))
                        && WAGE_REFORM_SALARY_STANDARD.equals(historyCalculationPeriod(record))
                        && isProbationAppointment(record.positionCode(), record.positionName()));
        if (probationReformHistory) {
            return true;
        }
        return payrollRepository.findPositionAtOrBefore(organizationCode, personCode, WAGE_REFORM_SALARY_STANDARD)
                .map(position -> isProbationAppointment(position.positionCode(), position.positionName()))
                .orElse(false);
    }

    private PositionChangeCandidate probationFallbackPositionAtWageReform(PayrollHistorySnapshot latest) {
        return payrollRepository.findHistoryChain(latest.organizationCode(), latest.personCode()).stream()
                .filter(record -> "套改".equals(emptyToNull(record.calculationType()))
                        && WAGE_REFORM_SALARY_STANDARD.equals(historyCalculationPeriod(record))
                        && isProbationAppointment(record.positionCode(), record.positionName()))
                .findFirst()
                .map(record -> new PositionChangeCandidate(
                        record.positionCode(),
                        record.positionName(),
                        record.positionStartYearMonth()))
                .orElseGet(() -> {
                    if (isProbationAppointment(latest.positionCode(), latest.positionName())) {
                        return new PositionChangeCandidate(
                                latest.positionCode(),
                                latest.positionName(),
                                latest.positionStartYearMonth());
                    }
                    String start = emptyToNull(latest.workStartYearMonth());
                    return new PositionChangeCandidate(
                            "01FF",
                            "见习期",
                            start != null ? start : WAGE_REFORM_SALARY_STANDARD);
                });
    }

    private WageProjectionStart wageProjectionStartForProbationAtReform(
            PayrollHistorySnapshot latest,
            PositionChangeCandidate probationPosition,
            String regularization) {
        EducationPromotionSource education = findEducationForRegularization(
                latest.organizationCode(),
                latest.personCode(),
                regularization,
                WAGE_REFORM_SALARY_STANDARD);
        Optional<InternSalaryStandard> internStandard = payrollRepository.findInternSalaryStandard(
                probationPosition.positionCode(),
                education == null ? null : education.educationCode(),
                education == null ? null : education.educationName(),
                WAGE_REFORM_SALARY_STANDARD);
        if (internStandard.isEmpty() || internStandard.get().firstYearAmount() <= 0) {
            return WageProjectionStart.ineligible("2006.07 套改时仍处试用期，但未能按学历匹配 bz06_zzdz 见习工资标准。");
        }
        int reformYears = payrollRepository.calculatedWageReformYears(latest.organizationCode(), latest.personCode());
        if (reformYears <= 0) {
            reformYears = Math.max(1, 2006 - yearOf(latest.workStartYearMonth()) + 1 - nullToZero(latest.interruptedSalaryYears()));
        }
        String regularizationNote = regularization.isBlank()
                ? ""
                : "，转正时间 " + formatYearMonth(regularization);
        String note = "2006.07 套改时仍处试用期，按 bz06_zzdz 见习工资标准确定起点：职务 "
                + positionDisplay(probationPosition.positionCode(), probationPosition.positionName())
                + "，试用期工资 " + internStandard.get().firstYearAmount()
                + "（学历 " + emptyToDash(internStandard.get().educationName()) + regularizationNote + "）"
                + "；套改年限 " + reformYears + " 年，待转正后再按转正定级标准接续推算。";
        return new WageProjectionStart(
                true,
                WAGE_REFORM_SALARY_STANDARD,
                probationPosition.positionCode(),
                probationPosition.positionName(),
                "0",
                "0",
                "2006",
                "2006",
                probationPosition.startYearMonth(),
                reformYears,
                note);
    }

    private int internSalaryAmount(
            PayrollHistorySnapshot latest,
            String positionCode,
            String standardYearMonth) {
        return internSalaryAmount(
                new WageProjectionState(
                        positionCode,
                        null,
                        "0",
                        "0",
                        "0",
                        null,
                        null,
                        "GRADE",
                        standardYearMonth,
                        null,
                        null,
                        null,
                        null,
                        null),
                latest,
                standardYearMonth);
    }

    private int internSalaryAmount(
            WageProjectionState state,
            PayrollHistorySnapshot latest,
            String lookupPeriod) {
        if (!isProbationAppointment(state.positionCode(), state.positionName())) {
            return 0;
        }
        String standardYearMonth = projectionPeriod(
                emptyToNull(state.salaryStandardYearMonth()) != null
                        ? state.salaryStandardYearMonth()
                        : lookupPeriod);
        if (standardYearMonth.isBlank()) {
            return 0;
        }
        String regularization = normalizeYearMonth(
                payrollRepository.findRegularizationYearMonth(latest.organizationCode(), latest.personCode()));
        EducationPromotionSource education = findEducationForRegularization(
                latest.organizationCode(),
                latest.personCode(),
                regularization,
                projectionPeriod(lookupPeriod).isBlank() ? standardYearMonth : lookupPeriod);
        return payrollRepository.findInternSalaryStandard(
                        state.positionCode(),
                        education == null ? null : education.educationCode(),
                        education == null ? null : education.educationName(),
                        standardYearMonth)
                .map(InternSalaryStandard::firstYearAmount)
                .orElse(0);
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
        boolean stepChanged = !promotedGradeStep.equals(currentGradeStep);
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
                        ? stepChanged
                                ? "级别未变，按定级标准调整档次，仅执行工资职务层次变化，档次考核年限沿用原起算年"
                                : "级别和档次均未变，仅执行工资职务层次变化，档次考核年限沿用原起算年"
                        : stepChanged
                                ? "级别未变，按定级标准调整档次，档次考核年限沿用原起算年"
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
                return standard.gradeStep();
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

    private boolean hasPendingPositionChange(PayrollHistorySnapshot history) {
        if (isPositionChangeAuditType(history.calculationType())) {
            return false;
        }
        return payrollRepository.findCurrentPositionChangeCandidate(history.organizationCode(), history.personCode())
                .isPresent();
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
            boolean highPositionPromotion,
            PositionChangePreviewCache cache) {
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
            promotedStep = firstHigherGradeStep(promotedLevel, currentGradeSalary, history.salaryStandardYearMonth(), cache);
        }
        return new RankConversionResult(
                true,
                promotedLevel,
                promotedStep,
                cache.gradeSalary(promotedLevel, promotedStep, history.salaryStandardYearMonth()));
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

        WageReformSelection withNoteSuffix(String extraNote) {
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
            boolean eligible,
            List<WageProjectionStepDetail> stepDetails) {
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

    private String buildPendingPositionChangeNote(
            PayrollHistorySnapshot history,
            String lastPayrollChangeType,
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
            PoliceOfficerConversionResult policeOfficerResult,
            boolean pendingPositionChange,
            boolean applyEligible) {
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
                policeOfficerResult,
                false);
        if (pendingPositionChange && !applyEligible) {
            note = "存在待处理职务变化，但当前试算条件不满足，暂不能办理；"
                    + (note == null || note.isBlank() ? "" : note);
        }
        return note;
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
            PoliceOfficerConversionResult policeOfficerResult,
            boolean processedPositionChange) {
        if (processedPositionChange) {
            if (administrativeReplayResult != null) {
                return "已完成综合管理类回放处理，可执行还原。";
            }
            if (policeOfficerConversion) {
                return "已完成警员套改处理，可执行还原。";
            }
            if (rankConversion) {
                return "已完成职级套改处理，可执行还原。";
            }
            if (institutionResult != null) {
                return "已完成事业岗位变动处理，可执行还原。";
            }
            if (judicialConversion) {
                return "已完成法检套改处理，可执行还原。";
            }
            if (sequenceConversion) {
                return "已完成转换序列处理，可执行还原。";
            }
            return "已完成「" + history.calculationType() + "」处理，可执行还原。";
        }
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
            return "识别为职级套改；从 01 前缀行政职务转为职级类序列，按职级套改规则处理。职务工资减少额保留到 PGBC，后续职务晋升时从职务工资增资额中冲减。";
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
        if (candidate.positionCode() == null || !hasPendingPositionChange(history)) {
            return "未发现不同于当前工资记录的新任职务，不参与职务变化晋升试算。";
        }
        if (sequenceConversion) {
            return "新旧职务前缀属于不同序列，识别为转换序列；不按同序列职务晋升级别规则试算。";
        }
        if (!eligible) {
            return "仅公务员/参公岗位且存在新任职务级别范围时参与职务变化晋升试算。";
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
            String beforePositionCode,
            String beforePositionName,
            String afterPositionCode,
            String afterPositionName,
            String appointmentStartYearMonth,
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
                + "，原职务 " + emptyToDash(beforePositionCode) + " " + emptyToDash(beforePositionName)
                + "，原级别/薪级 " + emptyToDash(history.gradeSalaryLevel()) + "/" + emptyToDash(currentStep) + "。");
        lines.add("新任职务：" + emptyToDash(afterPositionCode) + " " + emptyToDash(afterPositionName)
                + "，任职年月 " + emptyToDash(appointmentStartYearMonth)
                + "，执行年月 " + emptyToDash(nextMonth(appointmentStartYearMonth)) + "。");
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
        return firstHigherGradeStep(gradeLevel, currentGradeSalary, standardYearMonth, PositionChangePreviewCache.empty(payrollRepository));
    }

    private String firstHigherGradeStep(
            String gradeLevel,
            Integer currentGradeSalary,
            String standardYearMonth,
            PositionChangePreviewCache cache) {
        for (int step = 1; step <= 20; step++) {
            int amount = cache.gradeSalary(gradeLevel, String.valueOf(step), standardYearMonth);
            if (amount > nullToZero(currentGradeSalary)) {
                return String.valueOf(step);
            }
        }
        return "20";
    }

    private String firstHigherGradeStepAtOrAbove(
            String gradeLevel,
            String currentStep,
            Integer targetSalary,
            String standardYearMonth) {
        int minimumStep = Math.max(1, payrollRepository.intValue(currentStep));
        for (int step = minimumStep; step <= 20; step++) {
            int amount = payrollRepository.gradeSalary(gradeLevel, String.valueOf(step), standardYearMonth);
            if (amount >= nullToZero(targetSalary)) {
                return String.valueOf(step);
            }
        }
        return String.valueOf(Math.min(20, minimumStep));
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

    private boolean isLevelPromotionProcessedInYear(PayrollHistorySnapshot history, int promotionYear) {
        return history != null
                && LEVEL_PROMOTION_PROCESSED_CHANGE_TYPE.equals(history.calculationType())
                && yearOf(history.calculationYear()) == promotionYear;
    }

    private boolean isLevelPromotionListExcludedChangeType(String changeType) {
        return changeType != null && LEVEL_PROMOTION_LIST_EXCLUDED_CHANGE_TYPES.contains(changeType.trim());
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
            boolean reformLevelRollingDue,
            boolean normalLevelDue,
            boolean stepPromotionDue,
            boolean gradeIncreaseExceedsStepDifference,
            boolean levelSpanExceedsFiveYears,
            int qualifiedYearsForLevel) {
        if (!eligible) {
            return "当前岗位前缀或工资类型暂不参与级别晋升试算。";
        }
        if (reformLevelRollingDue && stepPromotionDue) {
            return gradeIncreaseExceedsStepDifference
                    ? "2007-2010套改后级别滚动，且同年满足晋升档次条件；级别滚动增资额超过下一级别一个档差，xckhndzw 从本次滚动年度重新计算。"
                    : "2007-2010套改后级别滚动，且同年满足晋升档次条件，已按先级别滚动、再晋升档次试算。";
        }
        if (reformLevelRollingDue) {
            return gradeIncreaseExceedsStepDifference
                    ? "2007-2010套改后级别滚动：上一年度考核称职及以上且达到套改表规定年限；级别滚动增资额超过下一级别一个档差，xckhndzw 从本次滚动年度重新计算。"
                    : "2007-2010套改后级别滚动：上一年度考核称职及以上且达到套改表规定年限，按就近就高晋升1个级别试算。";
        }
        if (normalLevelDue && stepPromotionDue) {
            return gradeIncreaseExceedsStepDifference
                    ? "级别考核起算年与晋升年度差额超过5年，且期间累计5年考核称职/合格及以上，按次年1月晋升1个级别试算；同年满足晋升档次条件，已按先晋升级别、再晋升档次试算；级别晋升增资额超过下一级别一个档差，xckhndzw 从本次晋升年度重新计算。"
                    : "级别考核起算年与晋升年度差额超过5年，且期间累计5年考核称职/合格及以上，按次年1月晋升1个级别试算；同年满足晋升档次条件，已按先晋升级别、再晋升档次试算。";
        }
        if (normalLevelDue) {
            return gradeIncreaseExceedsStepDifference
                    ? "级别考核起算年与晋升年度差额超过5年，且期间累计5年考核称职/合格及以上，按次年1月晋升1个级别试算；级别晋升增资额超过下一级别一个档差，xckhndzw 从本次晋升年度重新计算。"
                    : "级别考核起算年与晋升年度差额超过5年，且期间累计5年考核称职/合格及以上，按次年1月晋升1个级别试算；级别晋升增资额未超过下一级别一个档差，xckhndzw 沿用原起算年。";
        }
        if (levelSpanExceedsFiveYears && qualifiedYearsForLevel > 5) {
            return "考核称职/合格及以上年数已超过5年，上年已符合级别晋升条件。";
        }
        if (stepPromotionDue) {
            return "累计2年考核称职及以上，按晋升1个档次试算。";
        }
        if (!levelSpanExceedsFiveYears) {
            return "级别考核起算年与晋升年度差额未超过5年，暂不符合级别晋升条件。";
        }
        return "尚未满足级别晋升条件：级别考核起算年与晋升年度差额须超过5年，且期间考核称职/合格及以上须累计5年。";
    }

    private String rollbackEligibleLevelPromotionNote(
            boolean eligible,
            boolean reformLevelRollingDue,
            boolean normalLevelDue,
            boolean stepPromotionDue,
            boolean gradeIncreaseExceedsStepDifference,
            boolean levelSpanExceedsFiveYears,
            int qualifiedYearsForLevel,
            List<Integer> missingLevelAssessmentYears) {
        if (!reformLevelRollingDue && !normalLevelDue && !stepPromotionDue) {
            return "";
        }
        return composeLevelPromotionNote(
                levelPromotionNote(
                        eligible,
                        reformLevelRollingDue,
                        normalLevelDue,
                        stepPromotionDue,
                        gradeIncreaseExceedsStepDifference,
                        levelSpanExceedsFiveYears,
                        qualifiedYearsForLevel),
                List.of());
    }

    private String composeLevelPromotionNote(String baseNote, List<Integer> missingLevelAssessmentYears) {
        List<String> parts = new ArrayList<>();
        if (baseNote != null && !baseNote.isBlank()) {
            parts.add(baseNote);
        }
        if (!missingLevelAssessmentYears.isEmpty()) {
            parts.add("缺少 "
                    + missingLevelAssessmentYears.stream().map(String::valueOf).collect(Collectors.joining("、"))
                    + " 年度考核结果，请先补录后再办理级别晋升。");
        }
        return parts.isEmpty() ? "" : String.join("；", parts);
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

    private void requireStandardWritePermission() {
        if (!accessControlService.hasPermission("STANDARD_WRITE")) {
            throw new org.springframework.security.access.AccessDeniedException("STANDARD_WRITE permission required");
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
