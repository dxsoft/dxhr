package com.dxsoft.rsgzgl.ops.monitor;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class MonitorService {

    private static final Logger log = LoggerFactory.getLogger(MonitorService.class);

    private final HostMetricsCollector hostMetrics;
    private final SystemdStatusCollector systemdStatus;
    private final CertificateExpiryCollector certificates;
    private final MonitorRepository repository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
    private final double cpuWarn;
    private final double cpuCrit;
    private final double memoryWarn;
    private final double memoryCrit;
    private final double diskWarn;
    private final double diskCrit;
    private final double swapWarn;
    private final double swapCrit;
    private final double inodeWarn;
    private final double inodeCrit;
    private final int certWarnDays;
    private final int certCritDays;
    private final double heapWarn;
    private final double heapCrit;
    private final int historyHours;
    private final String webhookUrl;
    private final String defaultHealthUrl;

    MonitorService(
            HostMetricsCollector hostMetrics,
            SystemdStatusCollector systemdStatus,
            CertificateExpiryCollector certificates,
            MonitorRepository repository,
            ObjectMapper objectMapper,
            @Value("${rsgzgl.ops.monitor.cpu-warn:80}") double cpuWarn,
            @Value("${rsgzgl.ops.monitor.cpu-crit:95}") double cpuCrit,
            @Value("${rsgzgl.ops.monitor.memory-warn:85}") double memoryWarn,
            @Value("${rsgzgl.ops.monitor.memory-crit:95}") double memoryCrit,
            @Value("${rsgzgl.ops.monitor.disk-warn:85}") double diskWarn,
            @Value("${rsgzgl.ops.monitor.disk-crit:95}") double diskCrit,
            @Value("${rsgzgl.ops.monitor.swap-warn:50}") double swapWarn,
            @Value("${rsgzgl.ops.monitor.swap-crit:80}") double swapCrit,
            @Value("${rsgzgl.ops.monitor.inode-warn:85}") double inodeWarn,
            @Value("${rsgzgl.ops.monitor.inode-crit:95}") double inodeCrit,
            @Value("${rsgzgl.ops.monitor.cert-warn-days:21}") int certWarnDays,
            @Value("${rsgzgl.ops.monitor.cert-crit-days:7}") int certCritDays,
            @Value("${rsgzgl.ops.monitor.heap-warn:85}") double heapWarn,
            @Value("${rsgzgl.ops.monitor.heap-crit:95}") double heapCrit,
            @Value("${rsgzgl.ops.monitor.history-hours:72}") int historyHours,
            @Value("${rsgzgl.ops.monitor.webhook-url:}") String webhookUrl,
            @Value("${rsgzgl.ops.monitor.default-health-url:http://127.0.0.1:8080/actuator/health}") String defaultHealthUrl) {
        this.hostMetrics = hostMetrics;
        this.systemdStatus = systemdStatus;
        this.certificates = certificates;
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.cpuWarn = cpuWarn;
        this.cpuCrit = cpuCrit;
        this.memoryWarn = memoryWarn;
        this.memoryCrit = memoryCrit;
        this.diskWarn = diskWarn;
        this.diskCrit = diskCrit;
        this.swapWarn = swapWarn;
        this.swapCrit = swapCrit;
        this.inodeWarn = inodeWarn;
        this.inodeCrit = inodeCrit;
        this.certWarnDays = certWarnDays;
        this.certCritDays = certCritDays;
        this.heapWarn = heapWarn;
        this.heapCrit = heapCrit;
        this.historyHours = historyHours;
        this.webhookUrl = webhookUrl == null ? "" : webhookUrl.trim();
        this.defaultHealthUrl = defaultHealthUrl == null ? "" : defaultHealthUrl.trim();
    }

    @PostConstruct
    void seedDefaultTarget() {
        if (repository.countTargets() > 0 || defaultHealthUrl.isBlank()) {
            return;
        }
        try {
            validateHttpUrl(defaultHealthUrl);
            repository.insertTarget("本机人事系统", defaultHealthUrl, 5000, true);
        } catch (Exception ex) {
            log.warn("跳过默认健康探测目标：{}", ex.getMessage());
        }
    }

    public MonitorOverview overview(int historyLimit) {
        Sample sample = sampleLive();
        MonitorSnapshotView latest = toView(
                null, LocalDateTime.now(), sample.overall(), sample.host(), sample.probes(),
                sample.services(), sample.certs(), sample.runtimes());
        List<MonitorSnapshotView> history = repository.listSnapshots(
                        LocalDateTime.now().minusHours(historyHours),
                        Math.min(Math.max(historyLimit, 20), 500))
                .stream()
                .map(this::toView)
                .toList();
        return new MonitorOverview(
                latest,
                history,
                repository.listTargets(),
                repository.listAlerts(30),
                thresholds());
    }

    public MonitorSnapshotView collectAndStore() {
        Sample sample = sampleLive();
        repository.insertSnapshot(sample.overall(), sample.host(), writeDetail(sample));
        repository.pruneSnapshots(LocalDateTime.now().minusHours(historyHours));
        maybeAlert(sample);
        return toView(
                null, LocalDateTime.now(), sample.overall(), sample.host(), sample.probes(),
                sample.services(), sample.certs(), sample.runtimes());
    }

    public List<MonitorTargetView> listTargets() {
        return repository.listTargets();
    }

    public MonitorTargetView addTarget(MonitorTargetRequest request) {
        if (request == null || blank(request.name()) || blank(request.url())) {
            throw new IllegalArgumentException("请填写名称和探测地址");
        }
        URI uri = validateHttpUrl(request.url());
        int timeout = request.timeoutMs() == null ? 5000 : request.timeoutMs();
        if (timeout < 500 || timeout > 30000) {
            throw new IllegalArgumentException("超时需在 500–30000 毫秒之间");
        }
        boolean enabled = request.enabled() == null || request.enabled();
        return repository.insertTarget(request.name().trim(), uri.toString(), timeout, enabled);
    }

    public void deleteTarget(long id) {
        repository.deleteTarget(id);
    }

    public MonitorThresholds thresholds() {
        return new MonitorThresholds(
                cpuWarn, cpuCrit, memoryWarn, memoryCrit, diskWarn, diskCrit,
                swapWarn, swapCrit, inodeWarn, inodeCrit, certWarnDays, certCritDays, heapWarn, heapCrit);
    }

    private Sample sampleLive() {
        HostSnapshot host = hostMetrics.collect();
        List<ServiceStatus> services = systemdStatus.collect();
        List<CertificateStatus> certs = certificates.collect();
        List<MonitorTargetView> targets = repository.listEnabledTargets();
        List<ProbeResult> probes = targets.stream().map(this::probe).toList();
        List<InstanceRuntime> runtimes = targets.stream().map(this::scrapeRuntime).toList();
        return new Sample(host, probes, services, certs, runtimes, worstStatus(host, probes, services, certs, runtimes));
    }

    private ProbeResult probe(MonitorTargetView target) {
        long start = System.nanoTime();
        try {
            URI uri = validateHttpUrl(target.url());
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofMillis(Math.max(target.timeoutMs(), 500)))
                    .header("Accept", "application/json, text/plain, */*")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long latency = (System.nanoTime() - start) / 1_000_000;
            String body = response.body() == null ? "" : response.body();
            String location = response.headers().firstValue("Location").orElse("");
            return HealthProbe.classify(
                    target.id(),
                    target.name(),
                    target.url(),
                    response.statusCode(),
                    latency,
                    location,
                    body,
                    readActuatorStatus(body));
        } catch (Exception ex) {
            long latency = (System.nanoTime() - start) / 1_000_000;
            return new ProbeResult(target.id(), target.name(), target.url(), "CRIT",
                    0, latency, shorten(ex.getMessage(), 180));
        }
    }

    private InstanceRuntime scrapeRuntime(MonitorTargetView target) {
        try {
            URI uri = validateHttpUrl(HealthProbe.runtimeUrl(target.url()));
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofMillis(Math.max(target.timeoutMs(), 500)))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return failedRuntime(target, "HTTP " + response.statusCode());
            }
            RuntimePayload parsed = objectMapper.readValue(response.body(), RuntimePayload.class);
            return withStatus(target, parsed);
        } catch (Exception ex) {
            return failedRuntime(target, shorten(ex.getMessage(), 180));
        }
    }

    private InstanceRuntime withStatus(MonitorTargetView target, RuntimePayload raw) {
        double heapPct = raw.heapMaxBytes() <= 0 ? 0 : raw.heapUsedBytes() * 100.0 / raw.heapMaxBytes();
        String db = raw.dbStatus() == null ? "" : raw.dbStatus();
        String status = "UP".equalsIgnoreCase(db) ? "OK" : "CRIT";
        String message = "UP".equalsIgnoreCase(db) ? "db UP" : ("数据库 " + (db.isBlank() ? "未知" : db));
        if (heapPct >= heapCrit) {
            status = "CRIT";
            message = "堆 " + fmt(heapPct) + "%";
        } else if (heapPct >= heapWarn) {
            status = worse(status, "WARN");
            message = "堆 " + fmt(heapPct) + "%";
        }
        if (raw.hikariPending() > 0) {
            status = worse(status, "WARN");
            message = "连接池等待 " + raw.hikariPending();
        }
        if (raw.hikariMax() > 0 && raw.hikariActive() >= raw.hikariMax()) {
            status = worse(status, "WARN");
            message = "连接池已满 " + raw.hikariActive() + "/" + raw.hikariMax();
        }
        if (raw.tomcatMax() > 0 && raw.tomcatBusy() >= raw.tomcatMax()) {
            status = worse(status, "WARN");
            message = "Tomcat 线程已满";
        }
        return new InstanceRuntime(
                target.id(),
                target.name(),
                HealthProbe.runtimeUrl(target.url()),
                status,
                message,
                raw.dbStatus(),
                raw.heapUsedBytes(),
                raw.heapMaxBytes(),
                raw.gcCount(),
                raw.gcTimeMs(),
                raw.threads(),
                raw.hikariActive(),
                raw.hikariIdle(),
                raw.hikariPending(),
                raw.hikariMax(),
                raw.tomcatBusy(),
                raw.tomcatCurrent(),
                raw.tomcatMax(),
                raw.jvmUptimeMs());
    }

    private InstanceRuntime failedRuntime(MonitorTargetView target, String message) {
        return new InstanceRuntime(
                target.id(), target.name(), HealthProbe.runtimeUrl(target.url()),
                "CRIT", message, "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    private String readActuatorStatus(String body) {
        String trimmed = body == null ? "" : body.trim();
        if (!trimmed.startsWith("{")) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(trimmed);
            JsonNode status = node.get("status");
            return status == null || status.isNull() ? null : status.asText();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String worstStatus(
            HostSnapshot host,
            List<ProbeResult> probes,
            List<ServiceStatus> services,
            List<CertificateStatus> certs,
            List<InstanceRuntime> runtimes) {
        String status = worse(level(host.cpuPercent(), cpuWarn, cpuCrit),
                worse(level(host.memoryPercent(), memoryWarn, memoryCrit),
                        level(host.diskPercent(), diskWarn, diskCrit)));
        if (host.swapTotalBytes() > 0) {
            status = worse(status, level(host.swapPercent(), swapWarn, swapCrit));
        }
        if (host.worstInodePercent() > 0) {
            status = worse(status, level(host.worstInodePercent(), inodeWarn, inodeCrit));
        }
        for (ProbeResult probe : probes) {
            status = worse(status, probe.status());
        }
        for (ServiceStatus service : services) {
            status = worse(status, service.status());
        }
        for (CertificateStatus cert : certs) {
            status = worse(status, cert.status());
        }
        for (InstanceRuntime runtime : runtimes) {
            status = worse(status, runtime.status());
        }
        return status;
    }

    private static String level(double value, double warn, double crit) {
        if (value >= crit) {
            return "CRIT";
        }
        if (value >= warn) {
            return "WARN";
        }
        return "OK";
    }

    private static String worse(String a, String b) {
        return rank(a) >= rank(b) ? a : b;
    }

    private static int rank(String status) {
        if ("CRIT".equals(status)) {
            return 2;
        }
        if ("WARN".equals(status)) {
            return 1;
        }
        return 0;
    }

    private void maybeAlert(Sample sample) {
        String fingerprint = fingerprint(sample);
        if (fingerprint.equals(repository.lastAlertFingerprint())) {
            return;
        }
        if ("OK".equals(sample.overall()) && repository.lastAlertFingerprint() == null) {
            return;
        }
        HostSnapshot host = sample.host();
        String title = "OK".equals(sample.overall()) ? "监控已恢复正常" : ("监控告警 " + sample.overall());
        String message = host.hostname()
                + " CPU " + fmt(host.cpuPercent())
                + "% 内存 " + fmt(host.memoryPercent())
                + "% 磁盘 " + fmt(host.diskPercent())
                + "% swap " + fmt(host.swapPercent())
                + "% inode " + fmt(host.worstInodePercent()) + "%。"
                + extraSummary(sample);
        repository.insertAlert(sample.overall(), title, shorten(message, 900), fingerprint);
        notifyWebhook(sample.overall(), title, message, host.hostname());
    }

    private String fingerprint(Sample sample) {
        StringBuilder sb = new StringBuilder(sample.overall());
        HostSnapshot host = sample.host();
        sb.append('|').append(level(host.cpuPercent(), cpuWarn, cpuCrit));
        sb.append('|').append(level(host.memoryPercent(), memoryWarn, memoryCrit));
        sb.append('|').append(level(host.diskPercent(), diskWarn, diskCrit));
        if (host.swapTotalBytes() > 0) {
            sb.append('|').append(level(host.swapPercent(), swapWarn, swapCrit));
        }
        sb.append('|').append(level(host.worstInodePercent(), inodeWarn, inodeCrit));
        for (ProbeResult probe : sample.probes()) {
            sb.append('|').append(probe.targetId()).append('=').append(probe.status());
        }
        for (ServiceStatus service : sample.services()) {
            sb.append("|svc:").append(service.name()).append('=').append(service.status());
        }
        for (CertificateStatus cert : sample.certs()) {
            sb.append("|crt:").append(cert.host()).append('=').append(cert.status());
        }
        for (InstanceRuntime runtime : sample.runtimes()) {
            sb.append("|rt:").append(runtime.targetId()).append('=').append(runtime.status());
        }
        return Integer.toHexString(sb.toString().hashCode());
    }

    private void notifyWebhook(String level, String title, String message, String hostname) {
        if (webhookUrl.isBlank()) {
            return;
        }
        try {
            URI uri = validateHttpUrl(webhookUrl);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("level", level);
            payload.put("title", title);
            payload.put("message", message);
            payload.put("hostname", hostname);
            payload.put("source", "rsgzgl-ops");
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception ex) {
            log.warn("监控 webhook 发送失败：{}", ex.getMessage());
        }
    }

    private String writeDetail(Sample sample) {
        try {
            HostSnapshot host = sample.host();
            return objectMapper.writeValueAsString(new SnapshotDetail(
                    host.os(),
                    host.processors(),
                    host.jvmUptimeMs(),
                    host.diskName(),
                    host.swapUsedBytes(),
                    host.swapTotalBytes(),
                    host.disks(),
                    sample.probes(),
                    sample.services(),
                    sample.certs(),
                    sample.runtimes()));
        } catch (Exception ex) {
            return "{}";
        }
    }

    private MonitorSnapshotView toView(MonitorRepository.SnapshotRow row) {
        HostSnapshot host = new HostSnapshot(
                row.hostname(), "", row.cpuPercent(),
                row.memoryUsedBytes(), row.memoryTotalBytes(),
                0, 0,
                row.diskUsedBytes(), row.diskTotalBytes(),
                "", row.loadAverage(), 0, 0, List.of());
        List<ProbeResult> probes = List.of();
        List<ServiceStatus> services = List.of();
        List<CertificateStatus> certs = List.of();
        List<InstanceRuntime> runtimes = List.of();
        if (row.detail() != null && !row.detail().isBlank()) {
            try {
                SnapshotDetail detail = objectMapper.readValue(row.detail(), SnapshotDetail.class);
                host = new HostSnapshot(
                        row.hostname(),
                        Optional.ofNullable(detail.os()).orElse(""),
                        row.cpuPercent(),
                        row.memoryUsedBytes(),
                        row.memoryTotalBytes(),
                        detail.swapUsedBytes(),
                        detail.swapTotalBytes(),
                        row.diskUsedBytes(),
                        row.diskTotalBytes(),
                        Optional.ofNullable(detail.diskName()).orElse(""),
                        row.loadAverage(),
                        detail.jvmUptimeMs(),
                        detail.processors(),
                        detail.disks() == null ? List.of() : detail.disks());
                probes = detail.probes() == null ? List.of() : detail.probes();
                services = detail.services() == null ? List.of() : detail.services();
                certs = detail.certificates() == null ? List.of() : detail.certificates();
                runtimes = detail.runtimes() == null ? List.of() : detail.runtimes();
            } catch (Exception ignored) {
            }
        }
        return toView(row.id(), row.collectedAt(), row.overall(), host, probes, services, certs, runtimes);
    }

    private MonitorSnapshotView toView(
            Long id,
            LocalDateTime collectedAt,
            String overall,
            HostSnapshot host,
            List<ProbeResult> probes,
            List<ServiceStatus> services,
            List<CertificateStatus> certs,
            List<InstanceRuntime> runtimes) {
        return new MonitorSnapshotView(
                id,
                collectedAt,
                overall,
                host.cpuPercent(),
                host.memoryUsedBytes(),
                host.memoryTotalBytes(),
                host.swapUsedBytes(),
                host.swapTotalBytes(),
                host.diskUsedBytes(),
                host.diskTotalBytes(),
                host.loadAverage(),
                host.hostname(),
                probes,
                host.disks() == null ? List.of() : host.disks(),
                services == null ? List.of() : services,
                certs == null ? List.of() : certs,
                runtimes == null ? List.of() : runtimes,
                host.os(),
                host.processors(),
                host.jvmUptimeMs(),
                host.diskName());
    }

    static URI validateHttpUrl(String raw) {
        URI uri;
        try {
            uri = URI.create(raw.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("探测地址无效");
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https")) {
            throw new IllegalArgumentException("探测地址仅支持 http/https");
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("探测地址缺少主机");
        }
        return uri;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static String shorten(String value, int max) {
        if (value == null) {
            return "";
        }
        String text = value.replaceAll("\\s+", " ").trim();
        return text.length() <= max ? text : text.substring(0, max - 1) + "…";
    }

    private static String fmt(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String extraSummary(Sample sample) {
        List<String> bad = new ArrayList<>();
        for (ProbeResult probe : sample.probes()) {
            if (!"OK".equals(probe.status())) {
                bad.add(probe.name() + " " + probe.status() + " " + probe.message());
            }
        }
        for (ServiceStatus service : sample.services()) {
            if (!"OK".equals(service.status())) {
                bad.add(service.name() + " " + service.status() + " " + service.message());
            }
        }
        for (CertificateStatus cert : sample.certs()) {
            if (!"OK".equals(cert.status())) {
                bad.add(cert.host() + " 证书 " + cert.status() + " " + cert.message());
            }
        }
        for (InstanceRuntime runtime : sample.runtimes()) {
            if (!"OK".equals(runtime.status())) {
                bad.add(runtime.name() + " 运行时 " + runtime.status() + " " + runtime.message());
            }
        }
        return bad.isEmpty() ? "探测与进程全部正常。" : String.join("；", bad);
    }

    private record Sample(
            HostSnapshot host,
            List<ProbeResult> probes,
            List<ServiceStatus> services,
            List<CertificateStatus> certs,
            List<InstanceRuntime> runtimes,
            String overall
    ) {
    }
}
