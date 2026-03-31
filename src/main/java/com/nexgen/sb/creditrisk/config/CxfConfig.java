package com.nexgen.sb.creditrisk.config;

import java.util.HashMap;
import java.util.Map;

import jakarta.xml.ws.Endpoint;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nexgen.sb.creditrisk.endpoint.CreditRiskSoapEndpoint;
import com.nexgen.sb.creditrisk.endpoint.GatewaySoapEndpoint;

@Configuration
public class CxfConfig {

    @Bean
    public Endpoint mainSoapEndpoint(Bus bus, CreditRiskSoapEndpoint soapEndpoint) {
        EndpointImpl endpoint = new EndpointImpl(bus, soapEndpoint);
        endpoint.publish("/ws/soap/creditrisk");
        endpoint.getInInterceptors().add(wss4jInInterceptor());
        return endpoint;
    }

    @Bean
    public Endpoint gatewaySoapEndpoint(Bus bus, GatewaySoapEndpoint gwEndpoint) {
        EndpointImpl endpoint = new EndpointImpl(bus, gwEndpoint);
        endpoint.publish("/ws/creditriskapi");
        endpoint.getInInterceptors().add(wss4jInInterceptor());
        return endpoint;
    }

    private WSS4JInInterceptor wss4jInInterceptor() {
        Map<String, Object> props = new HashMap<>();
        props.put("action", "UsernameToken");
        props.put("passwordType", "PasswordText");
        return new WSS4JInInterceptor(props);
    }
}
