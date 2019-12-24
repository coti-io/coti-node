package io.coti.trustscore.services.calculationservices;

import io.coti.trustscore.data.tsbuckets.BucketDocumentEventData;
import io.coti.trustscore.data.tsenums.FinalEventType;
import io.coti.trustscore.data.contributiondata.DocumentDecayedContributionData;
import io.coti.trustscore.data.parameters.UserParameters;
import io.coti.trustscore.services.BucketDocumentScoreService;
import io.coti.trustscore.utils.DatesCalculation;

import java.util.Map;

public class BucketDocumentScoreCalculator extends BucketScoresCalculator<BucketDocumentEventData> {

    public BucketDocumentScoreCalculator(BucketDocumentEventData bucketData) {
        super(bucketData);
    }

    @Override
    protected void decayDailyScores(int daysDiff) {
        for (Map.Entry<FinalEventType, DocumentDecayedContributionData> actualScoresDataMapEntry : bucketData.getActualScoresDataMap().entrySet()) {
            UserParameters userParameters = BucketDocumentScoreService.userParameters(actualScoresDataMapEntry.getKey(), bucketData.getUserType());
            if (userParameters != null) {
                double decayedScore = DatesCalculation.calculateDecay(userParameters.getSemiDecay(), actualScoresDataMapEntry.getValue().getDecayedTrustScore(), daysDiff);
                actualScoresDataMapEntry.getValue().setDecayedTrustScore(decayedScore);
            }
        }
    }

    public double getBucketSumScore() {
        double sumScore = 0;
        for (Map.Entry<FinalEventType, DocumentDecayedContributionData> actualScoresDataMapEntry : bucketData.getActualScoresDataMap().entrySet()) {
            UserParameters userParameters = BucketDocumentScoreService.userParameters(actualScoresDataMapEntry.getKey(), bucketData.getUserType());
            if (userParameters != null) {
                sumScore += actualScoresDataMapEntry.getValue().getDecayedTrustScore() * userParameters.getWeight();
            }
        }
        return sumScore;
    }
}



