package com.dxsoft.rsgzgl.payroll;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
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
}
