package io.coti.trustscore.config.rules;

import io.coti.trustscore.data.scoreenums.TrustScoreRangeType;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class RulesData {

    private List<ScoreRules> trustScores;
    private List<UserNetworkFeeByTrustScoreRange> userNetworkFeeByTrustScoreRangeList;

    public Map<String, ScoreRules> getClassToScoreRulesMap() {
        return trustScores.stream().collect(
                Collectors.toMap(t -> (t.getName()), t -> t));
    }

    public void setNetworkFee(List<UserNetworkFeeByTrustScoreRange> userNetworkFeeByTrustScoreRangeList) {
        this.userNetworkFeeByTrustScoreRangeList = userNetworkFeeByTrustScoreRangeList;
    }

    public Map<TrustScoreRangeType, UserNetworkFeeByTrustScoreRange> getTrustScoreRangeTypeToUserScoreMap() {
        return userNetworkFeeByTrustScoreRangeList.stream().collect(
                Collectors.toMap(t -> TrustScoreRangeType.enumFromString(t.getType()), t -> t));
    }
}












