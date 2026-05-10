package com.dxsoft.rsgzgl.organization;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.security.AccessControlService;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final AccessControlService accessControlService;

    OrganizationService(OrganizationRepository organizationRepository, AccessControlService accessControlService) {
        this.organizationRepository = organizationRepository;
        this.accessControlService = accessControlService;
    }

    public PageResponse<OrganizationSummary> list(String keyword, PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        return PageResponse.of(
                organizationRepository.findAll(keyword, scope, pageRequest),
                pageRequest,
                organizationRepository.count(keyword, scope));
    }

    public PageResponse<OrganizationMaintenanceRecord> maintenanceRecords(String keyword, PageRequest pageRequest) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        return PageResponse.of(
                organizationRepository.findMaintenanceRecords(keyword, scope, pageRequest),
                pageRequest,
                organizationRepository.countMaintenanceRecords(keyword, scope));
    }

    public List<OrganizationTreeNode> tree(String keyword) {
        OrganizationScope scope = accessControlService.organizationScope(Optional.empty());
        return organizationRepository.findTree(scope, keyword);
    }
}
