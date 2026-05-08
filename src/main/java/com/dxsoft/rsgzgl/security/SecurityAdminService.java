package com.dxsoft.rsgzgl.security;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
class SecurityAdminService {

    private final SecurityAdminRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityAuditService auditService;

    SecurityAdminService(
            SecurityAdminRepository repository,
            PasswordEncoder passwordEncoder,
            SecurityAuditService auditService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    List<UserAdminView> users() {
        return repository.users();
    }

    PageResponse<UserAdminView> users(String keyword, PageRequest pageRequest) {
        return PageResponse.of(
                repository.users(keyword, pageRequest),
                pageRequest,
                repository.countUsers(keyword));
    }

    List<RoleAdminView> roles() {
        return repository.roles();
    }

    PageResponse<RoleAdminView> roles(String keyword, PageRequest pageRequest) {
        return PageResponse.of(
                repository.roles(keyword, pageRequest),
                pageRequest,
                repository.countRoles(keyword));
    }

    List<PermissionView> permissions() {
        return repository.permissions();
    }

    PageResponse<MenuAdminView> menus(String keyword, PageRequest pageRequest) {
        return PageResponse.of(
                repository.menus(keyword, pageRequest),
                pageRequest,
                repository.countMenus(keyword));
    }

    UserAdminView createUser(CreateUserRequest request) {
        Long id = repository.createUser(
                normalize(request.username()),
                passwordEncoder.encode(request.password()),
                normalize(request.displayName()),
                request.enabled() == null || request.enabled());
        auditService.record("CREATE_USER", "USER", id, "创建用户 " + request.username());
        return repository.users().stream().filter(user -> user.id().equals(id)).findFirst().orElseThrow();
    }

    void updateUserRoles(Long userId, CodesRequest request) {
        List<String> codes = normalizeCodes(request.codes());
        repository.replaceUserRoles(userId, codes);
        auditService.record("UPDATE_USER_ROLES", "USER", userId, "更新用户角色 " + codes);
    }

    void updateUserEnabled(Long userId, EnabledRequest request) {
        repository.updateUserEnabled(userId, request.enabled());
        auditService.record("UPDATE_USER_ENABLED", "USER", userId, "更新用户启用状态为 " + request.enabled());
    }

    void updateUserPassword(Long userId, PasswordRequest request) {
        repository.updateUserPassword(userId, passwordEncoder.encode(request.password()));
        auditService.record("UPDATE_USER_PASSWORD", "USER", userId, "重置用户密码");
    }

    RoleAdminView createRole(CreateRoleRequest request) {
        Long id = repository.createRole(
                normalize(request.code()).toUpperCase(),
                normalize(request.name()),
                normalize(request.dataScope()).toUpperCase());
        auditService.record("CREATE_ROLE", "ROLE", id, "创建角色 " + request.code());
        return repository.roles().stream().filter(role -> role.id().equals(id)).findFirst().orElseThrow();
    }

    MenuAdminView createMenu(CreateMenuRequest request) {
        Long id = repository.createMenu(
                normalize(request.code()).toUpperCase(),
                normalize(request.title()),
                normalize(request.path()),
                normalize(request.permissionCode()).toUpperCase(),
                request.sortOrder(),
                request.enabled() == null || request.enabled());
        auditService.record("CREATE_MENU", "MENU", id, "创建菜单 " + request.code());
        return repository.menus(request.code(), PageRequest.of(0, 1)).stream().findFirst().orElseThrow();
    }

    void updateMenu(Long menuId, UpdateMenuRequest request) {
        repository.updateMenu(
                menuId,
                normalize(request.title()),
                normalize(request.path()),
                normalize(request.permissionCode()).toUpperCase(),
                request.sortOrder(),
                request.enabled() == null || request.enabled());
        auditService.record("UPDATE_MENU", "MENU", menuId, "更新菜单 " + request.title());
    }

    void updateRolePermissions(Long roleId, CodesRequest request) {
        List<String> codes = normalizeCodes(request.codes());
        repository.replaceRolePermissions(roleId, codes);
        auditService.record("UPDATE_ROLE_PERMISSIONS", "ROLE", roleId, "更新角色功能权限 " + codes);
    }

    void updateRoleOrganizations(Long roleId, CodesRequest request) {
        List<String> codes = normalizeCodes(request.codes());
        repository.replaceRoleOrganizations(roleId, codes);
        auditService.record("UPDATE_ROLE_ORGANIZATIONS", "ROLE", roleId, "更新角色单位范围 " + codes);
    }

    List<SecurityAuditLog> auditLogs(Integer limit) {
        return auditService.recent(limit == null ? 50 : limit);
    }

    PageResponse<SecurityAuditLog> auditLogs(String keyword, PageRequest pageRequest) {
        return auditService.search(keyword, pageRequest);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Value must not be blank");
        }
        return value.trim();
    }

    private List<String> normalizeCodes(List<String> codes) {
        if (codes == null) {
            return List.of();
        }
        return codes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(code -> code.trim().toUpperCase())
                .distinct()
                .toList();
    }

    record UserAdminView(Long id, String username, String displayName, Boolean enabled, List<String> roleCodes) {
    }

    record RoleAdminView(Long id, String code, String name, String dataScope, List<String> permissionCodes, List<String> organizationCodes) {
    }

    record PermissionView(String code, String name, String category) {
    }

    record MenuAdminView(Long id, String code, String title, String path, String permissionCode, Integer sortOrder, Boolean enabled) {
    }

    record CreateUserRequest(String username, String password, String displayName, Boolean enabled) {
    }

    record CreateRoleRequest(String code, String name, String dataScope) {
    }

    record CreateMenuRequest(String code, String title, String path, String permissionCode, Integer sortOrder, Boolean enabled) {
    }

    record UpdateMenuRequest(String title, String path, String permissionCode, Integer sortOrder, Boolean enabled) {
    }

    record CodesRequest(List<String> codes) {
    }

    record EnabledRequest(Boolean enabled) {
    }

    record PasswordRequest(String password) {
    }
}
