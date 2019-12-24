package io.coti.trustscore.data.tsbuckets;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.tsenums.FinalEventType;
import io.coti.trustscore.data.tsevents.FrequencyBasedEventData;
import io.coti.trustscore.data.contributiondata.FrequencyBasedCountAndContributionData;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class BucketFrequencyBasedEventData extends BucketData<FrequencyBasedEventData> implements IEntity {

    private static final long serialVersionUID = -8499437987144276842L;
    private Map<FinalEventType, FrequencyBasedCountAndContributionData> actualScoresDataMap;
    private Map<FinalEventType, Map<LocalDate, Double>> oldEventsScoreDateMap;
    private Map<FinalEventType, Map<LocalDate, Integer>> oldEventsCountDateMap;

    public BucketFrequencyBasedEventData() {
        super();

        actualScoresDataMap = new ConcurrentHashMap<>();
        oldEventsScoreDateMap = new ConcurrentHashMap<>();
        oldEventsCountDateMap = new ConcurrentHashMap<>();

        Map<LocalDate, Integer> oldCountsDateMap = new ConcurrentHashMap<>();
        oldEventsCountDateMap.put(FinalEventType.CLAIM, oldCountsDateMap);
        Map<LocalDate, Double> oldScoresDateMap = new ConcurrentHashMap<>();
        oldEventsScoreDateMap.put(FinalEventType.CLAIM, oldScoresDateMap);
    }
}
