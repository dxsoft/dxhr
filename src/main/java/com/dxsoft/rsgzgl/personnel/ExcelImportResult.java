package com.dxsoft.rsgzgl.personnel;

import java.util.List;

public record ExcelImportResult(
        String organizationCode,
        int importedCount,
        int skippedCount,
        List<String> errors,
        String message) {
}
