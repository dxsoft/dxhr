package com.dxsoft.rsgzgl.personnel;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class LocalNetworkAddressSupport {

    private LocalNetworkAddressSupport() {
    }

    static List<String> privateIpv4Addresses() {
        Set<String> addresses = new LinkedHashSet<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress address = inetAddresses.nextElement();
                    if (!(address instanceof Inet4Address) || address.isLoopbackAddress() || address.isLinkLocalAddress()) {
                        continue;
                    }
                    String host = address.getHostAddress();
                    if (host != null && isPrivateIpv4(host)) {
                        addresses.add(host);
                    }
                }
            }
        } catch (SocketException ignored) {
            // Fall back to an empty list when network interfaces cannot be enumerated.
        }
        return sortByPreference(addresses);
    }

    static String pickPreferredAddress(List<String> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }
        return sortByPreference(new LinkedHashSet<>(addresses)).getFirst();
    }

    static String buildBaseUrl(String scheme, String host, int port) {
        String normalizedScheme = scheme == null || scheme.isBlank() ? "http" : scheme.trim().toLowerCase();
        int normalizedPort = port > 0 ? port : "https".equals(normalizedScheme) ? 443 : 80;
        if ("http".equals(normalizedScheme) && normalizedPort == 80) {
            return normalizedScheme + "://" + host;
        }
        if ("https".equals(normalizedScheme) && normalizedPort == 443) {
            return normalizedScheme + "://" + host;
        }
        return normalizedScheme + "://" + host + ":" + normalizedPort;
    }

    static String normalizePublicBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return null;
        }
        String trimmed = baseUrl.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    static boolean isPublicBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(normalizePublicBaseUrl(baseUrl));
            String host = uri.getHost();
            return host != null && !isLocalOrPrivateHost(host);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    static boolean isLocalhostHost(String host) {
        if (host == null || host.isBlank()) {
            return true;
        }
        String normalized = host.trim().toLowerCase(Locale.ROOT);
        return "localhost".equals(normalized) || "127.0.0.1".equals(normalized) || "::1".equals(normalized);
    }

    static boolean isPrivateLanHost(String host) {
        return host != null && !host.isBlank() && isPrivateIpv4(host.trim()) && !isLocalhostHost(host);
    }

    static boolean isLocalOrPrivateHost(String host) {
        if (host == null || host.isBlank()) {
            return true;
        }
        if (isLocalhostHost(host)) {
            return true;
        }
        return isPrivateIpv4(host.trim().toLowerCase(Locale.ROOT));
    }

    static String resolveSuggestedPublicBaseUrl(
            String configuredBaseUrl,
            String browserOrigin,
            String requestScheme,
            String requestHost,
            int requestPort) {
        String configured = normalizePublicBaseUrl(configuredBaseUrl);
        if (configured != null && isPublicBaseUrl(configured)) {
            return configured;
        }
        String origin = normalizePublicBaseUrl(browserOrigin);
        if (origin != null && isPublicBaseUrl(origin)) {
            return origin;
        }
        if (origin != null) {
            try {
                String originHost = URI.create(origin).getHost();
                if (isPrivateLanHost(originHost)) {
                    return origin;
                }
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed browser origins.
            }
        }
        if (requestHost != null && !requestHost.isBlank()) {
            String normalizedHost = requestHost.trim().toLowerCase(Locale.ROOT);
            if (!isLocalhostHost(normalizedHost)
                    && (isPrivateLanHost(normalizedHost) || !isLocalOrPrivateHost(normalizedHost))) {
                return buildBaseUrl(requestScheme, normalizedHost, requestPort);
            }
        }
        String preferred = pickPreferredAddress(privateIpv4Addresses());
        if (preferred == null) {
            return null;
        }
        return buildBaseUrl("http", preferred, requestPort);
    }

    private static List<String> sortByPreference(Set<String> addresses) {
        return addresses.stream()
                .sorted(Comparator.comparingInt(LocalNetworkAddressSupport::addressPriority).thenComparing(String::compareTo))
                .toList();
    }

    private static int addressPriority(String host) {
        if (host.startsWith("192.168.")) {
            return 0;
        }
        if (host.startsWith("10.")) {
            return 1;
        }
        String[] parts = host.split("\\.");
        if (parts.length == 4) {
            try {
                int second = Integer.parseInt(parts[1]);
                if (second >= 16 && second <= 31) {
                    return 2;
                }
            } catch (NumberFormatException ignored) {
                // Ignore malformed addresses.
            }
        }
        return 3;
    }

    private static boolean isPrivateIpv4(String host) {
        String[] parts = host.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        try {
            int first = Integer.parseInt(parts[0]);
            int second = Integer.parseInt(parts[1]);
            if (first == 10) {
                return true;
            }
            if (first == 172 && second >= 16 && second <= 31) {
                return true;
            }
            return first == 192 && second == 168;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
