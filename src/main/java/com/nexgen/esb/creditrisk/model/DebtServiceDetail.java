package com.nexgen.esb.creditrisk.model;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class DebtServiceDetail {

    @XmlElement
    private Double totalMonthlyDebt;

    @XmlElement
    private Double totalMonthlyIncome;

    @XmlElement
    private Double debtServiceRatio;

    @XmlElement
    private Double totalDebtServiceRatio;

    @XmlElement
    private String affordabilityRating;

    public Double getTotalMonthlyDebt() { return totalMonthlyDebt; }
    public void setTotalMonthlyDebt(Double totalMonthlyDebt) { this.totalMonthlyDebt = totalMonthlyDebt; }
    public Double getTotalMonthlyIncome() { return totalMonthlyIncome; }
    public void setTotalMonthlyIncome(Double totalMonthlyIncome) { this.totalMonthlyIncome = totalMonthlyIncome; }
    public Double getDebtServiceRatio() { return debtServiceRatio; }
    public void setDebtServiceRatio(Double debtServiceRatio) { this.debtServiceRatio = debtServiceRatio; }
    public Double getTotalDebtServiceRatio() { return totalDebtServiceRatio; }
    public void setTotalDebtServiceRatio(Double totalDebtServiceRatio) { this.totalDebtServiceRatio = totalDebtServiceRatio; }
    public String getAffordabilityRating() { return affordabilityRating; }
    public void setAffordabilityRating(String affordabilityRating) { this.affordabilityRating = affordabilityRating; }
}
