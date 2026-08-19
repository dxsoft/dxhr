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
    private final OrganizationScopeResolver organizationScopeResolver;

    SecurityAdminService(
            SecurityAdminRepository repository,
            PasswordEncoder passwordEncoder,
            SecurityAuditService auditService,
            OrganizationScopeResolver organizationScopeResolver) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.organizationScopeResolver = organizationScopeResolver;
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

    List<MenuAdminView> menusAll(String keyword) {
        return repository.menusAll(keyword);
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
        ensureUserHasRequiredDataScope(userId);
        auditService.record("UPDATE_USER_ROLES", "USER", userId, "更新用户角色 " + codes);
    }

    void updateUserDataScope(Long userId, DataScopeRequest request) {
        boolean allOrganizations = Boolean.TRUE.equals(request.allOrganizations());
        String organizationCode = allOrganizations ? null : blankToNull(request.organizationCode());
        if (!allOrganizations && organizationCode != null
                && !organizationScopeResolver.organizationExists(organizationCode)) {
            throw new IllegalArgumentException("主管单位不存在: " + organizationCode);
        }
        repository.updateUserDataScope(userId, allOrganizations, organizationCode);
        if (!allOrganizations) {
            ensureUserHasRequiredDataScope(userId);
        }
        auditService.record(
                "UPDATE_USER_DATA_SCOPE",
                "USER",
                userId,
                allOrganizations
                        ? "设置用户数据范围为全部单位"
                        : (organizationCode == null
                                ? "清除用户主管单位"
                                : "设置用户主管单位为 " + organizationCode));
    }

    void updateUserHomeOrganization(Long userId, HomeOrganizationRequest request) {
        updateUserDataScope(userId, new DataScopeRequest(false, request.organizationCode()));
    }

    private void ensureUserHasRequiredDataScope(Long userId) {
        if (repository.userAllOrganizations(userId)) {
            return;
        }
        if (!repository.userHasAnyRole(userId)) {
            return;
        }
        String homeOrganizationCode = repository.homeOrganizationCodeForUser(userId);
        if (homeOrganizationCode == null || homeOrganizationCode.isBlank()) {
            throw new IllegalArgumentException("已分配角色的用户必须设置数据范围：全部单位或主管单位");
        }
    }

    void updateUserEnabled(Long userId, EnabledRequest request) {
        repository.updateUserEnabled(userId, request.enabled());
        auditService.record("UPDATE_USER_ENABLED", "USER", userId, "更新用户启用状态为 " + request.enabled());
    }

    void updateUsersEnabled(BatchEnabledRequest request) {
        List<Long> userIds = request.userIds() == null ? List.of() : request.userIds().stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            throw new IllegalArgumentException("请选择用户");
        }
        if (request.enabled() == null) {
            throw new IllegalArgumentException("enabled 不能为空");
        }
        repository.updateUsersEnabled(userIds, request.enabled());
        auditService.record(
                "UPDATE_USERS_ENABLED",
                "USER",
                userIds.size(),
                (request.enabled() ? "批量启用" : "批量停用") + "用户 " + userIds);
    }

    void updateUserPassword(Long userId, PasswordRequest request) {
        repository.updateUserPassword(userId, passwordEncoder.encode(request.password()));
        auditService.record("UPDATE_USER_PASSWORD", "USER", userId, "重置用户密码");
    }

    void updateUserUkey(Long userId, UkeyBindingRequest request) {
        String ukeyId = blankToNull(request.ukeyId());
        String sm2UserId = blankToNull(request.sm2UserId());
        String pubX = normalizeHex(request.sm2PubkeyX());
        String pubY = normalizeHex(request.sm2PubkeyY());
        String encKey = normalizeHex(request.encAlgoKey());
        String modes = blankToNull(request.ukeyAuthModes());
        if (ukeyId != null) {
            Long existing = repository.findUserIdByUkeyId(ukeyId);
            if (existing != null && !existing.equals(userId)) {
                throw new IllegalArgumentException("该 UKey 已绑定其他用户");
            }
        }
        boolean hasSm2 = sm2UserId != null && pubX != null && pubY != null;
        boolean hasEnc = encKey != null;
        if (ukeyId != null && !hasSm2 && !hasEnc) {
            throw new IllegalArgumentException("绑定 UKey 时需填写 SM2 公钥身份，或增强算法密钥");
        }
        if (hasSm2 && (sm2UserId == null || pubX == null || pubY == null)) {
            throw new IllegalArgumentException("SM2 绑定需同时填写身份与公钥 X/Y");
        }
        if (encKey != null && encKey.length() != 32) {
            throw new IllegalArgumentException("增强算法密钥须为 32 位十六进制");
        }
        if (modes == null && ukeyId != null) {
            if (hasSm2 && hasEnc) {
                modes = "BOTH";
            } else if (hasEnc) {
                modes = "ENC";
            } else if (hasSm2) {
                modes = "SM2";
            }
        }
        if (modes != null) {
            modes = modes.toUpperCase();
            if (!List.of("SM2", "ENC", "BOTH").contains(modes)) {
                throw new IllegalArgumentException("ukeyAuthModes 仅支持 SM2 / ENC / BOTH");
            }
        }
        Integer ukeyRequired = request.ukeyRequired();
        if (ukeyRequired != null && ukeyRequired != 0 && ukeyRequired != 1) {
            throw new IllegalArgumentException("ukeyRequired 仅支持 null（继承）/ 1（强制）/ 0（不强制）");
        }
        repository.updateUserUkey(userId, ukeyId, sm2UserId, pubX, pubY, encKey, modes, ukeyRequired);
        auditService.record(
                "UPDATE_USER_UKEY",
                "USER",
                userId,
                ukeyId == null ? "清除用户 UKey 绑定" : "更新用户 UKey 绑定 " + ukeyId);
    }

    RoleAdminView createRole(CreateRoleRequest request) {
        Long id = repository.createRole(
                normalize(request.code()).toUpperCase(),
                normalize(request.name()),
                "CUSTOM");
        auditService.record("CREATE_ROLE", "ROLE", id, "创建角色 " + request.code());
        return repository.roles().stream().filter(role -> role.id().equals(id)).findFirst().orElseThrow();
    }

    MenuAdminView createMenu(CreateMenuRequest request) {
        Long id = repository.createMenu(
                normalize(request.code()).toUpperCase(),
                normalize(request.title()),
                normalize(request.path()),
                normalize(request.permissionCode()).toUpperCase(),
                request.parentId(),
                request.sortOrder(),
                request.enabled() == null || request.enabled());
        auditService.record("CREATE_MENU", "MENU", id, "创建菜单 " + request.code());
        return repository.menusAll(request.code()).stream().findFirst().orElseThrow();
    }

    void updateMenu(Long menuId, UpdateMenuRequest request) {
        repository.updateMenu(
                menuId,
                normalize(request.title()),
                normalize(request.path()),
                normalize(request.permissionCode()).toUpperCase(),
                request.parentId(),
                request.sortOrder(),
                request.enabled() == null || request.enabled());
        auditService.record("UPDATE_MENU", "MENU", menuId, "更新菜单 " + request.title());
    }

    void reorderMenus(MenuReorderRequest request) {
        List<MenuOrderItem> items = request.items() == null ? List.of() : request.items();
        if (items.isEmpty()) {
            throw new IllegalArgumentException("排序项不能为空");
        }
        repository.reorderMenus(items);
        auditService.record("REORDER_MENUS", "MENU", items.size(), "调整菜单树排序");
    }

    void updateRolePermissions(Long roleId, CodesRequest request) {
        List<String> codes = normalizeCodes(request.codes());
        repository.replaceRolePermissions(roleId, codes);
        auditService.record("UPDATE_ROLE_PERMISSIONS", "ROLE", roleId, "更新角色功能权限 " + codes);
    }

    void updateRoleOrganizations(Long roleId, CodesRequest request) {
        List<String> codes = normalizeOrganizationCodes(request.codes());
        repository.replaceRoleOrganizations(roleId, codes);
        auditService.record("UPDATE_ROLE_ORGANIZATIONS", "ROLE", roleId, "更新角色单位范围 " + codes);
    }

    List<SecurityAuditLog> auditLogs(Integer limit) {
        return auditService.recent(limit == null ? 50 : limit);
    }

    PageResponse<SecurityAuditLog> auditLogs(
            String keyword,
            java.time.LocalDate fromDate,
            java.time.LocalDate toDate,
            PageRequest pageRequest) {
        return auditService.search(keyword, fromDate, toDate, pageRequest);
    }

    byte[] exportAuditLogsCsv(String keyword, java.time.LocalDate fromDate, java.time.LocalDate toDate) {
        return auditService.exportCsv(keyword, fromDate, toDate);
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

    private List<String> normalizeOrganizationCodes(List<String> codes) {
        if (codes == null) {
            return List.of();
        }
        return codes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeHex(String value) {
        String trimmed = blankToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }

    record UserAdminView(
            Long id,
            String username,
            String displayName,
            Boolean enabled,
            List<String> roleCodes,
            Boolean allOrganizations,
            String homeOrganizationCode,
            String ukeyId,
            String sm2UserId,
            String sm2PubkeyX,
            String sm2PubkeyY,
            String encAlgoKey,
            String ukeyAuthModes,
            Integer ukeyRequired) {
    }

    record RoleAdminView(Long id, String code, String name, String dataScope, List<String> permissionCodes, List<String> organizationCodes) {
    }

    record PermissionView(String code, String name, String category) {
    }

    record MenuAdminView(
            Long id,
            String code,
            String title,
            String path,
            String permissionCode,
            Long parentId,
            Integer sortOrder,
            Boolean enabled) {
    }

    record CreateUserRequest(String username, String password, String displayName, Boolean enabled) {
    }

    record CreateRoleRequest(String code, String name) {
    }

    record CreateMenuRequest(
            String code,
            String title,
            String path,
            String permissionCode,
            Long parentId,
            Integer sortOrder,
            Boolean enabled) {
    }

    record UpdateMenuRequest(
            String title,
            String path,
            String permissionCode,
            Long parentId,
            Integer sortOrder,
            Boolean enabled) {
    }

    record MenuOrderItem(Long id, Long parentId, Integer sortOrder) {
    }

    record MenuReorderRequest(List<MenuOrderItem> items) {
    }

    record CodesRequest(List<String> codes) {
    }

    record EnabledRequest(Boolean enabled) {
    }

    record BatchEnabledRequest(List<Long> userIds, Boolean enabled) {
    }

    record PasswordRequest(String password) {
    }

    record UkeyBindingRequest(
            String ukeyId,
            String sm2UserId,
            String sm2PubkeyX,
            String sm2PubkeyY,
            String encAlgoKey,
            String ukeyAuthModes,
            Integer ukeyRequired) {
    }

    record HomeOrganizationRequest(String organizationCode) {
    }

    record DataScopeRequest(Boolean allOrganizations, String organizationCode) {
    }
}
