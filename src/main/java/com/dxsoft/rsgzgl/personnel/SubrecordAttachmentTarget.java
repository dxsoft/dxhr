package com.dxsoft.rsgzgl.personnel;

enum SubrecordAttachmentTarget {
    MAIN("dryjbxx"),
    EDUCATION("dxl"),
    POSITION("dryzwbh"),
    ASSESSMENT("dndkh"),
    AWARD("hjxx"),
    RANK("jx"),
    TRANSFER("app_personnel_transfer"),
    PAYROLL("hisbase");

    private final String tableName;

    SubrecordAttachmentTarget(String tableName) {
        this.tableName = tableName;
    }

    String tableName() {
        return tableName;
    }

    SubrecordAttachmentKey keyForInt(int recordId) {
        return SubrecordAttachmentKey.forIntRecord(tableName, recordId);
    }

    SubrecordAttachmentKey keyForString(String recordKey) {
        return SubrecordAttachmentKey.forStringRecord(tableName, recordKey);
    }
}
