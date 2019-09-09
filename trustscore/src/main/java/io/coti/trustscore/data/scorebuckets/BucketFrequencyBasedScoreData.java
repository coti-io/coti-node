package io.coti.trustscore.data.scorebuckets;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.data.scoreevents.FrequencyBasedScoreData;
import io.coti.trustscore.data.parameters.FrequencyBasedCountAndContributionData;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class BucketFrequencyBasedScoreData extends BucketData<FrequencyBasedScoreData> implements IEntity {

    private static final long serialVersionUID = -8499437987144276842L;
    private Map<FinalScoreType, FrequencyBasedCountAndContributionData> actualScoresDataMap;
    private Map<FinalScoreType, Map<LocalDate, Double>> oldEventsScoreDateMap;
    private Map<FinalScoreType, Map<LocalDate, Integer>> oldEventsCountDateMap;

    public BucketFrequencyBasedScoreData() {
        super();

        actualScoresDataMap = new ConcurrentHashMap<>();
        oldEventsScoreDateMap = new ConcurrentHashMap<>();
        oldEventsCountDateMap = new ConcurrentHashMap<>();

        Map<LocalDate, Integer> oldCountsDateMap = new ConcurrentHashMap<>();
        oldEventsCountDateMap.put(FinalScoreType.CLAIM, oldCountsDateMap);
        Map<LocalDate, Double> oldScoresDateMap = new ConcurrentHashMap<>();
        oldEventsScoreDateMap.put(FinalScoreType.CLAIM, oldScoresDateMap);
    }
}
