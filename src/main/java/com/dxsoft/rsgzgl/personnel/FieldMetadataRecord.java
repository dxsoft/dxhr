package com.dxsoft.rsgzgl.personnel;

public record FieldMetadataRecord(
        String fieldName,
        String fieldCaption,
        String category,
        boolean salaryField,
        boolean readOnly,
        boolean manualAllowed,
        int sequence) {
}
