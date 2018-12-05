package io.coti.trustscore.config.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.coti.trustscore.data.Enums.BehaviorEventsScoreType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BehaviorEventsScore {

    private List<BaseEventScore> baseEventScoreList;


    public List<BaseEventScore> getBaseEventScoreList() {
        return baseEventScoreList;
    }

    public void setBaseEventScoreList(List<BaseEventScore> baseEventScoreList) {
        this.baseEventScoreList = baseEventScoreList;
    }

    public Map<BehaviorEventsScoreType, BaseEventScore> getBaseEventScoreMap() {
        return getBaseEventScoreList().stream().collect(
                Collectors.toMap(t -> BehaviorEventsScoreType.enumFromString(t.getName()), t -> t));
    }


}

