package io.coti.trustscore.config.rules;

import io.coti.trustscore.data.Enums.TrustScoreRangeType;
import io.coti.trustscore.data.Enums.UserType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RulesData {

    private List<UserScoresByType> userScoresByTypeList;

    private List<UserNetworkFeeByTrustScoreRange> userNetworkFeeByTrustScoreRangeList;

    public RulesData() {
    }

    public List<UserScoresByType> getUserScoresByTypeList() {
        return userScoresByTypeList;
    }

    public void setUser(List<UserScoresByType> userScoresByTypeList) {
        this.userScoresByTypeList = userScoresByTypeList;
    }

    public UserScoresByType getUsersRules(UserType userType) {
        return getUserTypeToUserScoreMap().get(userType);
    }

    public Map<UserType, UserScoresByType> getUserTypeToUserScoreMap() {
        return userScoresByTypeList.stream().collect(
                Collectors.toMap(t -> UserType.enumFromString(t.getType()), t -> t));
    }

    public List<UserNetworkFeeByTrustScoreRange> getUserNetworkFeeByTrustScoreRangeList() {
        return userNetworkFeeByTrustScoreRangeList;
    }

    public void setNetworkFee(List<UserNetworkFeeByTrustScoreRange> userNetworkFeeByTrustScoreRangeList) {
        this.userNetworkFeeByTrustScoreRangeList = userNetworkFeeByTrustScoreRangeList;
    }

    public Map<TrustScoreRangeType, UserNetworkFeeByTrustScoreRange> getTrustScoreRangeTypeToUserScoreMap() {
        return userNetworkFeeByTrustScoreRangeList.stream().collect(
                Collectors.toMap(t -> TrustScoreRangeType.enumFromString(t.getType()), t -> t));
    }
}












