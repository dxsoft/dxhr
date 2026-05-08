package com.dxsoft.rsgzgl.security;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/users-page")
    PageResponse<SecurityAdminService.UserAdminView> usersPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return service.users(keyword, PageRequest.of(page, size));
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

    @GetMapping("/roles-page")
    PageResponse<SecurityAdminService.RoleAdminView> rolesPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return service.roles(keyword, PageRequest.of(page, size));
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

    @GetMapping("/menus-page")
    PageResponse<SecurityAdminService.MenuAdminView> menusPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return service.menus(keyword, PageRequest.of(page, size));
    }

    @PostMapping("/menus")
    SecurityAdminService.MenuAdminView createMenu(@RequestBody SecurityAdminService.CreateMenuRequest request) {
        return service.createMenu(request);
    }

    @PutMapping("/menus/{menuId}")
    void updateMenu(@PathVariable Long menuId, @RequestBody SecurityAdminService.UpdateMenuRequest request) {
        service.updateMenu(menuId, request);
    }

    @GetMapping("/audit-logs")
    List<SecurityAuditLog> auditLogs(@RequestParam(required = false) Integer limit) {
        return service.auditLogs(limit);
    }

    @GetMapping("/audit-logs-page")
    PageResponse<SecurityAuditLog> auditLogsPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return service.auditLogs(keyword, PageRequest.of(page, size));
    }
}
