package com.dxsoft.rsgzgl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RsgzglApplication {

    public static void main(String[] args) {
        SpringApplication.run(RsgzglApplication.class, args);
    }
}
