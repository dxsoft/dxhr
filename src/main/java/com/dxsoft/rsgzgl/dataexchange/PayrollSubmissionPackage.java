package com.dxsoft.rsgzgl.dataexchange;

import java.util.List;

public record PayrollSubmissionPackage(
        String packageType,
        String generatedAt,
        List<String> organizationCodes,
        Boolean includeDescendants,
        List<PersonnelExportRecord> personnel,
        List<DataExchangeService.ExchangeTable> payrollTables,
        List<DataExchangeService.ExchangeTable> relatedTables) {
}
