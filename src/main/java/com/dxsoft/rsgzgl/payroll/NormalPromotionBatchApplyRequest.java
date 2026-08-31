package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record NormalPromotionBatchApplyRequest(
        List<NormalPromotionBatchApplyItem> items
) {
    public record NormalPromotionBatchApplyItem(
            String payrollHistoryId,
            String organizationCode,
            String personCode,
            String calculationPeriod,
            String promotedGradeOrLevel,
            String gradeSalaryLevel,
            Integer promotedBaseSalary,
            Integer increaseAmount,
            String baseSalarySource
    ) {
        NormalPromotionApplyRequest toApplyRequest() {
            return new NormalPromotionApplyRequest(
                    organizationCode,
                    personCode,
                    calculationPeriod,
                    promotedGradeOrLevel,
                    gradeSalaryLevel,
                    promotedBaseSalary,
                    increaseAmount,
                    baseSalarySource);
        }
    }
}
