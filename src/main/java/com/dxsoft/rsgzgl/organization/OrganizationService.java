package com.dxsoft.rsgzgl.organization;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public PageResponse<OrganizationSummary> list(String keyword, PageRequest pageRequest) {
        return PageResponse.of(
                organizationRepository.findAll(keyword, pageRequest),
                pageRequest,
                organizationRepository.count(keyword));
    }
}
