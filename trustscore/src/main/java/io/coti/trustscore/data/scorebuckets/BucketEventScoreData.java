package io.coti.trustscore.data.scorebuckets;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.data.scoreevents.EventScoreData;
import io.coti.trustscore.data.parameters.EventCountAndContributionData;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class BucketEventScoreData extends BucketData<EventScoreData> implements IEntity {

    private static final long serialVersionUID = -2070936228447916037L;
    private Map<FinalScoreType, EventCountAndContributionData> actualScoresDataMap;
    private Map<FinalScoreType, Map<LocalDate, Double>> oldEventsScoreDateMap;

    public BucketEventScoreData() {
        super();

        actualScoresDataMap = new ConcurrentHashMap<>();
        oldEventsScoreDateMap = new ConcurrentHashMap<>();

        Map<LocalDate, Double> oldFILLQUESTIONNAIREScoresDateMap = new ConcurrentHashMap<>();
        oldEventsScoreDateMap.put(FinalScoreType.FILLQUESTIONNAIRE, oldFILLQUESTIONNAIREScoresDateMap);
        Map<LocalDate, Double> oldFALSEQUESTIONNAIREScoresDateMap = new ConcurrentHashMap<>();
        oldEventsScoreDateMap.put(FinalScoreType.FALSEQUESTIONNAIRE, oldFALSEQUESTIONNAIREScoresDateMap);
        Map<LocalDate, Double> oldDOUBLESPENDINGScoresDateMap = new ConcurrentHashMap<>();
        oldEventsScoreDateMap.put(FinalScoreType.DOUBLESPENDING, oldDOUBLESPENDINGScoresDateMap);
        Map<LocalDate, Double> oldINVALIDTXScoresDateMap = new ConcurrentHashMap<>();
        oldEventsScoreDateMap.put(FinalScoreType.INVALIDTX, oldINVALIDTXScoresDateMap);
    }
}


