package com.dxsoft.rsgzgl;

import com.dxsoft.rsgzgl.exchange.notification.ExchangeDeploymentProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({ExchangeDeploymentProperties.class, com.dxsoft.rsgzgl.workflow.PayrollWorkflowProperties.class})
public class RsgzglApplication {

    public static void main(String[] args) {
        SpringApplication.run(RsgzglApplication.class, args);
    }
}
