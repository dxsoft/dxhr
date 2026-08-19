package com.dxsoft.rsgzgl.personnel;

public record PersonnelChangeRequest(
        String changeType,
        String effectivePeriod,
        String remark,
        String targetOrganizationCode,
        String targetOrganizationName
) {
    public PersonnelChangeRequest(String changeType, String effectivePeriod, String remark) {
        this(changeType, effectivePeriod, remark, null, null);
    }
}
