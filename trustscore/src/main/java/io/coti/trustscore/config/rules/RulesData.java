package io.coti.trustscore.config.rules;

import io.coti.trustscore.data.Enums.UserType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@XmlRootElement(name = "rules")
public class RulesData {

    public RulesData() {
    }

    private List<UserScoresByType> userScoresByTypeList;

    @XmlElement(name = "user")
    public List<UserScoresByType> getUserScoresByTypeList() {
        return userScoresByTypeList;
    }

    public void setUserScoresByTypeList( List<UserScoresByType> userScoresByTypeList) {
        this.userScoresByTypeList = userScoresByTypeList;
    }

    public UserScoresByType getUsersRules(UserType userType) {
        return getUserTypeToUserScoreMap().get(userType);
    }

    public Map<UserType, UserScoresByType> getUserTypeToUserScoreMap() {
        return userScoresByTypeList.stream().collect(
                Collectors.toMap(t -> UserType.enumFromString(t.getType()), t -> t));
    }
}



















