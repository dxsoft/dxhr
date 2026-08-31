package com.dxsoft.rsgzgl.security.ukey;

import com.dxsoft.rsgzgl.security.AppUserPrincipal;
import com.dxsoft.rsgzgl.security.SecurityAuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

@Component
public class DualFactorAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UkeyRequirementEvaluator requirementEvaluator;
    private final SecurityAuditService auditService;
    private final long preauthTtlSeconds;
    private final SavedRequestAwareAuthenticationSuccessHandler defaultHandler =
            new SavedRequestAwareAuthenticationSuccessHandler();

    DualFactorAuthenticationSuccessHandler(
            UkeyRequirementEvaluator requirementEvaluator,
            SecurityAuditService auditService,
            @Value("${rsgzgl.ukey.preauth-ttl-seconds:300}") long preauthTtlSeconds) {
        this.requirementEvaluator = requirementEvaluator;
        this.auditService = auditService;
        this.preauthTtlSeconds = preauthTtlSeconds;
        this.defaultHandler.setDefaultTargetUrl("/");
        this.defaultHandler.setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AppUserPrincipal user)) {
            defaultHandler.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        if (!requirementEvaluator.effectiveRequire(user.ukeyRequired())) {
            UkeyPreAuth.clear(request.getSession(false));
            auditService.recordAs(
                    user.getUsername(),
                    "LOGIN_SUCCESS",
                    "USER",
                    user.getUsername(),
                    "用户登录成功");
            defaultHandler.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        String ukeyId = user.ukeyId();
        // Tear down full auth — only pre-auth session remains until UKey verify-step.
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(true);
        session.removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);

        if (ukeyId == null || ukeyId.isBlank()) {
            UkeyPreAuth.clear(session);
            auditService.recordAs(
                    user.getUsername(),
                    "LOGIN_FAILURE",
                    "USER",
                    user.getUsername(),
                    "密码通过但未绑定 UKey，拒绝进入");
            response.sendRedirect("/login.html?ukey-missing=1");
            return;
        }

        UkeyPreAuth.store(session, user.getUsername(), preauthTtlSeconds);
        auditService.recordAs(
                user.getUsername(),
                "LOGIN_PREAUTH",
                "USER",
                user.getUsername(),
                "密码通过，等待 UKey 第二步认证");
        response.sendRedirect("/login.html?ukey-step=1");
    }
}
