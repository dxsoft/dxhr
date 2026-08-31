package com.dxsoft.rsgzgl.payroll;

public record OrganizationPayrollPolicy(
        String positionChangeIncludeTechnicalGrade,
        String rankChangeIncludeTechnicalGrade,
        String positionChangeOctoberRule) {
}
