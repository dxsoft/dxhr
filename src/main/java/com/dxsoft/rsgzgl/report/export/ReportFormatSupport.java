package com.dxsoft.rsgzgl.report.export;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public final class ReportFormatSupport {

    private static final DecimalFormat MONEY = new DecimalFormat("#,##0");

    private ReportFormatSupport() {
    }

    public static String formatMoney(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
            return "——";
        }
        return MONEY.format(value);
    }
}
