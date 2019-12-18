package io.coti.trustscore.config.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.coti.trustscore.data.enums.CompensableEventScoreType;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompensableEventsScore {
    private List<CompensableEventScore> compensableEventScoreList;

    public Map<CompensableEventScoreType, CompensableEventScore> getCompensableEventScoreMap() {
        return getCompensableEventScoreList().stream().collect(
                Collectors.toMap(t -> CompensableEventScoreType.enumFromString(t.getName()), t -> t));
    }
}
