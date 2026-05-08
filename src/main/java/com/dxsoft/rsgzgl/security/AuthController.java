package com.dxsoft.rsgzgl.security;

import java.util.Set;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    private final AccessControlService accessControlService;
    private final PasswordChangeService passwordChangeService;
    private final AppMenuService appMenuService;

    AuthController(
            AccessControlService accessControlService,
            PasswordChangeService passwordChangeService,
            AppMenuService appMenuService) {
        this.accessControlService = accessControlService;
        this.passwordChangeService = passwordChangeService;
        this.appMenuService = appMenuService;
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

    @PutMapping("/password")
    void changePassword(@RequestBody ChangePasswordRequest request) {
        passwordChangeService.changeCurrentUserPassword(request.currentPassword(), request.newPassword());
    }

    @GetMapping("/menus")
    List<AppMenuService.MenuItem> menus() {
        return appMenuService.currentUserMenus();
    }

    record CurrentUserResponse(
            String username,
            String displayName,
            Set<String> permissions,
            Boolean allOrganizations,
            Set<String> organizationCodes) {
    }

    record ChangePasswordRequest(String currentPassword, String newPassword) {
    }
}
