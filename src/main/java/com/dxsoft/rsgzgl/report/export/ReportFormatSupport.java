package com.dxsoft.rsgzgl.report.export;

import java.math.BigDecimal;
import java.text.DecimalFormat;

final class ReportFormatSupport {

    private static final DecimalFormat MONEY = new DecimalFormat("#,##0");

    private ReportFormatSupport() {
    }

    static String formatMoney(BigDecimal value) {
        return MONEY.format(value == null ? BigDecimal.ZERO : value);
    }
}
