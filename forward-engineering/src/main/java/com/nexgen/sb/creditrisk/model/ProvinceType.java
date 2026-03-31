package com.nexgen.sb.creditrisk.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "ProvinceType")
@XmlEnum
public enum ProvinceType {

    @XmlEnumValue("ON") ON("ON"),
    @XmlEnumValue("QC") QC("QC"),
    @XmlEnumValue("BC") BC("BC"),
    @XmlEnumValue("AB") AB("AB"),
    @XmlEnumValue("MB") MB("MB"),
    @XmlEnumValue("SK") SK("SK"),
    @XmlEnumValue("NS") NS("NS"),
    @XmlEnumValue("NB") NB("NB"),
    @XmlEnumValue("NL") NL("NL"),
    @XmlEnumValue("PE") PE("PE"),
    @XmlEnumValue("NT") NT("NT"),
    @XmlEnumValue("YT") YT("YT"),
    @XmlEnumValue("NU") NU("NU");

    private final String value;

    ProvinceType(String v) { this.value = v; }

    public String value() { return value; }

    public static ProvinceType fromValue(String v) {
        for (ProvinceType c : ProvinceType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
