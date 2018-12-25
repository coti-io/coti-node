package io.coti.trustscore.config.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.coti.trustscore.data.Enums.BehaviorEventsScoreType;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BehaviorEventsScore {

    private List<BaseEventScore> baseEventScoreList;

    public Map<BehaviorEventsScoreType, BaseEventScore> getBaseEventScoreMap() {
        return getBaseEventScoreList().stream().collect(
                Collectors.toMap(t -> BehaviorEventsScoreType.enumFromString(t.getName()), t -> t));
    }

}

