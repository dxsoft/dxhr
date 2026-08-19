package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record RegularizationBatchApplyRequest(
        List<RegularizationBatchApplyItem> items
) {
    public record RegularizationBatchApplyItem(
            String payrollHistoryId,
            String organizationCode,
            String personCode,
            String calculationPeriod,
            String regularPositionCode,
            String regularPositionName,
            String regularLevel,
            String regularStep,
            Integer regularPositionSalary,
            Integer regularBaseSalary,
            Integer increaseAmount
    ) {
        RegularizationApplyRequest toApplyRequest() {
            return new RegularizationApplyRequest(
                    organizationCode,
                    personCode,
                    calculationPeriod,
                    regularPositionCode,
                    regularPositionName,
                    regularLevel,
                    regularStep,
                    regularPositionSalary,
                    regularBaseSalary,
                    increaseAmount);
        }
    }
}
