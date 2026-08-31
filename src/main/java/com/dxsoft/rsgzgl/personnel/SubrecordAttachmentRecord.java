package com.dxsoft.rsgzgl.personnel;

import java.time.LocalDateTime;

public record SubrecordAttachmentRecord(
        long id,
        String tableName,
        int recordId,
        String recordKey,
        String originalName,
        String contentType,
        long fileSize,
        String uploadedBy,
        LocalDateTime createdAt) {
}
