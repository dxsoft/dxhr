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
                SELECT code, title, path, permission_code
                FROM app_menu
                WHERE enabled = 1
                ORDER BY sort_order, id
                """, (rs, rowNum) -> new MenuItem(
                rs.getString("code"),
                rs.getString("title"),
                rs.getString("path"),
                rs.getString("permission_code")))
                .stream()
                .filter(menu -> menuVisible(user, menu))
                .toList();
    }

    private boolean menuVisible(AppUserPrincipal user, MenuItem menu) {
        if ("PERSONNEL".equals(menu.code()) || "ANNUAL_ASSESSMENT_MANAGEMENT".equals(menu.code())) {
            return user.permissions().contains("PERSONNEL_READ")
                    || user.permissions().contains("PERSONNEL_WRITE");
        }
        return user.permissions().contains(menu.permissionCode());
    }

    record MenuItem(String code, String title, String path, String permissionCode) {
    }
}
