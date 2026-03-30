package com.nexgen.esb.creditrisk.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "RequestChannelType")
@XmlEnum
public enum RequestChannelType {

    @XmlEnumValue("ONLINE") ONLINE("ONLINE"),
    @XmlEnumValue("MOBILE") MOBILE("MOBILE"),
    @XmlEnumValue("BRANCH") BRANCH("BRANCH"),
    @XmlEnumValue("BROKER") BROKER("BROKER"),
    @XmlEnumValue("CALL_CENTER") CALL_CENTER("CALL_CENTER"),
    @XmlEnumValue("API") API("API"),
    @XmlEnumValue("BATCH") BATCH("BATCH");

    private final String value;

    RequestChannelType(String v) { this.value = v; }

    public String value() { return value; }

    public static RequestChannelType fromValue(String v) {
        for (RequestChannelType c : RequestChannelType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
