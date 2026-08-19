package com.dxsoft.rsgzgl.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class AccessControlServicePermissionTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void hasAnyPermissionReturnsTrueWhenOneGranted() {
        authenticate(Set.of("LEVEL_PROMOTION_READ"));
        AccessControlService service = new AccessControlService();
        assertTrue(service.hasAnyPermission("LEVEL_PROMOTION_READ", "PAYROLL_READ"));
        assertFalse(service.hasAnyPermission("LEVEL_PROMOTION_WRITE", "PAYROLL_WRITE"));
    }

    @Test
    void hasAnyPermissionSupportsLegacyPayrollWriteFallback() {
        authenticate(Set.of("PAYROLL_WRITE"));
        AccessControlService service = new AccessControlService();
        assertTrue(service.hasAnyPermission("LEVEL_PROMOTION_WRITE", "PAYROLL_WRITE"));
    }

    private static void authenticate(Set<String> permissions) {
        AppUserPrincipal user = new AppUserPrincipal(
                1L,
                "viewer",
                "hash",
                "查看员",
                true,
                Set.of(),
                permissions,
                false,
                Set.of("001"),
                "001",
                null,
                null);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
