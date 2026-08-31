package com.dxsoft.rsgzgl.personnel;

public enum PersonnelSubrecordType {
    EDUCATION("dxl", "education"),
    POSITION("dryzwbh", "positions"),
    ASSESSMENT("dndkh", "assessments"),
    AWARD("hjxx", "awards"),
    RANK("jx", "ranks");

    private final String tableName;
    private final String apiSegment;

    PersonnelSubrecordType(String tableName, String apiSegment) {
        this.tableName = tableName;
        this.apiSegment = apiSegment;
    }

    public String tableName() {
        return tableName;
    }

    public String apiSegment() {
        return apiSegment;
    }
}
