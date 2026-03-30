package com.nexgen.esb.creditrisk.model;

import javax.xml.bind.annotation.*;

/**
 * Request type for credit risk assessment.
 */
@XmlRootElement(name = "CreditRiskRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class CreditRiskReqType {

    @XmlElement(required = true)
    private RequestHeader requestHeader;

    @XmlElement(required = true)
    private String applicantId;

    @XmlElement(required = true)
    private String firstName;

    @XmlElement(required = true)
    private String lastName;

    @XmlElement(required = true)
    private String dateOfBirth;

    @XmlElement(required = true)
    private String socialInsuranceNumber;

    @XmlElement
    private String employmentStatus;

    @XmlElement
    private Double annualIncome;

    @XmlElement
    private String province;

    @XmlElement
    private String postalCode;

    @XmlElement
    private String requestChannel;

    @XmlElement
    private String productType;

    @XmlElement
    private Double requestedAmount;

    public RequestHeader getRequestHeader() { return requestHeader; }
    public void setRequestHeader(RequestHeader requestHeader) { this.requestHeader = requestHeader; }
    public String getApplicantId() { return applicantId; }
    public void setApplicantId(String applicantId) { this.applicantId = applicantId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getSocialInsuranceNumber() { return socialInsuranceNumber; }
    public void setSocialInsuranceNumber(String sin) { this.socialInsuranceNumber = sin; }
    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }
    public Double getAnnualIncome() { return annualIncome; }
    public void setAnnualIncome(Double annualIncome) { this.annualIncome = annualIncome; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getRequestChannel() { return requestChannel; }
    public void setRequestChannel(String requestChannel) { this.requestChannel = requestChannel; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    public Double getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(Double requestedAmount) { this.requestedAmount = requestedAmount; }
}
