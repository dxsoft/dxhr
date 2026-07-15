package com.dxsoft.rsgzgl.systemconfig;

public record SystemOptionUpdateRequest(
        String enterpriseTransferRaise,
        String gradeStepEducationLink,
        String decimalPlaces,
        String policeRankAllowance,
        String reformBonusBalance,
        String floatingSalary
) {
}
