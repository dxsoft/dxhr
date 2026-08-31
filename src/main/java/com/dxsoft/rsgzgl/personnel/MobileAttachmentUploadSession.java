package com.dxsoft.rsgzgl.personnel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record MobileAttachmentUploadSession(
        String token,
        String type,
        int uid,
        String recordId,
        String publicBaseUrl,
        Instant expiresAt,
        List<MobileAttachmentUploadFile> files
) {
    public MobileAttachmentUploadSession {
        files = files == null ? List.of() : List.copyOf(files);
    }

    MobileAttachmentUploadSession withFile(MobileAttachmentUploadFile file) {
        List<MobileAttachmentUploadFile> next = new ArrayList<>(files);
        next.add(file);
        return new MobileAttachmentUploadSession(token, type, uid, recordId, publicBaseUrl, expiresAt, next);
    }

    MobileAttachmentUploadSession withFiles(List<MobileAttachmentUploadFile> nextFiles) {
        return new MobileAttachmentUploadSession(token, type, uid, recordId, publicBaseUrl, expiresAt, nextFiles);
    }
}
