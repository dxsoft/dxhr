package com.dxsoft.rsgzgl.systemconfig;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system-config")
class SystemConfigController {

    private final SystemConfigService systemConfigService;

    SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @GetMapping("/local-policies")
    PageResponse<LocalPolicyConfig> localPolicies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return systemConfigService.localPolicies(keyword, PageRequest.of(page, size));
    }

    @GetMapping("/options")
    List<SystemOptionConfig> systemOptions() {
        return systemConfigService.systemOptions();
    }

    @PutMapping("/options")
    List<SystemOptionConfig> updateSystemOptions(@RequestBody SystemOptionUpdateRequest request) {
        return systemConfigService.updateSystemOptions(request);
    }

    @PutMapping("/local-policies/{id}")
    LocalPolicyConfig updateLocalPolicy(@PathVariable int id, @RequestBody LocalPolicyUpdateRequest request) {
        return systemConfigService.updateLocalPolicy(id, request);
    }
}
