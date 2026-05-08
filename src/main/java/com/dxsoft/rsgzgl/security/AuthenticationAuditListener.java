package com.dxsoft.rsgzgl.security;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
class AuthenticationAuditListener {

    private final SecurityAuditService auditService;

    AuthenticationAuditListener(SecurityAuditService auditService) {
        this.auditService = auditService;
    }

    @EventListener
    void onSuccess(AuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return;
        }
        auditService.recordAs(
                authentication.getName(),
                "LOGIN_SUCCESS",
                "USER",
                authentication.getName(),
                "用户登录成功");
    }

    @EventListener
    void onFailure(AbstractAuthenticationFailureEvent event) {
        Authentication authentication = event.getAuthentication();
        String username = authentication == null || authentication.getName() == null
                ? "UNKNOWN"
                : authentication.getName();
        auditService.recordAs(
                username,
                "LOGIN_FAILURE",
                "USER",
                username,
                "用户登录失败：" + event.getException().getMessage());
    }
}
