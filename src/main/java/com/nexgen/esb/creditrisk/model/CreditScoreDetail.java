package com.nexgen.esb.creditrisk.model;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class CreditScoreDetail {

    @XmlElement
    private Integer bureauScore;

    @XmlElement
    private String bureauScoreRange;

    @XmlElement
    private Integer delinquencyCount;

    @XmlElement
    private Integer inquiryCount;

    @XmlElement
    private Integer openAccountCount;

    @XmlElement
    private Double totalCreditLimit;

    @XmlElement
    private Double totalBalance;

    @XmlElement
    private Double utilizationRate;

    public Integer getBureauScore() { return bureauScore; }
    public void setBureauScore(Integer bureauScore) { this.bureauScore = bureauScore; }
    public String getBureauScoreRange() { return bureauScoreRange; }
    public void setBureauScoreRange(String bureauScoreRange) { this.bureauScoreRange = bureauScoreRange; }
    public Integer getDelinquencyCount() { return delinquencyCount; }
    public void setDelinquencyCount(Integer delinquencyCount) { this.delinquencyCount = delinquencyCount; }
    public Integer getInquiryCount() { return inquiryCount; }
    public void setInquiryCount(Integer inquiryCount) { this.inquiryCount = inquiryCount; }
    public Integer getOpenAccountCount() { return openAccountCount; }
    public void setOpenAccountCount(Integer openAccountCount) { this.openAccountCount = openAccountCount; }
    public Double getTotalCreditLimit() { return totalCreditLimit; }
    public void setTotalCreditLimit(Double totalCreditLimit) { this.totalCreditLimit = totalCreditLimit; }
    public Double getTotalBalance() { return totalBalance; }
    public void setTotalBalance(Double totalBalance) { this.totalBalance = totalBalance; }
    public Double getUtilizationRate() { return utilizationRate; }
    public void setUtilizationRate(Double utilizationRate) { this.utilizationRate = utilizationRate; }
}
