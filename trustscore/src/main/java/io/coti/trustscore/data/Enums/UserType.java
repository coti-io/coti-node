package io.coti.trustscore.data.Enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "type")
@XmlEnum
public enum UserType {
    @XmlEnumValue("merchant")
    MERCHANT,

    @XmlEnumValue("wallet")
    WALLET,

    @XmlEnumValue("node")
    FULL_NODE
}
