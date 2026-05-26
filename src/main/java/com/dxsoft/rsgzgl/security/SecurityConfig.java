package com.dxsoft.rsgzgl.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfig {

    private final SecurityAuditService auditService;

    SecurityConfig(SecurityAuditService auditService) {
        this.auditService = auditService;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login.html", "/auth.css", "/actuator/health").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/security/**").hasAuthority("SECURITY_ADMIN")
                        .requestMatchers("/api/dictionaries/field-configs", "/api/dictionaries/tree").hasAnyAuthority("PERSONNEL_WRITE", "SYSTEM_CONFIG")
                        .requestMatchers("/api/dictionaries/**").hasAuthority("SYSTEM_CONFIG")
                        .requestMatchers("/api/system-config/**").hasAuthority("SYSTEM_CONFIG")
                        .requestMatchers("/api/organizations/**").hasAuthority("ORG_READ")
                        .requestMatchers(HttpMethod.POST, "/api/personnel").hasAuthority("PERSONNEL_WRITE")
                        .requestMatchers(HttpMethod.PUT, "/api/personnel/**").hasAuthority("PERSONNEL_WRITE")
                        .requestMatchers(HttpMethod.DELETE, "/api/personnel/**").hasAuthority("PERSONNEL_WRITE")
                        .requestMatchers("/api/personnel/*/maintenance").hasAuthority("PERSONNEL_WRITE")
                        .requestMatchers("/api/personnel/**").hasAuthority("PERSONNEL_READ")
                        .requestMatchers(HttpMethod.POST, "/api/payroll/**").hasAuthority("PAYROLL_WRITE")
                        .requestMatchers(HttpMethod.PUT, "/api/payroll/**").hasAuthority("PAYROLL_WRITE")
                        .requestMatchers(HttpMethod.DELETE, "/api/payroll/**").hasAuthority("PAYROLL_WRITE")
                        .requestMatchers("/api/reports/**").hasAuthority("REPORT_READ")
                        .requestMatchers("/api/payroll/calculation-audits", "/api/payroll/calculation-audit-summary",
                                "/api/payroll/projection-audit-summary", "/api/payroll/projection-audit-export.csv",
                                "/api/payroll/projection-audit-export.xlsx").hasAuthority("AUDIT_READ")
                        .requestMatchers("/api/payroll/basic-standards", "/api/payroll/allowance-standards", "/api/payroll/rank-allowance-standards", "/api/payroll/retained-allowance-standards", "/api/payroll/year-allowance-standards", "/api/payroll/intern-salary-standards", "/api/payroll/wage-reform-standards", "/api/payroll/other-allowance-standards").hasAuthority("STANDARD_READ")
                        .requestMatchers("/api/payroll/**").hasAuthority("PAYROLL_READ")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login.html?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler((request, response, authentication) -> recordLogout(authentication))
                        .logoutSuccessUrl("/login.html?logout")
                        .permitAll());
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
