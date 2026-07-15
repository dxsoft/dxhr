package com.dxsoft.rsgzgl.personnel;

public record ExcelImportPreviewRow(
        int rowNumber,
        String personCode,
        String name,
        String highestEducation,
        String positionLevel,
        String action,
        String message) {
}
