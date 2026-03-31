package com.nexgen.sb.creditrisk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nexgen.bureau")
public class BureauProperties {

    private String endpointUrl;
    private String requestIdPrefix;
    private String subscriberCode;
    private String subscriberName;
    private int connectionTimeout = 30000;
    private int receiveTimeout = 30000;
    private boolean stubEnabled = true;

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public String getRequestIdPrefix() {
        return requestIdPrefix;
    }

    public void setRequestIdPrefix(String requestIdPrefix) {
        this.requestIdPrefix = requestIdPrefix;
    }

    public String getSubscriberCode() {
        return subscriberCode;
    }

    public void setSubscriberCode(String subscriberCode) {
        this.subscriberCode = subscriberCode;
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    public void setSubscriberName(String subscriberName) {
        this.subscriberName = subscriberName;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setReceiveTimeout(int receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    public boolean isStubEnabled() {
        return stubEnabled;
    }

    public void setStubEnabled(boolean stubEnabled) {
        this.stubEnabled = stubEnabled;
    }
}
