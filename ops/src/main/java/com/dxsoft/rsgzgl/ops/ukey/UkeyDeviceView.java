package com.dxsoft.rsgzgl.ops.ukey;

import java.time.LocalDateTime;

public record UkeyDeviceView(
        Long id,
        String chipId,
        String sm2UserId,
        String pubkeyX,
        String pubkeyY,
        String encAlgoKey,
        String authModes,
        String username,
        String orgCode,
        String note,
        String status,
        LocalDateTime provisionedAt
) {
}
