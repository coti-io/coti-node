package io.coti.trustscore.config.rules;

import io.coti.trustscore.data.enums.TrustScoreRangeType;
import io.coti.trustscore.data.enums.UserType;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class RulesData {

    private List<User> userList;
    private List<UserNetworkFeeByTrustScoreRange> userNetworkFeeByTrustScoreRangeList;

    public void setUser(List<User> userList) {
        this.userList = userList;
    }

    public User getUsersRules(UserType userType) {
        return getUserTypeToUserScoreMap().get(userType);
    }

    public Map<UserType, User> getUserTypeToUserScoreMap() {
        return userList.stream().collect(
                Collectors.toMap(t -> UserType.enumFromString(t.getType()), t -> t));
    }

    public void setNetworkFee(List<UserNetworkFeeByTrustScoreRange> userNetworkFeeByTrustScoreRangeList) {
        this.userNetworkFeeByTrustScoreRangeList = userNetworkFeeByTrustScoreRangeList;
    }

    public Map<TrustScoreRangeType, UserNetworkFeeByTrustScoreRange> getTrustScoreRangeTypeToUserScoreMap() {
        return userNetworkFeeByTrustScoreRangeList.stream().collect(
                Collectors.toMap(t -> TrustScoreRangeType.enumFromString(t.getType()), t -> t));
    }
}












