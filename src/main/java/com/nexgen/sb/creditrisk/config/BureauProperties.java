package com.nexgen.sb.creditrisk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the external credit bureau integration.
 * Values are bound from application properties prefixed with {@code nexgen.bureau}.
 */
@Component
public class BureauProperties {

    @Value("${nexgen.bureau.request-id-prefix:nexgen.com-}")
    private String requestIdPrefix;

    @Value("${nexgen.bureau.subscriber-code:}")
    private String subscriberCode;

    @Value("${nexgen.bureau.subscriber-name:}")
    private String subscriberName;

    @Value("${nexgen.bureau.stub-enabled:true}")
    private boolean stubEnabled;

    public String getRequestIdPrefix() { return requestIdPrefix; }
    public void setRequestIdPrefix(String requestIdPrefix) { this.requestIdPrefix = requestIdPrefix; }

    public String getSubscriberCode() { return subscriberCode; }
    public void setSubscriberCode(String subscriberCode) { this.subscriberCode = subscriberCode; }

    public String getSubscriberName() { return subscriberName; }
    public void setSubscriberName(String subscriberName) { this.subscriberName = subscriberName; }

    public boolean isStubEnabled() { return stubEnabled; }
    public void setStubEnabled(boolean stubEnabled) { this.stubEnabled = stubEnabled; }
}
