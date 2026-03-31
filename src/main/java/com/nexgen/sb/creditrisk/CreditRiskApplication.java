package com.nexgen.sb.creditrisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot application entry point for the NexGen Credit Risk Assessment Gateway.
 * Replaces the legacy JBoss Fuse / OSGi bundle deployment model.
 */
@SpringBootApplication
public class CreditRiskApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditRiskApplication.class, args);
    }
}
