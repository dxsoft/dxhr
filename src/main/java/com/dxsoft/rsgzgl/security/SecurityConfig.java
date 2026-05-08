package com.dxsoft.rsgzgl.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login.html", "/auth.css", "/actuator/health").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/security/**").hasAuthority("SECURITY_ADMIN")
                        .requestMatchers("/api/organizations/**").hasAuthority("ORG_READ")
                        .requestMatchers("/api/personnel/**").hasAuthority("PERSONNEL_READ")
                        .requestMatchers("/api/payroll/calculation-audits", "/api/payroll/calculation-audit-summary").hasAuthority("AUDIT_READ")
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
                        .logoutSuccessUrl("/login.html?logout")
                        .permitAll());
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
