package io.coti.trustscore.config.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.coti.trustscore.data.Enums.HighFrequencyEventScoreType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BehaviorHighFrequencyEventsScore {

    private List<HighFrequencyEventScore> highFrequencyEventsScoreList;

    public List<HighFrequencyEventScore> getHighFrequencyEventsScoreList() {
        return highFrequencyEventsScoreList;
    }

    public void setHighFrequencyEventsScoreList(List<HighFrequencyEventScore> highFrequencyEventsScoreList) {
        this.highFrequencyEventsScoreList = highFrequencyEventsScoreList;
    }

    public Map<HighFrequencyEventScoreType, HighFrequencyEventScore> getHighFrequencyEventScoreMap() {
        return highFrequencyEventsScoreList.stream().collect(
                Collectors.toMap(t -> HighFrequencyEventScoreType.enumFromString(t.getName()), t -> t));
    }
}