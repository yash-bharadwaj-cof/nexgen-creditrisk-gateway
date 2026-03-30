package com.nexgen.esb.creditrisk.generated;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class BureauInquiryResponse {

    @XmlElement
    private String requestId;

    @XmlElement
    private String responseId;

    @XmlElement
    private Integer creditScore;

    @XmlElement
    private Integer delinquencyCount;

    @XmlElement
    private Integer inquiryCount;

    @XmlElement
    private Integer openTradelineCount;

    @XmlElement
    private Double totalCreditLimit;

    @XmlElement
    private Double totalBalance;

    @XmlElement
    private String errorCode;

    @XmlElement
    private String errorMessage;

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getResponseId() { return responseId; }
    public void setResponseId(String responseId) { this.responseId = responseId; }
    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }
    public Integer getDelinquencyCount() { return delinquencyCount; }
    public void setDelinquencyCount(Integer delinquencyCount) { this.delinquencyCount = delinquencyCount; }
    public Integer getInquiryCount() { return inquiryCount; }
    public void setInquiryCount(Integer inquiryCount) { this.inquiryCount = inquiryCount; }
    public Integer getOpenTradelineCount() { return openTradelineCount; }
    public void setOpenTradelineCount(Integer openTradelineCount) { this.openTradelineCount = openTradelineCount; }
    public Double getTotalCreditLimit() { return totalCreditLimit; }
    public void setTotalCreditLimit(Double totalCreditLimit) { this.totalCreditLimit = totalCreditLimit; }
    public Double getTotalBalance() { return totalBalance; }
    public void setTotalBalance(Double totalBalance) { this.totalBalance = totalBalance; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
