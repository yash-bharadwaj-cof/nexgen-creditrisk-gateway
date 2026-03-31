package com.nexgen.esb.creditrisk.model;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class EmploymentRiskDetail {

    @XmlElement
    private String employmentType;

    @XmlElement
    private Integer yearsEmployed;

    @XmlElement
    private String industryCategory;

    @XmlElement
    private String riskLevel;

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
    public Integer getYearsEmployed() { return yearsEmployed; }
    public void setYearsEmployed(Integer yearsEmployed) { this.yearsEmployed = yearsEmployed; }
    public String getIndustryCategory() { return industryCategory; }
    public void setIndustryCategory(String industryCategory) { this.industryCategory = industryCategory; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
}
