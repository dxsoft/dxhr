package com.dxsoft.rsgzgl.security.ukey;

import com.dxsoft.rsgzgl.license.LicenseService;
import com.dxsoft.rsgzgl.license.LicenseStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UkeyRequirementEvaluator {

    private final boolean envEnabled;
    private final boolean envRequired;
    private final LicenseService licenseService;

    public UkeyRequirementEvaluator(
            @Value("${rsgzgl.ukey.enabled:true}") boolean envEnabled,
            @Value("${rsgzgl.ukey.required:false}") boolean envRequired,
            LicenseService licenseService) {
        this.envEnabled = envEnabled;
        this.envRequired = envRequired;
        this.licenseService = licenseService;
    }

    public boolean ukeyEnabled() {
        LicenseStatus status = licenseService.status();
        if (status.authorized() && status.ukeyEnabled() != null) {
            return status.ukeyEnabled();
        }
        return envEnabled;
    }

    public boolean globalRequired() {
        if (!ukeyEnabled()) {
            return false;
        }
        LicenseStatus status = licenseService.status();
        if (status.authorized() && status.ukeyRequired() != null) {
            return status.ukeyRequired();
        }
        return envRequired;
    }

    /**
     * @param userUkeyRequired NULL inherit global, 1 force, 0 exempt
     */
    public boolean effectiveRequire(Integer userUkeyRequired) {
        if (!ukeyEnabled()) {
            return false;
        }
        if (userUkeyRequired != null) {
            return userUkeyRequired == 1;
        }
        return globalRequired();
    }
}
