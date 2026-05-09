package com.dxsoft.rsgzgl.systemconfig;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    SystemConfigService(SystemConfigRepository systemConfigRepository) {
        this.systemConfigRepository = systemConfigRepository;
    }

    public PageResponse<LocalPolicyConfig> localPolicies(String keyword, PageRequest pageRequest) {
        return PageResponse.of(
                systemConfigRepository.findLocalPolicies(keyword, pageRequest),
                pageRequest,
                systemConfigRepository.countLocalPolicies(keyword));
    }

    public List<SystemOptionConfig> systemOptions() {
        return systemConfigRepository.findSystemOptions();
    }
}
