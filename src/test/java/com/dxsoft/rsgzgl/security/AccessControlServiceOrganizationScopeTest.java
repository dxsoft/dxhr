package com.dxsoft.rsgzgl.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class AccessControlServiceOrganizationScopeTest {

    @Test
    void canAccessOrganizationUsesExpandedCodesFromPrincipal() {
        AccessControlService service = new AccessControlService();
        AppUserPrincipal user = new AppUserPrincipal(
                1L,
                "unit-admin",
                "hash",
                "单位管理员",
                true,
                Set.of(),
                Set.of("ORG_READ"),
                false,
                Set.of("001", "00105", "00107"),
                "001",
                null,
                null);
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            assertTrue(service.canAccessOrganization("001"));
            assertTrue(service.canAccessOrganization("00105"));
            assertFalse(service.canAccessOrganization("002"));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
}
