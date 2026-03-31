package com.nexgen.esb.creditrisk.model;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class RequestHeader {

    @XmlElement(required = true)
    private String transactionId;

    @XmlElement(required = true)
    private String timestamp;

    @XmlElement
    private String sourceSystem;

    @XmlElement
    private String userId;

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getSourceSystem() { return sourceSystem; }
    public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
