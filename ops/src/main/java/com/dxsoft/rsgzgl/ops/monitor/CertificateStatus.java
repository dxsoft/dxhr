package com.dxsoft.rsgzgl.ops.monitor;

public record CertificateStatus(
        String host,
        int daysLeft,
        String notAfter,
        String status,
        String message
) {
}
