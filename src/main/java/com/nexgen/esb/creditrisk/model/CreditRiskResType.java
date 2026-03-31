package com.nexgen.esb.creditrisk.model;

import jakarta.xml.bind.annotation.*;
import java.util.List;

/**
 * Response type for credit risk assessment.
 */
@XmlRootElement(name = "CreditRiskResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class CreditRiskResType {

    @XmlElement(required = true)
    private ResponseHeader responseHeader;

    @XmlElement
    private String applicantId;

    @XmlElement
    private String riskCategory;

    @XmlElement
    private Integer overallScore;

    @XmlElement
    private CreditScoreDetail creditScoreDetail;

    @XmlElement
    private IncomeVerificationDetail incomeVerification;

    @XmlElement
    private EmploymentRiskDetail employmentRisk;

    @XmlElement
    private DebtServiceDetail debtService;

    @XmlElementWrapper(name = "riskFactors")
    @XmlElement(name = "factor")
    private List<String> riskFactors;

    @XmlElement
    private String recommendation;

    @XmlElement
    private String accuracyCode;

    @XmlElement
    private String scoringModelVersion;

    public ResponseHeader getResponseHeader() { return responseHeader; }
    public void setResponseHeader(ResponseHeader responseHeader) { this.responseHeader = responseHeader; }
    public String getApplicantId() { return applicantId; }
    public void setApplicantId(String applicantId) { this.applicantId = applicantId; }
    public String getRiskCategory() { return riskCategory; }
    public void setRiskCategory(String riskCategory) { this.riskCategory = riskCategory; }
    public Integer getOverallScore() { return overallScore; }
    public void setOverallScore(Integer overallScore) { this.overallScore = overallScore; }
    public CreditScoreDetail getCreditScoreDetail() { return creditScoreDetail; }
    public void setCreditScoreDetail(CreditScoreDetail creditScoreDetail) { this.creditScoreDetail = creditScoreDetail; }
    public IncomeVerificationDetail getIncomeVerification() { return incomeVerification; }
    public void setIncomeVerification(IncomeVerificationDetail incomeVerification) { this.incomeVerification = incomeVerification; }
    public EmploymentRiskDetail getEmploymentRisk() { return employmentRisk; }
    public void setEmploymentRisk(EmploymentRiskDetail employmentRisk) { this.employmentRisk = employmentRisk; }
    public DebtServiceDetail getDebtService() { return debtService; }
    public void setDebtService(DebtServiceDetail debtService) { this.debtService = debtService; }
    public List<String> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public String getAccuracyCode() { return accuracyCode; }
    public void setAccuracyCode(String accuracyCode) { this.accuracyCode = accuracyCode; }
    public String getScoringModelVersion() { return scoringModelVersion; }
    public void setScoringModelVersion(String scoringModelVersion) { this.scoringModelVersion = scoringModelVersion; }
}
