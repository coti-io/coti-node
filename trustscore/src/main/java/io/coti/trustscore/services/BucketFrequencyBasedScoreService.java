package io.coti.trustscore.services;

import io.coti.trustscore.config.rules.ScoreRules;
import io.coti.trustscore.config.rules.UserScoreRules;
import io.coti.trustscore.data.scorebuckets.BucketFrequencyBasedScoreData;
import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.data.scoreenums.ScoreType;
import io.coti.trustscore.data.scoreenums.UserType;
import io.coti.trustscore.data.scoreevents.FrequencyBasedScoreData;
import io.coti.trustscore.data.parameters.FrequencyBasedCountAndContributionData;
import io.coti.trustscore.data.parameters.FrequencyBasedUserParameters;
import io.coti.trustscore.services.calculationservices.BucketFrequencyBasedScoreCalculator;
import io.coti.trustscore.services.interfaces.IBucketService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BucketFrequencyBasedScoreService implements IBucketService<FrequencyBasedScoreData, BucketFrequencyBasedScoreData> {

    private static Map<UserType, FrequencyBasedUserParameters> ClaimFrequencyBasedUserParametersMap;

    public static FrequencyBasedUserParameters userParameters(FinalScoreType finalScoreType, UserType userType) {
        switch (finalScoreType) {
            case CLAIM:
                return ClaimFrequencyBasedUserParametersMap.get(userType);
        }
        return null;
    }

    public static void init(Map<String, ScoreRules> classToScoreRulesMap) {

        ClaimFrequencyBasedUserParametersMap = new ConcurrentHashMap<>();

        for (UserScoreRules userScoreRules : classToScoreRulesMap.get("ClaimFrequencyBasedScoreData").getUsers()) {
            ClaimFrequencyBasedUserParametersMap.put(UserType.enumFromString(userScoreRules.getUserType()), new FrequencyBasedUserParameters(userScoreRules));
        }
    }

    @Override
    public BucketFrequencyBasedScoreData addScoreToCalculations(FrequencyBasedScoreData scoreData, BucketFrequencyBasedScoreData bucketData) {
        BucketFrequencyBasedScoreCalculator bucketCalculator = new BucketFrequencyBasedScoreCalculator(bucketData);
        bucketCalculator.decayScores();

        FinalScoreType finalScoreType = FinalScoreType.enumFromString(scoreData.getClass().getSimpleName());

        FrequencyBasedCountAndContributionData frequencyBasedCountAndContributionData
                = bucketData.getActualScoresDataMap().get(finalScoreType);

        if (frequencyBasedCountAndContributionData == null) {
            bucketData.getActualScoresDataMap().put(finalScoreType, new FrequencyBasedCountAndContributionData(1, 0, 0));
            bucketData.getOldEventsScoreDateMap().put(finalScoreType, new ConcurrentHashMap<>());
            bucketData.getOldEventsCountDateMap().put(finalScoreType, new ConcurrentHashMap<>());
        } else {
            bucketData.getActualScoresDataMap().put(finalScoreType,
                    new FrequencyBasedCountAndContributionData(frequencyBasedCountAndContributionData.getCountCurrent() + 1,
                            0, 0));
        }

        bucketCalculator.setCurrentScores();
        return bucketData;
    }

    @Override
    public ScoreType getScoreType() {
        return ScoreType.FREQUENCY_BASED_SCORE;
    }

    @Override
    public double getBucketSumScore(BucketFrequencyBasedScoreData bucketData) {
        BucketFrequencyBasedScoreCalculator bucketCalculator = new BucketFrequencyBasedScoreCalculator(bucketData);
        if (bucketCalculator.decayScores()) {
            bucketCalculator.setCurrentScores();
        }
        return bucketCalculator.getBucketSumScore();
    }
}
