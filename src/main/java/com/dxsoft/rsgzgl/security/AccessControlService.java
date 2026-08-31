package com.dxsoft.rsgzgl.security;

import java.util.Optional;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {

    public AppUserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new AccessDeniedException("登录已失效，请重新登录后再操作。");
        }
        return principal;
    }

    public boolean hasPermission(String permission) {
        return currentUser().permissions().contains(permission);
    }

    public boolean hasAnyPermission(String... permissions) {
        Set<String> granted = currentUser().permissions();
        for (String permission : permissions) {
            if (granted.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    public boolean canAccessOrganization(String organizationCode) {
        AppUserPrincipal user = currentUser();
        return user.allOrganizations() || user.organizationCodes().contains(organizationCode);
    }

    public void requireOrganization(String organizationCode) {
        if (!canAccessOrganization(organizationCode)) {
            throw new AccessDeniedException("No permission for organization: " + organizationCode);
        }
    }

    public OrganizationScope organizationScope(Optional<String> requestedOrganizationCode) {
        AppUserPrincipal user = currentUser();
        if (user.allOrganizations()) {
            return requestedOrganizationCode
                    .map(code -> OrganizationScope.custom(Set.of(code)))
                    .orElseGet(OrganizationScope::unrestricted);
        }
        if (requestedOrganizationCode.isPresent()) {
            String code = requestedOrganizationCode.get();
            return user.organizationCodes().contains(code)
                    ? OrganizationScope.custom(Set.of(code))
                    : OrganizationScope.none();
        }
        return user.organizationCodes().isEmpty()
                ? OrganizationScope.none()
                : OrganizationScope.custom(user.organizationCodes());
    }
}
