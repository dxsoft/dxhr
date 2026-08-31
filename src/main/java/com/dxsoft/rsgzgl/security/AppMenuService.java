package com.dxsoft.rsgzgl.security;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
class AppMenuService {

    private final JdbcTemplate jdbcTemplate;
    private final AccessControlService accessControlService;

    AppMenuService(JdbcTemplate jdbcTemplate, AccessControlService accessControlService) {
        this.jdbcTemplate = jdbcTemplate;
        this.accessControlService = accessControlService;
    }

    List<MenuItem> currentUserMenus() {
        AppUserPrincipal user = accessControlService.currentUser();
        return jdbcTemplate.query("""
                SELECT code, title, path, permission_code, sort_order
                FROM app_menu
                WHERE enabled = 1
                ORDER BY sort_order, id
                """, (rs, rowNum) -> new MenuItem(
                rs.getString("code"),
                rs.getString("title"),
                rs.getString("path"),
                rs.getString("permission_code"),
                rs.getInt("sort_order")))
                .stream()
                .filter(menu -> menuVisible(user, menu))
                .toList();
    }

    private boolean menuVisible(AppUserPrincipal user, MenuItem menu) {
        if ("PERSONNEL".equals(menu.code()) || "ANNUAL_ASSESSMENT_MANAGEMENT".equals(menu.code())) {
            return user.permissions().contains("PERSONNEL_READ")
                    || user.permissions().contains("PERSONNEL_WRITE")
                    || user.permissions().contains("PERSONNEL_BASIC_READ")
                    || user.permissions().contains("PERSONNEL_BASIC_WRITE");
        }
        if ("PERSONNEL_APPROVAL_TRACKING".equals(menu.code())) {
            for (String permission : PersonnelFeaturePermissions.approvalTrackingReadAuthorities()) {
                if (user.permissions().contains(permission)) {
                    return true;
                }
            }
            return false;
        }
        if ("RETIREMENT_PROCESSING".equals(menu.code())
                || "RETIREE_PERSONNEL".equals(menu.code())
                || "RETIREMENT_RATIO_STANDARDS".equals(menu.code())
                || "RETIREMENT_APPROVAL_REPORT".equals(menu.code())
                || "RETIREMENT_DATA_EXCHANGE".equals(menu.code())) {
            return user.permissions().contains("RETIREMENT_READ")
                    || user.permissions().contains("RETIREMENT_WRITE");
        }
        return user.permissions().contains(menu.permissionCode());
    }

    record MenuItem(String code, String title, String path, String permissionCode, Integer sortOrder) {
    }
}
