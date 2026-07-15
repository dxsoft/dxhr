package com.dxsoft.rsgzgl.personnel;

import java.util.List;

public record ExcelImportPreview(
        String organizationCode,
        String organizationName,
        int totalRows,
        int validRows,
        int duplicateRows,
        int errorRows,
        List<ExcelImportPreviewRow> rows,
        List<String> errors,
        String message) {
}
