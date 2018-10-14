package io.coti.trustscore.rulesData;

import javax.xml.bind.annotation.XmlElement;

public class UsersScoresByType {
    @XmlElement(name = "user")
    public UserScoresByType[] userScoresByTypeList;
}
