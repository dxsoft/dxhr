package com.dxsoft.rsgzgl.organization;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @GetMapping("/maintenance/{id}")
    OrganizationMaintenanceRecord maintenanceById(@PathVariable int id) {
        return organizationService.maintenanceRecordById(id);
    }

    @GetMapping("/by-code/{organizationCode}")
    OrganizationMaintenanceRecord maintenanceByCode(@PathVariable String organizationCode) {
        return organizationService.maintenanceRecordByCode(organizationCode);
    }

    @GetMapping("/tree")
    List<OrganizationTreeNode> tree(@RequestParam(required = false) String keyword) {
        return organizationService.tree(keyword);
    }

    @GetMapping("/field-options")
    OrganizationFieldOptions fieldOptions() {
        return organizationService.fieldOptions();
    }

    @PostMapping("/next-root-code")
    OrganizationCodeSuggestion nextRootCode() {
        return organizationService.nextRootCode();
    }

    @PostMapping("/{parentCode}/next-child-code")
    OrganizationCodeSuggestion nextChildCode(@PathVariable String parentCode) {
        return organizationService.nextChildCode(parentCode);
    }

    @PutMapping("/{id}")
    OrganizationMaintenanceRecord update(@PathVariable int id, @RequestBody OrganizationMaintenanceRequest request) {
        return organizationService.updateMaintenanceRecord(id, request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    OrganizationMaintenanceRecord create(@RequestBody OrganizationCreateRequest request) {
        return organizationService.createOrganization(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable int id) {
        organizationService.deleteOrganization(id);
    }
}
