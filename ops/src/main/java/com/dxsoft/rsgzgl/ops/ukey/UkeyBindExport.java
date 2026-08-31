package com.dxsoft.rsgzgl.ops.ukey;

import java.util.List;

record UkeyBindExportDocument(
        String format,
        List<UkeyBindExportItem> devices
) {
    static final String FORMAT = "ukey-bind-v2";
}

record UkeyBindExportItem(
        String ukeyId,
        String sm2UserId,
        String sm2PubkeyX,
        String sm2PubkeyY,
        String encAlgoKey,
        String authModes,
        String username,
        String note,
        String orgCode
) {
}
