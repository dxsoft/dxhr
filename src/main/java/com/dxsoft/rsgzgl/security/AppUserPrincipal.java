package com.dxsoft.rsgzgl.security;

import java.util.Collection;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class AppUserPrincipal extends User {

    private final Long userId;
    private final String displayName;
    private final Set<String> permissions;
    private final boolean allOrganizations;
    private final Set<String> organizationCodes;
    private final String homeOrganizationCode;
    private final String ukeyId;
    private final Integer ukeyRequired;

    public AppUserPrincipal(
            Long userId,
            String username,
            String password,
            String displayName,
            boolean enabled,
            Collection<? extends GrantedAuthority> authorities,
            Set<String> permissions,
            boolean allOrganizations,
            Set<String> organizationCodes,
            String homeOrganizationCode,
            String ukeyId,
            Integer ukeyRequired) {
        super(username, password, enabled, true, true, true, authorities);
        this.userId = userId;
        this.displayName = displayName;
        this.permissions = Set.copyOf(permissions);
        this.allOrganizations = allOrganizations;
        this.organizationCodes = Set.copyOf(organizationCodes);
        this.homeOrganizationCode = homeOrganizationCode;
        this.ukeyId = ukeyId;
        this.ukeyRequired = ukeyRequired;
    }

    public Long userId() {
        return userId;
    }

    public String displayName() {
        return displayName;
    }

    public Set<String> permissions() {
        return permissions;
    }

    public boolean allOrganizations() {
        return allOrganizations;
    }

    public Set<String> organizationCodes() {
        return organizationCodes;
    }

    public String homeOrganizationCode() {
        return homeOrganizationCode;
    }

    public String ukeyId() {
        return ukeyId;
    }

    /** NULL inherit global, 1 force, 0 exempt. */
    public Integer ukeyRequired() {
        return ukeyRequired;
    }
}
