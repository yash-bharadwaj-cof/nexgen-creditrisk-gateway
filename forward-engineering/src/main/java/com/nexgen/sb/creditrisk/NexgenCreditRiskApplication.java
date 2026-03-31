package com.nexgen.sb.creditrisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.nexgen.sb.creditrisk.config.BureauProperties;
import com.nexgen.sb.creditrisk.config.NexgenProperties;
import com.nexgen.sb.creditrisk.config.ScoringProperties;

@SpringBootApplication
@EnableConfigurationProperties({BureauProperties.class, ScoringProperties.class, NexgenProperties.class})
public class NexgenCreditRiskApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexgenCreditRiskApplication.class, args);
    }
}
