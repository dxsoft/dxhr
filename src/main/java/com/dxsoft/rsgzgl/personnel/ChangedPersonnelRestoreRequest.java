package com.dxsoft.rsgzgl.personnel;

public record ChangedPersonnelRestoreRequest(
        String organizationCode,
        String personCode
) {
}
