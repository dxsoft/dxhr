package com.dxsoft.rsgzgl.monitor;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;

final class LoopbackAccess {

    private LoopbackAccess() {
    }

    static boolean allowed(HttpServletRequest request) {
        if (request.getHeader("X-Forwarded-For") != null
                || request.getHeader("X-Real-IP") != null
                || request.getHeader("Forwarded") != null) {
            return false;
        }
        return isLoopback(request.getRemoteAddr());
    }

    static boolean isLoopback(String address) {
        if (address == null || address.isBlank()) {
            return false;
        }
        try {
            return InetAddress.getByName(address.trim()).isLoopbackAddress();
        } catch (Exception ex) {
            return false;
        }
    }
}
