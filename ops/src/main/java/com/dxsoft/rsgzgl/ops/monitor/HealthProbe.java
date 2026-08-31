package com.dxsoft.rsgzgl.ops.monitor;

import java.util.Locale;

final class HealthProbe {

    private HealthProbe() {
    }

    static String runtimeUrl(String healthUrl) {
        if (healthUrl == null || healthUrl.isBlank()) {
            throw new IllegalArgumentException("探测地址无效");
        }
        String trimmed = healthUrl.trim();
        if (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (trimmed.endsWith("/actuator/health")) {
            return trimmed.substring(0, trimmed.length() - "/actuator/health".length()) + "/internal/runtime";
        }
        java.net.URI uri = java.net.URI.create(trimmed);
        int port = uri.getPort();
        String authority = uri.getHost() + (port > 0 ? ":" + port : "");
        return uri.getScheme() + "://" + authority + "/internal/runtime";
    }

    static boolean looksLikeLogin(String location, String body) {
        String loc = location == null ? "" : location.toLowerCase(Locale.ROOT);
        if (loc.contains("login")) {
            return true;
        }
        String html = body == null ? "" : body.toLowerCase(Locale.ROOT);
        return html.contains("login.html")
                || html.contains("name=\"password\"")
                || html.contains("type=\"password\"");
    }

    static ProbeResult classify(
            Long targetId,
            String name,
            String url,
            int httpStatus,
            long latencyMs,
            String location,
            String body,
            String actuatorStatus) {
        if (httpStatus >= 300 && httpStatus < 400) {
            String message = looksLikeLogin(location, body)
                    ? "健康检查被登录拦截"
                    : ("HTTP " + httpStatus);
            return new ProbeResult(targetId, name, url, "CRIT", httpStatus, latencyMs, message);
        }
        if (httpStatus < 200 || httpStatus >= 300) {
            return new ProbeResult(targetId, name, url, "CRIT", httpStatus, latencyMs, "HTTP " + httpStatus);
        }
        if (looksLikeLogin(location, body)) {
            return new ProbeResult(targetId, name, url, "CRIT", httpStatus, latencyMs, "返回登录页，不是健康检查");
        }
        if (actuatorStatus != null && !"UP".equalsIgnoreCase(actuatorStatus)) {
            return new ProbeResult(targetId, name, url, "WARN", httpStatus, latencyMs, "应用状态 " + actuatorStatus);
        }
        return new ProbeResult(
                targetId,
                name,
                url,
                "OK",
                httpStatus,
                latencyMs,
                actuatorStatus == null ? ("HTTP " + httpStatus) : "UP");
    }
}
