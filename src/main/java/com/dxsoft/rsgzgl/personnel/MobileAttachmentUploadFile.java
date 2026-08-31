package com.dxsoft.rsgzgl.personnel;

import java.time.Instant;

public record MobileAttachmentUploadFile(
        String id,
        String storedName,
        String originalName,
        String contentType,
        long size,
        Instant uploadedAt,
        boolean consumed
) {
    MobileAttachmentUploadFile markConsumed() {
        return new MobileAttachmentUploadFile(id, storedName, originalName, contentType, size, uploadedAt, true);
    }
}
