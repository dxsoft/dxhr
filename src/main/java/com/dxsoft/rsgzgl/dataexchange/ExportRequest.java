package com.dxsoft.rsgzgl.dataexchange;

record ExportRequest(
        String organizationCode,
        String keyword,
        String period,
        String startPeriod,
        String endPeriod,
        String exportType,
        String changeType,
        boolean includeHistory) {
}
