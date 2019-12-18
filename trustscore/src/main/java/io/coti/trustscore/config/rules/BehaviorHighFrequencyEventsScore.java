package io.coti.trustscore.config.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.coti.trustscore.data.enums.HighFrequencyEventScoreType;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BehaviorHighFrequencyEventsScore {

    private List<HighFrequencyEventScore> highFrequencyEventsScoreList;

    public Map<HighFrequencyEventScoreType, HighFrequencyEventScore> getHighFrequencyEventScoreMap() {
        return highFrequencyEventsScoreList.stream().collect(
                Collectors.toMap(t -> HighFrequencyEventScoreType.enumFromString(t.getName()), t -> t));
    }
}