package com.dxsoft.rsgzgl.organization;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizations")
class OrganizationController {

    private final OrganizationService organizationService;

    OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    PageResponse<OrganizationSummary> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return organizationService.list(keyword, PageRequest.of(page, size));
    }

    @GetMapping("/maintenance")
    PageResponse<OrganizationMaintenanceRecord> maintenance(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return organizationService.maintenanceRecords(keyword, PageRequest.of(page, size));
    }

    @GetMapping("/tree")
    List<OrganizationTreeNode> tree(@RequestParam(required = false) String keyword) {
        return organizationService.tree(keyword);
    }
}
