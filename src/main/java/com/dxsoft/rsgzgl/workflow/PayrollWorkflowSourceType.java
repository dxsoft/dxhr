package com.dxsoft.rsgzgl.workflow;

import com.dxsoft.rsgzgl.personnel.PersonnelSubrecordType;

public enum PayrollWorkflowSourceType {
    MAIN("人员基本信息"),
    EDUCATION("学历子记录"),
    POSITION("任职子记录"),
    ASSESSMENT("考核子记录"),
    AWARD("获奖子记录"),
    RANK("警衔子记录");

    private final String label;

    PayrollWorkflowSourceType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static PayrollWorkflowSourceType from(PersonnelSubrecordType type) {
        return switch (type) {
            case EDUCATION -> EDUCATION;
            case POSITION -> POSITION;
            case ASSESSMENT -> ASSESSMENT;
            case AWARD -> AWARD;
            case RANK -> RANK;
        };
    }
}
