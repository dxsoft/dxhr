package com.dxsoft.rsgzgl.payroll;

public record DisciplinaryDemotionPromotionListItem(
        String payrollHistoryId,
        int uid,
        String organizationCode,
        String organizationName,
        String personCode,
        String name,
        String currentPositionCode,
        String currentPositionName,
        String newPositionCode,
        String newPositionName,
        String positionStartYearMonth,
        String effectivePeriod,
        String currentLevel,
        String currentStep,
        String promotedLevel,
        String promotedStep,
        String positionChangeReason,
        Integer linkedAwardId,
        Integer totalIncrease,
        Boolean rollbackEligible,
        Boolean applyEligible,
        Boolean eligible,
        String note
) {
    static DisciplinaryDemotionPromotionListItem fromPreview(
            DisciplinaryDemotionPromotionPreview preview, int uid, String organizationName) {
        return new DisciplinaryDemotionPromotionListItem(
                preview.payrollHistoryId(),
                uid,
                preview.organizationCode(),
                organizationName,
                preview.personCode(),
                preview.name(),
                preview.currentPositionCode(),
                preview.currentPositionName(),
                preview.newPositionCode(),
                preview.newPositionName(),
                preview.positionStartYearMonth(),
                preview.effectivePeriod(),
                preview.currentLevel(),
                preview.currentStep(),
                preview.promotedLevel(),
                preview.promotedStep(),
                preview.positionChangeReason(),
                preview.linkedAwardId(),
                preview.totalIncrease(),
                preview.rollbackEligible(),
                preview.applyEligible(),
                preview.eligible(),
                preview.note());
    }
}
