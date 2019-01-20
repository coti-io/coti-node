package io.coti.trustscore.config.rules;


import io.coti.trustscore.data.Enums.InitialTrustScoreType;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class InitialTrustScoreEventsScore {

    private List<InitialTrustScoreEventScore> initialTrustScoreEventScoreList;

    public Map<InitialTrustScoreType, InitialTrustScoreEventScore> getInitialTrustScoreComponentMap() {
        return initialTrustScoreEventScoreList.stream().collect(
                Collectors.toMap(t -> InitialTrustScoreType.enumFromString(t.getName()), t -> t));
    }

    public InitialTrustScoreEventScore getComponentByType(InitialTrustScoreType initialTrustScoreType) {
        return initialTrustScoreEventScoreList.stream().filter(e -> e.getName().equals(initialTrustScoreType.name())).findFirst().get();
    }
}