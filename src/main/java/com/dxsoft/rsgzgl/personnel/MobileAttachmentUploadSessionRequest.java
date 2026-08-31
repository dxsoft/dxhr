package com.dxsoft.rsgzgl.personnel;

public record MobileAttachmentUploadSessionRequest(
        String type,
        int uid,
        String recordId,
        String publicBaseUrl
) {
}
