package com.dxsoft.rsgzgl.personnel;

public record PersonnelChangeRequest(
        String changeType,
        String effectivePeriod,
        String remark
) {
}
