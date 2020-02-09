package io.coti.trustscore.services.calculationservices;

import io.coti.trustscore.config.rules.InitialTrustScoreEventScore;
import io.coti.trustscore.config.rules.InitialTrustScoreEventsScore;
import io.coti.trustscore.config.rules.RulesData;
import io.coti.trustscore.data.Buckets.BucketInitialTrustScoreEventsData;
import io.coti.trustscore.data.Enums.InitialTrustScoreType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.InitialTrustScoreData;

import java.util.Map;
import java.util.stream.Collectors;

public class BucketInitialTrustScoreEventsCalculator extends BucketCalculator {

    private static Map<UserType, InitialTrustScoreEventsScore> userTypeToInitialTrustEventsScoreMap;
    private BucketInitialTrustScoreEventsData bucketInitialTrustScoreEventsData;
    private InitialTrustScoreEventsScore initialTrustScoreEventsScore;

    public BucketInitialTrustScoreEventsCalculator(BucketInitialTrustScoreEventsData bucketInitialTrustScoreEventsData) {
        this.bucketInitialTrustScoreEventsData = bucketInitialTrustScoreEventsData;
        initialTrustScoreEventsScore = userTypeToInitialTrustEventsScoreMap.get(bucketInitialTrustScoreEventsData.getUserType());
    }

    public static void init(RulesData rulesData) {
        userTypeToInitialTrustEventsScoreMap = rulesData.getUserTypeToUserScoreMap().entrySet().stream().
                collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getInitialTrustScore()));
    }

    @Override
    public void setCurrentScores() {

    }

    @Override
    protected void decayDailyEventScoresType(int daysDiff) {
        for (Map.Entry<InitialTrustScoreType, InitialTrustScoreData> entry
                : bucketInitialTrustScoreEventsData.getInitialTrustTypeToInitialTrustScoreDataMap().entrySet()) {
            InitialTrustScoreEventScore initialTrustScoreEventScore = getInitialTrustScoreEventScoreByEventScoreType(entry.getKey());
            EventDecay initialTrustScoreEventDecay = new EventDecay(initialTrustScoreEventScore, entry.getValue().getDecayedTrustScore());
            double decayedScore = (double) new DecayCalculator().calculateEntry(initialTrustScoreEventDecay, daysDiff).getValue();
            entry.getValue().setDecayedTrustScore(decayedScore);
        }
    }

    private double getWeightByEventScore(InitialTrustScoreType eventScoreType) {
        if (initialTrustScoreEventsScore.getInitialTrustScoreComponentMap().get(eventScoreType) == null) {
            return 0;
        }
        return initialTrustScoreEventsScore.getInitialTrustScoreComponentMap().get(eventScoreType).getWeight();
    }

    private InitialTrustScoreEventScore getInitialTrustScoreEventScoreByEventScoreType(InitialTrustScoreType initialTrustScoreType) {
        return initialTrustScoreEventsScore.getInitialTrustScoreComponentMap().get(initialTrustScoreType);
    }


    public double getBucketSumScore() {
        double sumScore = 0;
        for (Map.Entry<InitialTrustScoreType, InitialTrustScoreData> entry
                : bucketInitialTrustScoreEventsData.getInitialTrustTypeToInitialTrustScoreDataMap().entrySet()) {
            sumScore += entry.getValue().getDecayedTrustScore() * getWeightByEventScore(entry.getKey());
        }
        return sumScore;
    }
}
