package io.coti.trustscore.services.calculationservices;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.config.rules.CompensableEventScore;
import io.coti.trustscore.config.rules.CompensableEventsScore;
import io.coti.trustscore.config.rules.RulesData;
import io.coti.trustscore.data.Buckets.BucketNotFulfilmentEventsData;
import io.coti.trustscore.data.Enums.CompensableEventScoreType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.NotFulfilmentToClientContributionData;
import javafx.util.Pair;

import java.util.Map;
import java.util.stream.Collectors;

public class BucketNotFulfilmentEventsCalculator extends BucketCalculator {

    private static Map<UserType, CompensableEventsScore> userTypeToBehaviorEventsScoreMap;
    private BucketNotFulfilmentEventsData bucketNotFulfilmentEventsData;
    private CompensableEventScore compensableEventScore;

    public BucketNotFulfilmentEventsCalculator(BucketNotFulfilmentEventsData bucketNotFulfilmentEventsData) {
        this.bucketNotFulfilmentEventsData = bucketNotFulfilmentEventsData;
        CompensableEventsScore compensableEventsScore = userTypeToBehaviorEventsScoreMap.get(bucketNotFulfilmentEventsData.getUserType());
        compensableEventScore = compensableEventsScore.getCompensableEventScoreMap().get(CompensableEventScoreType.NON_FULFILMENT);
    }

    public static void init(RulesData rulesData) {
        userTypeToBehaviorEventsScoreMap = rulesData.getUserTypeToUserScoreMap().entrySet().stream().
                collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getCompensableEventsScore()));
    }

    @Override
    protected void decayDailyEventScoresType(int daysDiff) {
        bucketNotFulfilmentEventsData.getClientHashToNotFulfilmentContributionMap().values().forEach(clientNotFulfilmentToClientContributionData -> {
            decayedFine(clientNotFulfilmentToClientContributionData, daysDiff);
            decayedTail(clientNotFulfilmentToClientContributionData, daysDiff);
        });
    }

    private void decayedTail(NotFulfilmentToClientContributionData clientNotFulfilmentToClientContributionData, int daysDiff) {
        EventDecay notFulfilmentEventDecay = new EventDecay(compensableEventScore, clientNotFulfilmentToClientContributionData.getTail());
        clientNotFulfilmentToClientContributionData.setTail((double) new DecayCalculator().calculateEntry(notFulfilmentEventDecay, daysDiff).getValue());
    }

    private void decayedFine(NotFulfilmentToClientContributionData clientNotFulfilmentToClientContributionData, int daysDiff) {
        for (int i = 0; i < daysDiff; i++) {
            clientNotFulfilmentToClientContributionData
                    .setFine((double) clientNotFulfilmentToClientContributionData.getFine()
                                    + (compensableEventScore.getWeight1()*Math.signum(clientNotFulfilmentToClientContributionData.getCurrentDebt())
                                    + compensableEventScore.getWeight2() * clientNotFulfilmentToClientContributionData.getCurrentDebt()/10000
                                    + clientNotFulfilmentToClientContributionData.getFine()) * 1/365);
        }
    }

    private double calculateFine(NotFulfilmentToClientContributionData notFulfilmentToClientContributionData) {
        return ((compensableEventScore.getWeight1())*(Math.signum(notFulfilmentToClientContributionData.getCurrentDebt()))
                + (compensableEventScore.getWeight2()) * notFulfilmentToClientContributionData.getCurrentDebt()/10000) * 1/365;
    }

    public void setCurrentScoresForSpecificClient(boolean isDebtDecreasing, Hash clientHash) {
        NotFulfilmentToClientContributionData notFulfilmentToClientContributionData
                = bucketNotFulfilmentEventsData.getClientHashToNotFulfilmentContributionMap().get(clientHash);
        if (isDebtDecreasing) {
            notFulfilmentToClientContributionData.setTail(notFulfilmentToClientContributionData.getTail()
                    + notFulfilmentToClientContributionData.getFine());

        }
        notFulfilmentToClientContributionData.setFine(calculateFine(notFulfilmentToClientContributionData));
    }

    public double getBucketSumScore(BucketNotFulfilmentEventsData bucketNotFulfilmentEventsData) {
        return bucketNotFulfilmentEventsData.getClientHashToNotFulfilmentContributionMap().values()
                .stream().mapToDouble(o -> o.getTail() + o.getFine()).sum() * compensableEventScore.getWeight();
    }

    @Override
    public void setCurrentScores() {

    }
}

