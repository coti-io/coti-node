package io.coti.trustscore.config.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.coti.trustscore.data.enums.BehaviorEventsScoreType;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BehaviorEventsScore {

    private List<SuspiciousEventScore> suspiciousEventScoreList;

    public Map<BehaviorEventsScoreType, SuspiciousEventScore> getBaseEventScoreMap() {
        return this.getSuspiciousEventScoreList().stream().collect(
                Collectors.toMap(t -> BehaviorEventsScoreType.enumFromString(t.getName()), t -> t));
    }

}

