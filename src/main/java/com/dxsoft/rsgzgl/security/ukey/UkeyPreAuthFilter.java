package com.dxsoft.rsgzgl.security.ukey;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * While a password pre-auth is pending UKey verification, block business access and steer
 * the browser back to the second login step.
 */
@Component
public class UkeyPreAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String preAuthUser = UkeyPreAuth.usernameIfValid(session);
        if (preAuthUser == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Ensure incomplete dual-factor never looks authenticated.
        SecurityContextHolder.clearContext();

        String path = request.getRequestURI() == null ? "" : request.getRequestURI();
        if (isAllowedDuringPreAuth(path, request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (path.startsWith("/api/")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setCharacterEncoding("UTF-8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"请完成 UKey 第二步认证后再操作。\"}");
            return;
        }

        response.sendRedirect("/login.html?ukey-step=1");
    }

    private static boolean isAllowedDuringPreAuth(String path, String method) {
        if ("/login.html".equals(path)
                || "/auth.css".equals(path)
                || "/favicon.ico".equals(path)
                || path.startsWith("/vendor/")
                || path.startsWith("/actuator/health")
                || "/internal/runtime".equals(path)) {
            return true;
        }
        if ("POST".equalsIgnoreCase(method) && "/login".equals(path)) {
            return true;
        }
        if ("/logout".equals(path)) {
            return true;
        }
        if ("/api/auth/ukey/challenge".equals(path)
                || "/api/auth/ukey/verify-step".equals(path)
                || "/api/auth/ukey/options".equals(path)
                || "/api/auth/ukey/preauth-status".equals(path)
                || "/api/auth/ukey/cancel-preauth".equals(path)) {
            return true;
        }
        return false;
    }
}
