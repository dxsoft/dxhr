package com.dxsoft.rsgzgl.security;

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

    List<RoleAdminView> roles() {
        return repository.roles();
    }

    List<PermissionView> permissions() {
        return repository.permissions();
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

    record CreateUserRequest(String username, String password, String displayName, Boolean enabled) {
    }

    record CreateRoleRequest(String code, String name, String dataScope) {
    }

    record CodesRequest(List<String> codes) {
    }

    record EnabledRequest(Boolean enabled) {
    }

    record PasswordRequest(String password) {
    }
}
