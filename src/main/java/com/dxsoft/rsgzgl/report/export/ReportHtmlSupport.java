package com.dxsoft.rsgzgl.report.export;

public final class ReportHtmlSupport {

    private ReportHtmlSupport() {
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    public static String escapedMultilineBr(String value) {
        return escape(value).replace("\n", "<br/>");
    }

    static String blankDash(String value) {
        if (value == null) {
            return "-";
        }
        String text = value.trim();
        return text.isEmpty() ? "-" : text;
    }

    public static String loadClasspathText(String path) {
        try (var stream = ReportHtmlSupport.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Missing classpath resource: " + path);
            }
            return new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Failed to load resource: " + path, exception);
        }
    }
}
