package com.dxsoft.rsgzgl.security;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security")
class SecurityAdminController {

    private final SecurityAdminService service;

    SecurityAdminController(SecurityAdminService service) {
        this.service = service;
    }

    @GetMapping("/users")
    List<SecurityAdminService.UserAdminView> users() {
        return service.users();
    }

    @PostMapping("/users")
    SecurityAdminService.UserAdminView createUser(@RequestBody SecurityAdminService.CreateUserRequest request) {
        return service.createUser(request);
    }

    @PutMapping("/users/{userId}/roles")
    void updateUserRoles(@PathVariable Long userId, @RequestBody SecurityAdminService.CodesRequest request) {
        service.updateUserRoles(userId, request);
    }

    @PutMapping("/users/{userId}/enabled")
    void updateUserEnabled(@PathVariable Long userId, @RequestBody SecurityAdminService.EnabledRequest request) {
        service.updateUserEnabled(userId, request);
    }

    @PutMapping("/users/{userId}/password")
    void updateUserPassword(@PathVariable Long userId, @RequestBody SecurityAdminService.PasswordRequest request) {
        service.updateUserPassword(userId, request);
    }

    @GetMapping("/roles")
    List<SecurityAdminService.RoleAdminView> roles() {
        return service.roles();
    }

    @PostMapping("/roles")
    SecurityAdminService.RoleAdminView createRole(@RequestBody SecurityAdminService.CreateRoleRequest request) {
        return service.createRole(request);
    }

    @PutMapping("/roles/{roleId}/permissions")
    void updateRolePermissions(@PathVariable Long roleId, @RequestBody SecurityAdminService.CodesRequest request) {
        service.updateRolePermissions(roleId, request);
    }

    @PutMapping("/roles/{roleId}/organizations")
    void updateRoleOrganizations(@PathVariable Long roleId, @RequestBody SecurityAdminService.CodesRequest request) {
        service.updateRoleOrganizations(roleId, request);
    }

    @GetMapping("/permissions")
    List<SecurityAdminService.PermissionView> permissions() {
        return service.permissions();
    }
}
