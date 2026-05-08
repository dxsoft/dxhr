package com.dxsoft.rsgzgl.security;

import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    private final AccessControlService accessControlService;

    AuthController(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    @GetMapping("/me")
    CurrentUserResponse me() {
        AppUserPrincipal user = accessControlService.currentUser();
        return new CurrentUserResponse(
                user.getUsername(),
                user.displayName(),
                user.permissions(),
                user.allOrganizations(),
                user.organizationCodes());
    }

    record CurrentUserResponse(
            String username,
            String displayName,
            Set<String> permissions,
            Boolean allOrganizations,
            Set<String> organizationCodes) {
    }
}
