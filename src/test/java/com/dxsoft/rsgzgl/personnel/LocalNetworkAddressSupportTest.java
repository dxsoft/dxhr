package com.dxsoft.rsgzgl.personnel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;

class LocalNetworkAddressSupportTest {

    @Test
    void pickPreferredAddressPrefers192168Network() {
        List<String> addresses = List.of("10.0.0.8", "192.168.3.21", "172.16.0.4");
        assertEquals("192.168.3.21", LocalNetworkAddressSupport.pickPreferredAddress(addresses));
    }

    @Test
    void buildBaseUrlIncludesPortWhenNeeded() {
        assertEquals("http://192.168.3.21:8081", LocalNetworkAddressSupport.buildBaseUrl("http", "192.168.3.21", 8081));
        assertEquals("http://192.168.3.21", LocalNetworkAddressSupport.buildBaseUrl("http", "192.168.3.21", 80));
        assertEquals("https://pq.dxsoft.cn", LocalNetworkAddressSupport.buildBaseUrl("https", "pq.dxsoft.cn", 443));
    }

    @Test
    void pickPreferredAddressReturnsNullForEmptyList() {
        assertNull(LocalNetworkAddressSupport.pickPreferredAddress(List.of()));
    }

    @Test
    void isPublicBaseUrlDetectsPrivateAndPublicHosts() {
        assertTrue(LocalNetworkAddressSupport.isPublicBaseUrl("https://pq.dxsoft.cn"));
        assertTrue(LocalNetworkAddressSupport.isPublicBaseUrl("https://pq.dxsoft.cn/"));
        assertFalse(LocalNetworkAddressSupport.isPublicBaseUrl("http://192.168.1.10"));
        assertFalse(LocalNetworkAddressSupport.isPublicBaseUrl("http://localhost:8080"));
    }

    @Test
    void resolveSuggestedPublicBaseUrlPrefersConfiguredUrl() {
        assertEquals(
                "https://pq.dxsoft.cn",
                LocalNetworkAddressSupport.resolveSuggestedPublicBaseUrl(
                        "https://pq.dxsoft.cn",
                        "http://192.168.1.1",
                        "http",
                        "127.0.0.1",
                        8080));
    }

    @Test
    void resolveSuggestedPublicBaseUrlPrefersBrowserOriginOverLan() {
        assertEquals(
                "https://pq.dxsoft.cn",
                LocalNetworkAddressSupport.resolveSuggestedPublicBaseUrl(
                        "",
                        "https://pq.dxsoft.cn",
                        "http",
                        "127.0.0.1",
                        8080));
    }

    @Test
    void resolveSuggestedPublicBaseUrlUsesPrivateLanBrowserOrigin() {
        assertEquals(
                "http://192.168.3.21:8081",
                LocalNetworkAddressSupport.resolveSuggestedPublicBaseUrl(
                        "",
                        "http://192.168.3.21:8081",
                        "http",
                        "127.0.0.1",
                        8081));
    }

    @Test
    void resolveSuggestedPublicBaseUrlUsesPrivateRequestHost() {
        assertEquals(
                "http://192.168.3.21:8081",
                LocalNetworkAddressSupport.resolveSuggestedPublicBaseUrl(
                        "",
                        "http://localhost:8081",
                        "http",
                        "192.168.3.21",
                        8081));
    }

    @Test
    void resolveSuggestedPublicBaseUrlNeverReturnsLocalhostWhenLanExists() {
        String suggested = LocalNetworkAddressSupport.resolveSuggestedPublicBaseUrl(
                "",
                "http://localhost:8080",
                "http",
                "127.0.0.1",
                8080);
        if (suggested != null) {
            assertFalse(LocalNetworkAddressSupport.isLocalhostHost(URI.create(suggested).getHost()));
        }
    }
}
