package io.coti.trustscore.rulesData;

import io.coti.trustscore.data.Enums.UserType;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UserAdapter extends XmlAdapter<UsersScoresByType, Map<UserType, UserScoresByType>> {
    @Override
    public Map<UserType, UserScoresByType> unmarshal(UsersScoresByType value) {
        Map<UserType, UserScoresByType> map = new HashMap<>();
        for (UserScoresByType userScoreByType : value.userScoresByTypeList)
            map.put(userScoreByType.type, userScoreByType);
        return map;
    }

    @Override
    public UsersScoresByType marshal(Map<UserType, UserScoresByType> map) {
        UsersScoresByType msgCont = new UsersScoresByType();
        Collection<UserScoresByType> usersScoreByType = map.values();
        msgCont.userScoresByTypeList = (usersScoreByType.toArray(new UserScoresByType[usersScoreByType.size()]));
        return msgCont;
    }
}
