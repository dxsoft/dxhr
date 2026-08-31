package com.dxsoft.rsgzgl.printauth;

import com.dxsoft.rsgzgl.license.LicenseService;
import com.dxsoft.rsgzgl.license.LicenseStatus;
import com.dxsoft.rsgzgl.payroll.PayrollChangeComparison;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 对标 VFP {@code dxvalid}：花名册/审批表打印前按授权年度校验。
 * 远程成功时比较打印年与远端授权年；远程失败时回退本地单位授权包。
 */
@Service
public class PrintAuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(PrintAuthorizationService.class);
    private static final Pattern YEAR_PATTERN = Pattern.compile("(19|20)\\d{2}");
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final LicenseService licenseService;
    private final PrintAuthIdentityRepository identityRepository;
    private final String baseUrl;
    private final Duration connectTimeout;
    private final Duration requestTimeout;
    private final HttpClient httpClient;

    PrintAuthorizationService(
            LicenseService licenseService,
            PrintAuthIdentityRepository identityRepository,
            @Value("${rsgzgl.print-auth.url:http://www.dxsoft.cn:7099/dxmanage/customer/selectCus.dx}") String baseUrl,
            @Value("${rsgzgl.print-auth.connect-timeout-ms:5000}") long connectTimeoutMs,
            @Value("${rsgzgl.print-auth.request-timeout-ms:8000}") long requestTimeoutMs) {
        this.licenseService = licenseService;
        this.identityRepository = identityRepository;
        this.baseUrl = blank(baseUrl)
                ? "http://www.dxsoft.cn:7099/dxmanage/customer/selectCus.dx"
                : baseUrl.trim();
        this.connectTimeout = Duration.ofMillis(Math.max(1000, connectTimeoutMs));
        this.requestTimeout = Duration.ofMillis(Math.max(1000, requestTimeoutMs));
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(this.connectTimeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public void assertAllowedForReports(Collection<PayrollChangeComparison> reports) {
        for (Map.Entry<String, Integer> entry : maxPrintYearByOrganization(reports).entrySet()) {
            int printYear = entry.getValue() == null ? 0 : entry.getValue();
            assertAllowed(printYear, identityFor(reports, entry.getKey()));
        }
    }

    /**
     * 预览用软校验：数据问题仍抛错；仅授权未通过时返回 false（不拦截预览）。
     */
    public boolean isAuthorizedForReports(Collection<PayrollChangeComparison> reports) {
        for (Map.Entry<String, Integer> entry : maxPrintYearByOrganization(reports).entrySet()) {
            int printYear = entry.getValue() == null ? 0 : entry.getValue();
            try {
                assertAllowed(printYear, identityFor(reports, entry.getKey()));
            } catch (IllegalStateException ex) {
                log.info("print-auth: unauthorized for preview: {}", ex.getMessage());
                return false;
            }
        }
        return true;
    }

    private Map<String, Integer> maxPrintYearByOrganization(Collection<PayrollChangeComparison> reports) {
        if (reports == null || reports.isEmpty()) {
            throw new IllegalStateException("没有可打印的工资变动记录。");
        }
        Map<String, Integer> maxYearByOrg = new LinkedHashMap<>();
        for (PayrollChangeComparison report : reports) {
            if (report == null) {
                continue;
            }
            String orgCode = empty(report.organizationCode());
            if (orgCode.isEmpty()) {
                throw new IllegalStateException("工资变动记录缺少单位编码，不能打印。");
            }
            int year = yearOf(report.calculationPeriod());
            maxYearByOrg.merge(orgCode, year, Math::max);
        }
        if (maxYearByOrg.isEmpty()) {
            throw new IllegalStateException("无法确定打印单位，不能打印。");
        }
        for (Map.Entry<String, Integer> entry : maxYearByOrg.entrySet()) {
            int printYear = entry.getValue() == null ? 0 : entry.getValue();
            if (printYear <= 0) {
                throw new IllegalStateException("无法确定工资变动年度，不能打印。");
            }
        }
        return maxYearByOrg;
    }

    private PrintAuthIdentity identityFor(Collection<PayrollChangeComparison> reports, String organizationCode) {
        PrintAuthIdentity identity = identityRepository.findByOrganizationCode(organizationCode);
        if (blank(identity.organizationName()) && !blank(reportNameFallback(reports, organizationCode))) {
            return new PrintAuthIdentity(
                    identity.membership(),
                    identity.organizationCode(),
                    reportNameFallback(reports, organizationCode));
        }
        return identity;
    }

    public void assertAllowed(int printYear, PrintAuthIdentity identity) {
        if (printYear <= 0) {
            throw new IllegalStateException("打印年度无效。");
        }
        OptionalInt remoteYear = queryRemoteAuthorizedYear(identity);
        if (remoteYear.isPresent()) {
            denyIfBeyond(printYear, remoteYear.getAsInt(), "远程授权");
            return;
        }
        assertAgainstLocalLicense(printYear);
    }

    private static String reportNameFallback(Collection<PayrollChangeComparison> reports, String organizationCode) {
        for (PayrollChangeComparison report : reports) {
            if (report != null && organizationCode.equalsIgnoreCase(empty(report.organizationCode()))) {
                String name = empty(report.organizationName());
                if (!name.isEmpty()) {
                    return name;
                }
            }
        }
        return "";
    }

    private void assertAgainstLocalLicense(int printYear) {
        LicenseStatus status = licenseService.status();
        if (!status.authorized()) {
            throw new IllegalStateException(
                    "远程授权校验失败，且本地尚未导入有效单位授权包，不能打印花名册/审批表。");
        }
        Integer localYear = authorizedYearFromExpiresAt(status.expiresAt());
        if (localYear == null) {
            // 本地授权包长期有效：允许任意打印年度
            return;
        }
        denyIfBeyond(printYear, localYear, "本地授权包");
    }

    private OptionalInt queryRemoteAuthorizedYear(PrintAuthIdentity identity) {
        String membership = empty(identity.membership());
        String code = empty(identity.organizationCode());
        String name = empty(identity.organizationName());
        if (code.isEmpty() && name.isEmpty() && membership.isEmpty()) {
            log.warn("print-auth: missing identity, skip remote and use local license");
            return OptionalInt.empty();
        }
        // 对标 VFP：membership / name 先 Base64，再 URL 编码；code 仅 URL 编码。
        String url = baseUrl
                + (baseUrl.contains("?") ? "&" : "?")
                + "membership=" + encode(base64Utf8(membership))
                + "&code=" + encode(code)
                + "&name=" + encode(base64Utf8(name))
                + "&selectType=0";
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(requestTimeout)
                    .header("Cache-Control", "no-cache")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                log.warn("print-auth: remote status={} url={}", response.statusCode(), redact(url));
                return OptionalInt.empty();
            }
            String body = response.body() == null ? "" : response.body();
            // 对标 VFP：响应体需包含 "200"，否则视为未授权
            if (!body.contains("200")) {
                throw new IllegalStateException("远程授权校验未通过，不能打印花名册/审批表。");
            }
            OptionalInt year = parseAuthorizedYear(body);
            if (year.isEmpty()) {
                throw new IllegalStateException("远程授权响应无法解析授权年度，不能打印花名册/审批表。");
            }
            return year;
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("print-auth: remote interrupted, fallback to local license: {}", ex.toString());
            return OptionalInt.empty();
        } catch (IOException | RuntimeException ex) {
            log.warn("print-auth: remote failed, fallback to local license: {}", ex.toString());
            return OptionalInt.empty();
        }
    }

    /**
     * 对标 VFP：{@code SUBSTR(STRCONV(body,11),9,4)}；并补充常见 4 位年份匹配。
     */
    static OptionalInt parseAuthorizedYear(String body) {
        if (body == null || body.isBlank()) {
            return OptionalInt.empty();
        }
        String text = body.trim();
        if (text.length() >= 12) {
            String fixed = text.substring(8, 12);
            if (looksLikeYear(fixed)) {
                return OptionalInt.of(Integer.parseInt(fixed));
            }
        }
        Matcher matcher = YEAR_PATTERN.matcher(text);
        while (matcher.find()) {
            String token = matcher.group();
            if (looksLikeYear(token)) {
                return OptionalInt.of(Integer.parseInt(token));
            }
        }
        return OptionalInt.empty();
    }

    private static Integer authorizedYearFromExpiresAt(String expiresAt) {
        if (blank(expiresAt)) {
            return null;
        }
        try {
            if (expiresAt.contains("T")) {
                return Instant.parse(expiresAt).atZone(ZONE).getYear();
            }
            String datePart = expiresAt.substring(0, Math.min(10, expiresAt.length()));
            if (datePart.length() >= 4 && looksLikeYear(datePart.substring(0, 4))) {
                if (datePart.length() >= 10) {
                    return LocalDate.parse(datePart).getYear();
                }
                return Integer.parseInt(datePart.substring(0, 4));
            }
        } catch (DateTimeParseException | StringIndexOutOfBoundsException | NumberFormatException ignored) {
            // fall through
        }
        Matcher matcher = YEAR_PATTERN.matcher(expiresAt);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return null;
    }

    private static void denyIfBeyond(int printYear, int authorizedYear, String source) {
        if (printYear > authorizedYear) {
            throw new IllegalStateException(
                    "打印年度 " + printYear + " 超出" + source + "授权年度（授权至 "
                            + authorizedYear + " 年），不能打印花名册/审批表。");
        }
    }

    private static int yearOf(String period) {
        if (period == null || period.isBlank()) {
            return 0;
        }
        String trimmed = period.trim();
        if (trimmed.length() >= 4 && looksLikeYear(trimmed.substring(0, 4))) {
            return Integer.parseInt(trimmed.substring(0, 4));
        }
        Matcher matcher = YEAR_PATTERN.matcher(trimmed);
        return matcher.find() ? Integer.parseInt(matcher.group()) : 0;
    }

    private static boolean looksLikeYear(String value) {
        if (value == null || value.length() != 4) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        int year = Integer.parseInt(value);
        return year >= 1990 && year <= 2100;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    /** UTF-8 bytes → standard Base64 (no wrapping), matching VFP membership/name transport. */
    static String base64Utf8(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String redact(String url) {
        int q = url.indexOf('?');
        return q < 0 ? url : url.substring(0, q) + "?…";
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String empty(String value) {
        return value == null ? "" : value.trim();
    }
}
