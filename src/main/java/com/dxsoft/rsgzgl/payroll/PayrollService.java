package com.dxsoft.rsgzgl.payroll;

import com.dxsoft.rsgzgl.common.NotFoundException;
import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PayrollService {

    private final PayrollRepository payrollRepository;

    PayrollService(PayrollRepository payrollRepository) {
        this.payrollRepository = payrollRepository;
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

    public PayrollCalculationContext calculationContext(int uid) {
        PayrollHistorySnapshot history = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
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
        return new PayrollCalculationContext(
                uid,
                history,
                basicCalculation,
                allowanceCalculation,
                totalComparison(history, components, basicCalculation, allowanceCalculation),
                components,
                payrollRepository.findMatchedPositionStandards(history),
                payrollRepository.findMatchedAllowanceStandards(history));
    }

    public PageResponse<PayrollCalculationAudit> calculationAudits(String organizationCode, PageRequest pageRequest) {
        List<PayrollFieldMetadata> calculationFields = payrollRepository.findCalculationFields();
        List<PayrollCalculationAudit> audits = payrollRepository
                .findPersonnelUidsWithPayrollHistory(organizationCode, pageRequest)
                .stream()
                .map(uid -> calculationAudit(uid, calculationFields))
                .toList();
        return PageResponse.of(
                audits,
                pageRequest,
                payrollRepository.countPersonnelWithPayrollHistory(organizationCode));
    }

    public PayrollAuditSummary auditSummary(String organizationCode, PageRequest pageRequest) {
        List<PayrollFieldMetadata> calculationFields = payrollRepository.findCalculationFields();
        List<PayrollCalculationAudit> audits = payrollRepository
                .findPersonnelUidsWithPayrollHistory(organizationCode, pageRequest)
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
                payrollRepository.countPersonnelWithPayrollHistory(organizationCode),
                audits.size(),
                differences.size(),
                maxAbsoluteDifference,
                differences);
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
                payrollRepository.positionSalary(positionCode, standardYearMonth),
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

    private PayrollCalculationAudit calculationAudit(int uid, List<PayrollFieldMetadata> calculationFields) {
        PayrollHistorySnapshot history = payrollRepository.findLatestHistory(uid)
                .orElseThrow(() -> new NotFoundException("Payroll history not found for personnel record: " + uid));
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
        PayrollTotalComparison total = totalComparison(history, components, basicCalculation, allowanceCalculation);
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
        BigDecimal performanceAllowance = payrollRepository.performanceAllowance(
                history.organizationCode(),
                history.positionCode(),
                history.allowanceStandardYearMonth());
        int subsidyAllowance = payrollRepository.subsidyAllowance(
                history.positionCode(),
                history.allowanceStandardYearMonth());
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

    private PayrollTotalComparison totalComparison(
            PayrollHistorySnapshot history,
            List<PayrollComponentValue> components,
            BasicPayrollCalculation basic,
            AllowanceCalculation allowance) {
        Integer teachingAllowance = teachingAllowance(history);
        Integer salaryIncrease = salaryIncrease(history, basic);
        List<PayrollComponentDifference> componentDifferences = componentDifferences(
                history,
                basic,
                allowance,
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
            Integer teachingAllowance,
            Integer salaryIncrease) {
        List<PayrollComponentDifference> differences = new ArrayList<>();
        addDifference(differences, "ZWGZSE2", "职务工资", history.storedPositionSalary(), basic.positionSalary());
        addDifference(differences, "JBGZSE2", "级别/薪级工资", history.storedGradeSalary(), basic.selectedBaseSalary());
        addDifference(differences, "JSDJGZ2", "技术等级工资", history.storedTechnicalGradeSalary(), basic.technicalGradeSalary());
        addDifference(differences, "DFBT2", "基础性绩效工资", history.storedPerformanceAllowance(), allowance.performanceAllowance());
        addDifference(differences, "SDBT", "工作性/生活性补贴", history.storedSubsidyAllowance(), allowance.subsidyAllowance());
        addDifference(differences, "BLFB2", "保留福补", history.storedRetainedAllowance(), allowance.retainedAllowance());
        addDifference(differences, "NJBT", "年补贴", history.storedYearAllowance(), allowance.yearAllowance());
        addDifference(differences, "JHLJT", "教护龄津贴", history.storedTeachingAllowance(), teachingAllowance);
        addDifference(differences, "JSFSZWTG2", "提高工资", history.storedSalaryIncrease(), salaryIncrease);
        return differences;
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
}
