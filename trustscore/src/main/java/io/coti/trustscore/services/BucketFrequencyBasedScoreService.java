package io.coti.trustscore.services;

import io.coti.trustscore.config.rules.ScoreRules;
import io.coti.trustscore.config.rules.UserScoreRules;
import io.coti.trustscore.data.tsbuckets.BucketFrequencyBasedEventData;
import io.coti.trustscore.data.tsenums.FinalEventType;
import io.coti.trustscore.data.tsenums.EventType;
import io.coti.trustscore.data.tsenums.UserType;
import io.coti.trustscore.data.tsevents.FrequencyBasedEventData;
import io.coti.trustscore.data.contributiondata.FrequencyBasedCountAndContributionData;
import io.coti.trustscore.data.parameters.FrequencyBasedUserParameters;
import io.coti.trustscore.services.calculationservices.BucketFrequencyBasedScoreCalculator;
import io.coti.trustscore.services.interfaces.IBucketService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BucketFrequencyBasedScoreService implements IBucketService<FrequencyBasedEventData, BucketFrequencyBasedEventData> {

    private static Map<UserType, FrequencyBasedUserParameters> claimFrequencyBasedUserParametersMap;

    public static FrequencyBasedUserParameters userParameters(FinalEventType finalEventType, UserType userType) {
        if (finalEventType == FinalEventType.CLAIM) {
            return claimFrequencyBasedUserParametersMap.get(userType);
        } else {
            return null;
        }
    }

    public static void init(Map<String, ScoreRules> classToScoreRulesMap) {

        claimFrequencyBasedUserParametersMap = new ConcurrentHashMap<>();

        for (UserScoreRules userScoreRules : classToScoreRulesMap.get("ClaimFrequencyBasedScoreData").getUsers()) {
            claimFrequencyBasedUserParametersMap.put(UserType.enumFromString(userScoreRules.getUserType()), new FrequencyBasedUserParameters(userScoreRules));
        }
    }

    @Override
    public BucketFrequencyBasedEventData addScoreToCalculations(FrequencyBasedEventData scoreData, BucketFrequencyBasedEventData bucketData) {
        BucketFrequencyBasedScoreCalculator bucketCalculator = new BucketFrequencyBasedScoreCalculator(bucketData);
        bucketCalculator.decayScores();

        FinalEventType finalEventType = FinalEventType.enumFromString(scoreData.getClass().getSimpleName());

        FrequencyBasedCountAndContributionData frequencyBasedCountAndContributionData
                = bucketData.getActualScoresDataMap().get(finalEventType);

        if (frequencyBasedCountAndContributionData == null) {
            bucketData.getActualScoresDataMap().put(finalEventType, new FrequencyBasedCountAndContributionData(1, 0, 0));
            bucketData.getOldEventsScoreDateMap().put(finalEventType, new ConcurrentHashMap<>());
            bucketData.getOldEventsCountDateMap().put(finalEventType, new ConcurrentHashMap<>());
        } else {
            bucketData.getActualScoresDataMap().put(finalEventType,
                    new FrequencyBasedCountAndContributionData(frequencyBasedCountAndContributionData.getCountCurrent() + 1,
                            0, 0));
        }

        bucketCalculator.setCurrentScores();
        return bucketData;
    }

    @Override
    public EventType getScoreType() {
        return EventType.FREQUENCY_BASED_SCORE;
    }

    @Override
    public double getBucketSumScore(BucketFrequencyBasedEventData bucketData) {
        BucketFrequencyBasedScoreCalculator bucketCalculator = new BucketFrequencyBasedScoreCalculator(bucketData);
        if (bucketCalculator.decayScores()) {
            bucketCalculator.setCurrentScores();
        }
        return bucketCalculator.getBucketSumScore();
    }
}
