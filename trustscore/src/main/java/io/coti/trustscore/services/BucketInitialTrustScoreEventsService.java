package io.coti.trustscore.services;

import io.coti.trustscore.config.rules.RulesData;
import io.coti.trustscore.data.Buckets.BucketEventData;
import io.coti.trustscore.data.Buckets.BucketInitialTrustScoreEventsData;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Events.EventData;
import io.coti.trustscore.data.Events.InitialTrustScoreData;
import io.coti.trustscore.data.Events.InitialTrustScoreEventsData;
import io.coti.trustscore.services.calculationservices.BucketInitialTrustScoreEventsCalculator;
import io.coti.trustscore.services.interfaces.IBucketEventService;
import org.springframework.stereotype.Service;

@Service
public class BucketInitialTrustScoreEventsService implements IBucketEventService<InitialTrustScoreEventsData, BucketInitialTrustScoreEventsData> {

    @Override
    public BucketInitialTrustScoreEventsData addEventToCalculations(InitialTrustScoreEventsData initialTrustScoreEventsData,
                                                                    BucketInitialTrustScoreEventsData bucketInitialTrustScoreEventsData) {
        BucketInitialTrustScoreEventsCalculator bucketCalculator
                = new BucketInitialTrustScoreEventsCalculator(bucketInitialTrustScoreEventsData);
        bucketCalculator.decayScores(bucketInitialTrustScoreEventsData);

        double score = initialTrustScoreEventsData.getScore();
        InitialTrustScoreData initialTrustScoreData = new InitialTrustScoreData(initialTrustScoreEventsData.getInitialTrustScoreType(),
                score,
                score);
        bucketInitialTrustScoreEventsData.getInitialTrustTypeToInitialTrustScoreDataMap().put(initialTrustScoreEventsData.getInitialTrustScoreType(), initialTrustScoreData);
        return bucketInitialTrustScoreEventsData;
    }

    @Override
    public double getBucketSumScore(BucketEventData<? extends EventData> bucketInitialTrustScoreEventsData) {
        BucketInitialTrustScoreEventsCalculator bucketCalculator = new BucketInitialTrustScoreEventsCalculator(bucketInitialTrustScoreEventsData);
        // Decay on case that this is the first event, or first access to data today
        bucketCalculator.decayScores(bucketInitialTrustScoreEventsData);
        return bucketCalculator.getBucketSumScore();
    }

    @Override
    public EventType getBucketEventType() {
        return EventType.INITIAL_EVENT;
    }

    public void init(RulesData rulesData) {
        BucketInitialTrustScoreEventsCalculator.init(rulesData);
    }
}
