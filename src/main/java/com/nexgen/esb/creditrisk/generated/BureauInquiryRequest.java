package com.nexgen.esb.creditrisk.generated;

import javax.xml.bind.annotation.*;

/**
 * Represents the outbound request to the external credit bureau SOAP service.
 */
@XmlRootElement(name = "BureauInquiryRequest", namespace = "http://ws.esb.nexgen.com/bureau/v1")
@XmlAccessorType(XmlAccessType.FIELD)
public class BureauInquiryRequest {

    @XmlElement
    private String requestId;

    @XmlElement
    private String timestamp;

    @XmlElement
    private BureauSubscriber subscriber;

    @XmlElement
    private BureauSubject subject;

    @XmlElement
    private String productType;

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public BureauSubscriber getSubscriber() { return subscriber; }
    public void setSubscriber(BureauSubscriber subscriber) { this.subscriber = subscriber; }
    public BureauSubject getSubject() { return subject; }
    public void setSubject(BureauSubject subject) { this.subject = subject; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
}
