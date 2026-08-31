package com.dxsoft.rsgzgl.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

final class PersonnelFeatureSecurityCustomizer {

    private PersonnelFeatureSecurityCustomizer() {
    }

    static void configurePersonnelBasicRules(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorize) {
        authorize
                .requestMatchers(HttpMethod.GET, "/api/personnel/approval-tracking")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalTrackingReadAuthorities())
                .requestMatchers(HttpMethod.GET, "/api/personnel/assessments/approval-stats")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalTrackingReadAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/approval-tracking/batch-approve")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalWriteAuthorities())
                .requestMatchers(HttpMethod.GET, "/api/personnel/*/field-policy")
                .hasAnyAuthority(PersonnelFeaturePermissions.basicReadAuthorities())
                .requestMatchers(HttpMethod.GET, "/api/personnel/*/maintenance")
                .hasAnyAuthority(PersonnelFeaturePermissions.basicReadAuthorities())
                .requestMatchers(HttpMethod.PUT, "/api/personnel/*")
                .hasAnyAuthority(PersonnelFeaturePermissions.basicWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/approval/submit")
                .hasAnyAuthority(PersonnelFeaturePermissions.basicWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/approval/cancel")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/approval/approve")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/approval/return-to-draft")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/education/*/approval/submit")
                .hasAnyAuthority(PersonnelFeaturePermissions.subrecordWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/positions/*/approval/submit")
                .hasAnyAuthority(PersonnelFeaturePermissions.subrecordWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/assessments/*/approval/submit")
                .hasAnyAuthority(PersonnelFeaturePermissions.subrecordWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/awards/*/approval/submit")
                .hasAnyAuthority(PersonnelFeaturePermissions.subrecordWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/ranks/*/approval/submit")
                .hasAnyAuthority(PersonnelFeaturePermissions.subrecordWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/education/*/approval/return-to-draft")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/positions/*/approval/return-to-draft")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/assessments/*/approval/return-to-draft")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/awards/*/approval/return-to-draft")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/ranks/*/approval/return-to-draft")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/education/*/approval/approve")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/positions/*/approval/approve")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/assessments/*/approval/approve")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/awards/*/approval/approve")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/ranks/*/approval/approve")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/education/*/approval/cancel")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/positions/*/approval/cancel")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/assessments/*/approval/cancel")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/awards/*/approval/cancel")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalWriteAuthorities())
                .requestMatchers(HttpMethod.POST, "/api/personnel/*/ranks/*/approval/cancel")
                .hasAnyAuthority(PersonnelFeaturePermissions.approvalWriteAuthorities());
    }
}
