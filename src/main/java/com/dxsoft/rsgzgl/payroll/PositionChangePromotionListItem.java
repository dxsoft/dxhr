package com.dxsoft.rsgzgl.payroll;

public record PositionChangePromotionListItem(
        String payrollHistoryId,
        String organizationCode,
        String personCode,
        String name,
        String currentPositionCode,
        String currentPositionName,
        String newPositionCode,
        String newPositionName,
        String changeType,
        String positionStartYearMonth,
        String effectivePeriod,
        String currentLevel,
        String currentStep,
        String promotedLevel,
        String promotedStep,
        Integer positionSalaryIncrease,
        Integer netPositionSalaryIncrease,
        Integer gradeSalaryIncrease,
        Integer totalIncrease,
        String note,
        Boolean rollbackEligible,
        Boolean applyEligible,
        Boolean sequenceConversion
) {
    static PositionChangePromotionListItem fromPreview(PositionChangePromotionPreview preview) {
        return new PositionChangePromotionListItem(
                preview.payrollHistoryId(),
                preview.organizationCode(),
                preview.personCode(),
                preview.name(),
                preview.currentPositionCode(),
                preview.currentPositionName(),
                preview.newPositionCode(),
                preview.newPositionName(),
                preview.changeType(),
                preview.positionStartYearMonth(),
                preview.effectivePeriod(),
                preview.currentLevel(),
                preview.currentStep(),
                preview.promotedLevel(),
                preview.promotedStep(),
                preview.positionSalaryIncrease(),
                preview.netPositionSalaryIncrease(),
                preview.gradeSalaryIncrease(),
                preview.totalIncrease(),
                preview.note() == null || preview.note().isBlank() ? preview.changeType() : preview.note(),
                preview.rollbackEligible(),
                preview.applyEligible(),
                preview.sequenceConversion());
    }
}
