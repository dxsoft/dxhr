package com.dxsoft.rsgzgl.personnel;

public record PersonnelFieldPolicyEntry(
        String fieldName,
        String requestProperty,
        String elementId,
        boolean visible,
        boolean editable,
        String readOnlyReason,
        String category,
        boolean salaryField) {
}
