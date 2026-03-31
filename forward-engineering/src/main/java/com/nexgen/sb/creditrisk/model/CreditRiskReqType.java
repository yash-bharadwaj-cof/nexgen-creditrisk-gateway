package com.nexgen.sb.creditrisk.model;

/**
 * Request type for credit risk assessment.
 */
public class CreditRiskReqType {

    private String applicantId;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String socialInsuranceNumber;
    private String employmentStatus;
    private Double annualIncome;
    private String province;
    private String postalCode;
    private String requestChannel;
    private String productType;
    private Double requestedAmount;

    public String getApplicantId() { return applicantId; }
    public void setApplicantId(String applicantId) { this.applicantId = applicantId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getSocialInsuranceNumber() { return socialInsuranceNumber; }
    public void setSocialInsuranceNumber(String socialInsuranceNumber) { this.socialInsuranceNumber = socialInsuranceNumber; }

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
