package com.nexgen.sb.creditrisk.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Apache CXF configuration for SOAP endpoints.
 *
 * <p>Provides a {@link WSS4JInInterceptor} bean that enforces WS-Security
 * UsernameToken authentication on inbound SOAP messages. This interceptor
 * should be wired to any {@code Endpoint} that requires SOAP-level security.</p>
 */
@Configuration
public class CxfConfig {

    /**
     * WSS4J in-interceptor configured for {@code UsernameToken / PasswordText}.
     *
     * <p>This mirrors the legacy CXF {@code WSS4JInInterceptor} defined in
     * {@code blueprint.xml} and is consumed by SOAP endpoint beans that need
     * WS-Security enforcement.</p>
     *
     * @return configured {@link WSS4JInInterceptor}
     */
    @Bean
    public WSS4JInInterceptor wss4jInInterceptor() {
        Map<String, Object> props = new HashMap<>();
        props.put("action", "UsernameToken");
        props.put("passwordType", "PasswordText");
        return new WSS4JInInterceptor(props);
    }
}
