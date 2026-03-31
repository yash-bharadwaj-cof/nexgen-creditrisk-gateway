package com.nexgen.sb.creditrisk.service;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.ws.WebFault;

@WebFault(name = "CreditRiskServiceException",
          targetNamespace = "http://ws.esb.nexgen.com/schema/creditrisk/v1")
public class CreditRiskServiceException extends Exception {

    private static final long serialVersionUID = 1L;
    private DetailException detailException;

    public CreditRiskServiceException(String message) {
        super(message);
    }

    public CreditRiskServiceException(String message, DetailException faultInfo) {
        super(message);
        this.detailException = faultInfo;
    }

    public CreditRiskServiceException(String message, DetailException faultInfo, Throwable cause) {
        super(message, cause);
        this.detailException = faultInfo;
    }

    public DetailException getFaultInfo() {
        return detailException;
    }

    @XmlRootElement(name = "DetailException")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DetailException {

        @XmlElement
        private String errorCode;

        @XmlElement
        private String errorMessage;

        @XmlElement
        private String correlationId;

        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    }
}
