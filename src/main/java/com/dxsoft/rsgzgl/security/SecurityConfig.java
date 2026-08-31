package com.dxsoft.rsgzgl.security;

import com.dxsoft.rsgzgl.security.ukey.DualFactorAuthenticationSuccessHandler;
import com.dxsoft.rsgzgl.security.ukey.UkeyPreAuth;
import com.dxsoft.rsgzgl.security.ukey.UkeyPreAuthFilter;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
class SecurityConfig {

    private final SecurityAuditService auditService;
    private final DualFactorAuthenticationSuccessHandler dualFactorSuccessHandler;
    private final UkeyPreAuthFilter ukeyPreAuthFilter;

    SecurityConfig(
            SecurityAuditService auditService,
            DualFactorAuthenticationSuccessHandler dualFactorSuccessHandler,
            UkeyPreAuthFilter ukeyPreAuthFilter) {
        this.auditService = auditService;
        this.dualFactorSuccessHandler = dualFactorSuccessHandler;
        this.ukeyPreAuthFilter = ukeyPreAuthFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> {
                    authorize
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/app.js",
                                "/app.css",
                                "/month-picker.js",
                                "/login.html",
                                "/auth.css",
                                "/favicon.ico",
                                "/mobile-upload.html",
                                "/vendor/**",
                                "/actuator/health",
                                "/internal/runtime").permitAll()
                        .requestMatchers("/api/auth/ukey/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reports/payroll-change-export-jobs/*/download").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reports/payroll-change-export-jobs/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/mobile-attachment-sessions/network-hints")
                                .hasAuthority("PERSONNEL_WRITE")
                        .requestMatchers(HttpMethod.GET, "/api/mobile-attachment-sessions/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/mobile-attachment-sessions/*/files/*/download").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/mobile-attachment-sessions/*/files").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/mobile-attachment-sessions").hasAuthority("PERSONNEL_WRITE")
                        .requestMatchers(HttpMethod.POST, "/api/mobile-attachment-sessions/*/files/*/consume")
                                .hasAuthority("PERSONNEL_WRITE")
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/auth/**").authenticated()
                        .requestMatchers("/api/security/**").hasAuthority("SECURITY_ADMIN")
                        .requestMatchers("/api/operation-logs/**").hasAuthority("OPERATION_LOG_READ")
                        .requestMatchers(HttpMethod.POST, "/api/data-maintenance/**").hasAuthority("DATA_MAINTENANCE")
                        .requestMatchers("/api/data-maintenance/**").hasAuthority("DATA_MAINTENANCE")
                        .requestMatchers(
                                "/api/dictionaries/field-configs",
                                "/api/dictionaries/payroll-field-configs",
                                "/api/dictionaries/tree")
                                .hasAnyAuthority("PERSONNEL_WRITE", "RETIREMENT_WRITE", "RETIREMENT_READ", "SYSTEM_CONFIG")
                        .requestMatchers(HttpMethod.POST, "/api/dictionaries/**").hasAuthority("SYSTEM_CONFIG")
                        .requestMatchers(HttpMethod.PUT, "/api/dictionaries/**").hasAuthority("SYSTEM_CONFIG")
                        .requestMatchers(HttpMethod.DELETE, "/api/dictionaries/**").hasAuthority("SYSTEM_CONFIG")
                        .requestMatchers("/api/dictionaries/**").hasAuthority("SYSTEM_CONFIG")
                        .requestMatchers(HttpMethod.PUT, "/api/system-config/**").hasAuthority("SYSTEM_CONFIG")
                        .requestMatchers("/api/system-config/**").hasAuthority("SYSTEM_CONFIG")
                        .requestMatchers("/api/system-setup/**").hasAuthority("SYSTEM_CONFIG")
                        .requestMatchers(HttpMethod.POST, "/api/license/import").hasAnyAuthority("LICENSE_IMPORT", "SYSTEM_CONFIG")
                        .requestMatchers(HttpMethod.POST, "/api/license/issue").hasAnyAuthority("LICENSE_IMPORT", "SYSTEM_CONFIG")
                        .requestMatchers(HttpMethod.GET, "/api/license/issue-preview").hasAnyAuthority("LICENSE_IMPORT", "SYSTEM_CONFIG")
                        .requestMatchers(HttpMethod.GET, "/api/license/orgs-export").hasAnyAuthority("LICENSE_IMPORT", "SYSTEM_CONFIG")
                        .requestMatchers("/api/license/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/organizations/**").hasAuthority("ORG_WRITE")
                        .requestMatchers(HttpMethod.PUT, "/api/organizations/**").hasAuthority("ORG_WRITE")
                        .requestMatchers(HttpMethod.DELETE, "/api/organizations/**").hasAuthority("ORG_WRITE")
                        .requestMatchers("/api/organizations/**").hasAuthority("ORG_READ")
                        .requestMatchers(HttpMethod.POST, "/api/standards/**").hasAuthority("STANDARD_WRITE")
                        .requestMatchers(HttpMethod.PUT, "/api/standards/**").hasAuthority("STANDARD_WRITE")
                        .requestMatchers(HttpMethod.DELETE, "/api/standards/**").hasAuthority("STANDARD_WRITE")
                        .requestMatchers("/api/standards/**").hasAuthority("STANDARD_READ")
                        .requestMatchers("/api/statistics/**").hasAuthority("PERSONNEL_READ")
                        .requestMatchers(HttpMethod.POST, "/api/personnel").hasAuthority("PERSONNEL_WRITE");
                PersonnelFeatureSecurityCustomizer.configurePersonnelBasicRules(authorize);
                authorize
                        .requestMatchers(HttpMethod.DELETE, "/api/personnel/**").hasAuthority("PERSONNEL_WRITE")
                        .requestMatchers("/api/personnel/**").hasAuthority("PERSONNEL_READ");
                PayrollFeatureSecurityCustomizer.configurePayrollFeaturePostRules(authorize);
                authorize
                        .requestMatchers(HttpMethod.POST, "/api/payroll/**").hasAuthority("PAYROLL_WRITE")
                        .requestMatchers(HttpMethod.PUT, "/api/payroll/field-config").hasAuthority("SYSTEM_CONFIG")
                        .requestMatchers(HttpMethod.PUT, "/api/payroll/**").hasAuthority("PAYROLL_WRITE")
                        .requestMatchers(HttpMethod.DELETE, "/api/payroll/**").hasAuthority("PAYROLL_WRITE")
                        .requestMatchers("/api/reports/**").hasAuthority("REPORT_READ")
                        .requestMatchers(HttpMethod.POST, "/api/retirement/approval-report/**")
                                .hasAnyAuthority("RETIREMENT_READ", "RETIREMENT_WRITE")
                        .requestMatchers(HttpMethod.POST, "/api/retirement/**").hasAuthority("RETIREMENT_WRITE")
                        .requestMatchers(HttpMethod.PUT, "/api/retirement/**").hasAuthority("RETIREMENT_WRITE")
                        .requestMatchers(HttpMethod.DELETE, "/api/retirement/**").hasAuthority("RETIREMENT_WRITE")
                        .requestMatchers("/api/retirement/**").hasAuthority("RETIREMENT_READ")
                        .requestMatchers("/api/payroll/calculation-audits", "/api/payroll/calculation-audit-summary",
                                "/api/payroll/projection-audit-summary", "/api/payroll/projection-audit-export.csv",
                                "/api/payroll/projection-audit-export.xlsx").hasAuthority("AUDIT_READ")
                        .requestMatchers("/api/payroll/field-config").hasAuthority("SYSTEM_CONFIG")
                        .requestMatchers("/api/payroll/basic-standards", "/api/payroll/basic-standards/**", "/api/payroll/allowance-standards", "/api/payroll/rank-allowance-standards", "/api/payroll/retained-allowance-standards", "/api/payroll/retained-allowance-standards/**", "/api/payroll/year-allowance-standards", "/api/payroll/intern-salary-standards", "/api/payroll/wage-reform-standards", "/api/payroll/wage-reform-standards/**", "/api/payroll/other-allowance-standards", "/api/payroll/other-allowance-standards/**").hasAuthority("STANDARD_READ");
                PayrollFeatureSecurityCustomizer.configurePayrollFeatureReadRules(authorize);
                authorize
                        .requestMatchers("/api/payroll/**").hasAuthority("PAYROLL_READ")
                        .anyRequest().authenticated();
            })
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            String path = request.getRequestURI() == null ? "" : request.getRequestURI();
                            if (path.contains("/api/")) {
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                response.setCharacterEncoding("UTF-8");
                                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                response.getWriter().write("{\"message\":\"登录已失效，请重新登录后再操作。\"}");
                                return;
                            }
                            HttpSession session = request.getSession(false);
                            if (UkeyPreAuth.hasValid(session)) {
                                response.sendRedirect("/login.html?ukey-step=1");
                                return;
                            }
                            response.sendRedirect("/login.html");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            String path = request.getRequestURI() == null ? "" : request.getRequestURI();
                            if (path.contains("/api/")) {
                                response.setStatus(HttpStatus.FORBIDDEN.value());
                                response.setCharacterEncoding("UTF-8");
                                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                String message = accessDeniedException.getMessage();
                                if (message == null || message.isBlank()) {
                                    message = "当前账号无权执行此操作。";
                                }
                                String escaped = message.replace("\\", "\\\\").replace("\"", "\\\"");
                                response.getWriter().write("{\"detail\":\"" + escaped + "\"}");
                                return;
                            }
                            response.sendError(HttpStatus.FORBIDDEN.value());
                        }))
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login")
                        .successHandler(dualFactorSuccessHandler)
                        .failureUrl("/login.html?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        // Keep GET working for login-page / toolbar links (CSRF is disabled).
                        .logoutRequestMatcher(request -> {
                            String path = request.getRequestURI() == null ? "" : request.getRequestURI();
                            if (!"/logout".equals(path)) {
                                return false;
                            }
                            String method = request.getMethod();
                            return "POST".equalsIgnoreCase(method) || "GET".equalsIgnoreCase(method);
                        })
                        .addLogoutHandler((request, response, authentication) -> {
                            UkeyPreAuth.clear(request.getSession(false));
                            recordLogout(authentication);
                        })
                        .logoutSuccessUrl("/login.html?logout")
                        .permitAll())
                .addFilterBefore(ukeyPreAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void recordLogout(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return;
        }
        auditService.recordAs(
                authentication.getName(),
                "LOGOUT",
                "USER",
                authentication.getName(),
                "用户退出登录");
    }
}
