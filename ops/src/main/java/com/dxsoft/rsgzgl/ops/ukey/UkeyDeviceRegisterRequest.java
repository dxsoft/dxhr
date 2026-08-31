package com.dxsoft.rsgzgl.ops.ukey;

public record UkeyDeviceRegisterRequest(
        String chipId,
        String sm2UserId,
        String pubkeyX,
        String pubkeyY,
        String encAlgoKey,
        String authModes,
        String username,
        String orgCode,
        String note
) {
}
