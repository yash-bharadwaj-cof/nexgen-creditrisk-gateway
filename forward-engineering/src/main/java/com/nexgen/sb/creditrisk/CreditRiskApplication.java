package com.nexgen.sb.creditrisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

import com.nexgen.sb.creditrisk.config.BureauProperties;
import com.nexgen.sb.creditrisk.config.NexgenProperties;
import com.nexgen.sb.creditrisk.config.ScoringProperties;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties({BureauProperties.class, ScoringProperties.class, NexgenProperties.class})
public class CreditRiskApplication {
    public static void main(String[] args) {
        SpringApplication.run(CreditRiskApplication.class, args);
    }
}
