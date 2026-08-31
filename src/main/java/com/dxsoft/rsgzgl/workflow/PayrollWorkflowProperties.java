package com.dxsoft.rsgzgl.workflow;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rsgzgl.workflow")
public class PayrollWorkflowProperties {

    private boolean enabled = false;
    private boolean hideDataExchange = false;
    private boolean autoQueueOnApprove = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isHideDataExchange() {
        return hideDataExchange;
    }

    public void setHideDataExchange(boolean hideDataExchange) {
        this.hideDataExchange = hideDataExchange;
    }

    public boolean isAutoQueueOnApprove() {
        return autoQueueOnApprove;
    }

    public void setAutoQueueOnApprove(boolean autoQueueOnApprove) {
        this.autoQueueOnApprove = autoQueueOnApprove;
    }
}
