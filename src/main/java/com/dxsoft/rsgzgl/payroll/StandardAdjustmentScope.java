package com.dxsoft.rsgzgl.payroll;

import java.util.Set;

public enum StandardAdjustmentScope {
    ALL("调标晋升", Set.of("调标晋升", "工资调标", "津补贴调标", "绩效标准调标"), null, null),
    /** 基本工资调标：写入/识别 jslb=调标晋升。 */
    BASIC("调标晋升", Set.of("调标晋升"), true, null),
    CIVIL_ALLOWANCE("津补贴调标", Set.of("津补贴调标"), null, true),
    PERFORMANCE("绩效标准调标", Set.of("绩效标准调标"), null, false);

    private final String changeType;
    private final Set<String> rollbackChangeTypes;
    private final Boolean civilServantPosition;
    private final Boolean institutionPosition;

    StandardAdjustmentScope(
            String changeType,
            Set<String> rollbackChangeTypes,
            Boolean civilServantPosition,
            Boolean institutionPosition) {
        this.changeType = changeType;
        this.rollbackChangeTypes = rollbackChangeTypes;
        this.civilServantPosition = civilServantPosition;
        this.institutionPosition = institutionPosition;
    }

    public String changeType() {
        return changeType;
    }

    public Set<String> rollbackChangeTypes() {
        return rollbackChangeTypes;
    }

    public Boolean civilServantPosition() {
        return civilServantPosition;
    }

    public Boolean institutionPosition() {
        return institutionPosition;
    }

    public static StandardAdjustmentScope parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return ALL;
        }
        return switch (raw.trim().toUpperCase()) {
            case "BASIC" -> BASIC;
            case "CIVIL_ALLOWANCE", "CIVIL" -> CIVIL_ALLOWANCE;
            case "PERFORMANCE" -> PERFORMANCE;
            default -> ALL;
        };
    }
}
