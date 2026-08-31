package com.dxsoft.rsgzgl.personnel;

import java.util.List;

public record MobileAttachmentUploadNetworkHints(
        String suggestedBaseUrl,
        List<String> addresses,
        boolean autoDetected
) {
    public MobileAttachmentUploadNetworkHints {
        addresses = addresses == null ? List.of() : List.copyOf(addresses);
    }
}
