package com.dxsoft.rsgzgl.personnel;

record SubrecordAttachmentKey(String tableName, int recordId, String recordKey) {

    static SubrecordAttachmentKey forIntRecord(String tableName, int recordId) {
        return new SubrecordAttachmentKey(tableName, recordId, "");
    }

    static SubrecordAttachmentKey forStringRecord(String tableName, String recordKey) {
        return new SubrecordAttachmentKey(tableName, 0, recordKey == null ? "" : recordKey.trim());
    }
}
