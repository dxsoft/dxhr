package com.dxsoft.rsgzgl.ops.monitor;

import java.net.InetSocketAddress;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class CertificateExpiryCollector {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault());

    private final List<String> hosts;
    private final int warnDays;
    private final int critDays;

    CertificateExpiryCollector(
            @Value("${rsgzgl.ops.monitor.cert-hosts:renshi.dxsoft.cn,pq.dxsoft.cn,shpr.dxsoft.cn,xyzzb.dxsoft.cn}") String hosts,
            @Value("${rsgzgl.ops.monitor.cert-warn-days:21}") int warnDays,
            @Value("${rsgzgl.ops.monitor.cert-crit-days:7}") int critDays) {
        this.hosts = SystemdStatusCollector.split(hosts);
        this.warnDays = warnDays;
        this.critDays = critDays;
    }

    List<CertificateStatus> collect() {
        if (hosts.isEmpty()) {
            return List.of();
        }
        List<CertificateStatus> rows = new ArrayList<>();
        for (String host : hosts) {
            rows.add(read(host));
        }
        return List.copyOf(rows);
    }

    private CertificateStatus read(String host) {
        try (SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket()) {
            socket.setSoTimeout(5000);
            socket.connect(new InetSocketAddress(host, 443), 5000);
            socket.startHandshake();
            Certificate[] certs = socket.getSession().getPeerCertificates();
            if (certs.length == 0 || !(certs[0] instanceof X509Certificate x509)) {
                return new CertificateStatus(host, 0, "", "CRIT", "未拿到服务器证书");
            }
            int days = (int) ChronoUnit.DAYS.between(Instant.now(), x509.getNotAfter().toInstant());
            String notAfter = DATE.format(x509.getNotAfter().toInstant());
            if (days <= critDays) {
                return new CertificateStatus(host, days, notAfter, "CRIT", "将于 " + notAfter + " 到期");
            }
            if (days <= warnDays) {
                return new CertificateStatus(host, days, notAfter, "WARN", "将于 " + notAfter + " 到期");
            }
            return new CertificateStatus(host, days, notAfter, "OK", "有效期至 " + notAfter);
        } catch (Exception ex) {
            return new CertificateStatus(host, 0, "", "CRIT", shorten(ex.getMessage()));
        }
    }

    private static String shorten(String message) {
        if (message == null || message.isBlank()) {
            return "证书探测失败";
        }
        String text = message.replaceAll("\\s+", " ").trim();
        return text.length() <= 160 ? text : text.substring(0, 159) + "…";
    }
}
