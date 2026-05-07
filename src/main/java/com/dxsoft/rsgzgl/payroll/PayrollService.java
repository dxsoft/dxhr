package com.dxsoft.rsgzgl.payroll;

import com.dxsoft.rsgzgl.common.NotFoundException;
import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
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

        return new PayrollCalculationContext(
                uid,
                history,
                basicCalculation(history),
                components,
                payrollRepository.findMatchedPositionStandards(history),
                payrollRepository.findMatchedAllowanceStandards(history));
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
}
