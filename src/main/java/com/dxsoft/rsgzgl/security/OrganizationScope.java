package com.dxsoft.rsgzgl.security;

import java.util.Set;

public record OrganizationScope(boolean all, Set<String> organizationCodes) {

    public static OrganizationScope unrestricted() {
        return new OrganizationScope(true, Set.of());
    }

    public static OrganizationScope custom(Set<String> organizationCodes) {
        return new OrganizationScope(false, Set.copyOf(organizationCodes));
    }

    public static OrganizationScope none() {
        return new OrganizationScope(false, Set.of());
    }

    public boolean noneScope() {
        return !all && organizationCodes.isEmpty();
    }
}
