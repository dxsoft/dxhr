package com.dxsoft.rsgzgl.exchange.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rsgzgl.exchange")
public class ExchangeDeploymentProperties {

    /**
     * SHARED: same database — notifications reach peer organization users directly.
     * STANDALONE: separate deployments — export/import creates local OUTBOUND/INBOUND reminders.
     */
    private String deploymentMode = "STANDALONE";

    public boolean sharedDatabase() {
        return "SHARED".equalsIgnoreCase(deploymentMode == null ? "" : deploymentMode.trim());
    }

    public String getDeploymentMode() {
        return deploymentMode;
    }

    public void setDeploymentMode(String deploymentMode) {
        this.deploymentMode = deploymentMode;
    }
}
