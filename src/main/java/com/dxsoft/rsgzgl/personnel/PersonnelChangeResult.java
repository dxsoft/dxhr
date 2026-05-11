package com.dxsoft.rsgzgl.personnel;

public record PersonnelChangeResult(
        String organizationCode,
        String personCode,
        String name,
        String changeType,
        String message
) {
}
