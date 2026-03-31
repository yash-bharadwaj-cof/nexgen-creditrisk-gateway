package com.nexgen.sb.creditrisk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Scoring configuration properties bound from application.properties/yaml
 * under the "scoring" prefix.
 */
@Component
@ConfigurationProperties(prefix = "scoring")
public class ScoringProperties {

    private String defaultStrategy = "standard";

    public String getDefaultStrategy() {
        return defaultStrategy;
    }

    public void setDefaultStrategy(String defaultStrategy) {
        this.defaultStrategy = defaultStrategy;
    }
}
