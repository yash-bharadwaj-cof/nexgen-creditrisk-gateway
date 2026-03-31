package com.nexgen.esb.creditrisk.generated;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class BureauSubject {

    @XmlElement
    private String firstName;

    @XmlElement
    private String lastName;

    @XmlElement
    private String dateOfBirth;

    @XmlElement
    private String socialInsuranceNumber;

    @XmlElement
    private String province;

    @XmlElement
    private String postalCode;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getSocialInsuranceNumber() { return socialInsuranceNumber; }
    public void setSocialInsuranceNumber(String sin) { this.socialInsuranceNumber = sin; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
}
