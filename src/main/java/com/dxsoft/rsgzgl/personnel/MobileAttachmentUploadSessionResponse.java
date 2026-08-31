package com.dxsoft.rsgzgl.personnel;

import java.time.Instant;

public record MobileAttachmentUploadSessionResponse(
        String token,
        String uploadUrl,
        Instant expiresAt
) {
}
