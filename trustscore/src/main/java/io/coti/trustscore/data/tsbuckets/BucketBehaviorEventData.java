package io.coti.trustscore.data.tsbuckets;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.tsenums.FinalEventType;
import io.coti.trustscore.data.tsevents.BehaviorEventData;
import io.coti.trustscore.data.contributiondata.EventCountAndContributionData;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class BucketBehaviorEventData extends BucketData<BehaviorEventData> implements IEntity {

    private static final long serialVersionUID = -2070936228447916037L;
    private Map<FinalEventType, EventCountAndContributionData> actualScoresDataMap;
    private Map<FinalEventType, Map<LocalDate, Double>> oldEventsScoreDateMap;

    public BucketBehaviorEventData() {
        super();

        actualScoresDataMap = new ConcurrentHashMap<>();
        oldEventsScoreDateMap = new ConcurrentHashMap<>();

        Map<LocalDate, Double> oldFILLQUESTIONNAIREScoresDateMap = new ConcurrentHashMap<>();
        oldEventsScoreDateMap.put(FinalEventType.FILLQUESTIONNAIRE, oldFILLQUESTIONNAIREScoresDateMap);
        Map<LocalDate, Double> oldFALSEQUESTIONNAIREScoresDateMap = new ConcurrentHashMap<>();
        oldEventsScoreDateMap.put(FinalEventType.FALSEQUESTIONNAIRE, oldFALSEQUESTIONNAIREScoresDateMap);
        Map<LocalDate, Double> oldDOUBLESPENDINGScoresDateMap = new ConcurrentHashMap<>();
        oldEventsScoreDateMap.put(FinalEventType.DOUBLESPENDING, oldDOUBLESPENDINGScoresDateMap);
        Map<LocalDate, Double> oldINVALIDTXScoresDateMap = new ConcurrentHashMap<>();
        oldEventsScoreDateMap.put(FinalEventType.INVALIDTX, oldINVALIDTXScoresDateMap);
    }
}


