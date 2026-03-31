package com.nexgen.sb.creditrisk.model;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class IncomeVerificationDetail {

    @XmlElement
    private String verificationStatus;

    @XmlElement
    private Double reportedIncome;

    @XmlElement
    private Double verifiedIncome;

    @XmlElement
    private String incomeSource;

    @XmlElement
    private Double debtToIncomeRatio;

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
    public Double getReportedIncome() { return reportedIncome; }
    public void setReportedIncome(Double reportedIncome) { this.reportedIncome = reportedIncome; }
    public Double getVerifiedIncome() { return verifiedIncome; }
    public void setVerifiedIncome(Double verifiedIncome) { this.verifiedIncome = verifiedIncome; }
    public String getIncomeSource() { return incomeSource; }
    public void setIncomeSource(String incomeSource) { this.incomeSource = incomeSource; }
    public Double getDebtToIncomeRatio() { return debtToIncomeRatio; }
    public void setDebtToIncomeRatio(Double debtToIncomeRatio) { this.debtToIncomeRatio = debtToIncomeRatio; }
}
