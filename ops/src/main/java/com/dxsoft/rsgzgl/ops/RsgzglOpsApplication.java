package com.dxsoft.rsgzgl.ops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RsgzglOpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(RsgzglOpsApplication.class, args);
    }
}
