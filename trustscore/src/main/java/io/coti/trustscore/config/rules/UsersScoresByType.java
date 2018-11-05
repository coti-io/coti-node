package io.coti.trustscore.config.rules;

import javax.xml.bind.annotation.XmlElement;

public class UsersScoresByType {
    @XmlElement(name = "user")
    public UserScoresByType[] userScoresByTypeList;
}
