package com.dxsoft.rsgzgl.payroll;

import java.util.List;

public record LevelPromotionBatchApplyRequest(
        List<LevelPromotionBatchApplyItem> items
) {
    public record LevelPromotionBatchApplyItem(
            String payrollHistoryId,
            String organizationCode,
            String personCode,
            Boolean reformLevelRollingDue,
            String calculationPeriod,
            String promotedLevel,
            String promotedStep,
            String nextLevelAssessmentStartYear,
            String nextStepAssessmentStartYear,
            Integer promotedGradeSalary,
            Integer increaseAmount
    ) {
        LevelPromotionApplyRequest toApplyRequest() {
            return new LevelPromotionApplyRequest(
                    organizationCode,
                    personCode,
                    reformLevelRollingDue,
                    calculationPeriod,
                    promotedLevel,
                    promotedStep,
                    nextLevelAssessmentStartYear,
                    nextStepAssessmentStartYear,
                    promotedGradeSalary,
                    increaseAmount);
        }
    }
}
