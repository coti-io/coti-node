package io.coti.trustscore.config.rules;

import io.coti.trustscore.data.Enums.UserType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;

@XmlRootElement(name = "rules")
public class RulesData {
    @XmlElement
    @XmlJavaTypeAdapter(UserAdapter.class)
    private HashMap<UserType, UserScoresByType> users;

    public RulesData() {
    }

    public RulesData(HashMap<UserType, UserScoresByType> users) {
        this.users = users;
    }

    public HashMap<UserType, UserScoresByType> getUsersRules() {
        return users;
    }

    public UserScoresByType getUsersRules(UserType userType) {
        return users.get(userType);
    }
}



















