package com.dxsoft.rsgzgl.report.export;

final class PrintAuthWatermark {

    private PrintAuthWatermark() {
    }

    static String wrap(String sheetHtml) {
        return "<div class=\"print-auth-watermark-wrap\">"
                + sheetHtml
                + "<div class=\"print-auth-watermark\" aria-hidden=\"true\">未授权</div></div>";
    }
}
