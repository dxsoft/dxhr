package com.dxsoft.rsgzgl.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

final class PayrollFeatureSecurityCustomizer {

    private PayrollFeatureSecurityCustomizer() {
    }

    static void configurePayrollFeaturePostRules(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorize) {
        for (PayrollFeaturePermissions.Feature feature : PayrollFeaturePermissions.all()) {
            authorize.requestMatchers(HttpMethod.POST, "/api/payroll/" + feature.apiPath() + "/**")
                    .hasAnyAuthority(PayrollFeaturePermissions.writeAuthorities(feature));
        }
    }

    static void configurePayrollFeatureReadRules(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorize) {
        for (PayrollFeaturePermissions.Feature feature : PayrollFeaturePermissions.all()) {
            authorize.requestMatchers("/api/payroll/" + feature.apiPath() + "/**")
                    .hasAnyAuthority(PayrollFeaturePermissions.readAuthorities(feature));
        }
    }
}
