package com.dxsoft.rsgzgl.security;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
class SecurityAdminService {

    private final SecurityAdminRepository repository;
    private final PasswordEncoder passwordEncoder;

    SecurityAdminService(SecurityAdminRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
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
        return repository.users().stream().filter(user -> user.id().equals(id)).findFirst().orElseThrow();
    }

    void updateUserRoles(Long userId, CodesRequest request) {
        repository.replaceUserRoles(userId, normalizeCodes(request.codes()));
    }

    void updateUserEnabled(Long userId, EnabledRequest request) {
        repository.updateUserEnabled(userId, request.enabled());
    }

    void updateUserPassword(Long userId, PasswordRequest request) {
        repository.updateUserPassword(userId, passwordEncoder.encode(request.password()));
    }

    RoleAdminView createRole(CreateRoleRequest request) {
        Long id = repository.createRole(
                normalize(request.code()).toUpperCase(),
                normalize(request.name()),
                normalize(request.dataScope()).toUpperCase());
        return repository.roles().stream().filter(role -> role.id().equals(id)).findFirst().orElseThrow();
    }

    void updateRolePermissions(Long roleId, CodesRequest request) {
        repository.replaceRolePermissions(roleId, normalizeCodes(request.codes()));
    }

    void updateRoleOrganizations(Long roleId, CodesRequest request) {
        repository.replaceRoleOrganizations(roleId, normalizeCodes(request.codes()));
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
