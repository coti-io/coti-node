package io.coti.trustscore.services;

import io.coti.trustscore.config.rules.ScoreRules;
import io.coti.trustscore.config.rules.UserScoreRules;
import io.coti.trustscore.data.scorebuckets.BucketDepositBalanceBasedScoreData;
import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.data.scoreenums.ScoreType;
import io.coti.trustscore.data.scoreenums.UserType;
import io.coti.trustscore.data.scoreevents.BalanceBasedScoreData;
import io.coti.trustscore.data.parameters.BalanceBasedUserParameters;
import io.coti.trustscore.data.parameters.UserParameters;
import io.coti.trustscore.services.calculationservices.BucketDepositBalanceBasedScoreCalculator;
import io.coti.trustscore.services.interfaces.IBucketService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BucketDepositBalanceBasedScoreService implements IBucketService<BalanceBasedScoreData, BucketDepositBalanceBasedScoreData> {
    private static Map<UserType, UserParameters> DepositUserParametersMap;

    public static UserParameters userParameters(FinalScoreType finalScoreType, UserType userType) {
        return DepositUserParametersMap.get(userType);
    }

    public static void init(Map<String, ScoreRules> classToScoreRulesMap) {

        DepositUserParametersMap = new ConcurrentHashMap<>();

        for (UserScoreRules userScoreRules : classToScoreRulesMap.get("DepositBalanceBasedScoreData").getUsers()) {
            DepositUserParametersMap.put(UserType.enumFromString(userScoreRules.getUserType()), new BalanceBasedUserParameters(userScoreRules));
        }
    }

    @Override
    public BucketDepositBalanceBasedScoreData addScoreToCalculations(BalanceBasedScoreData scoreData, BucketDepositBalanceBasedScoreData bucketData) {
        // Decay on case that this is the first event today
        BucketDepositBalanceBasedScoreCalculator bucketCalculator = new BucketDepositBalanceBasedScoreCalculator(bucketData);
        bucketCalculator.decayScores();

        FinalScoreType finalScoreType = FinalScoreType.enumFromString(scoreData.getClass().getSimpleName());

        switch (finalScoreType) {
            case DEPOSIT:
                bucketData.setCurrentBalance(bucketData.getCurrentBalance() + scoreData.getAmount().doubleValue());
                break;
            case CLOSEDEPOSIT:
                bucketData.setCurrentBalance(bucketData.getCurrentBalance() - scoreData.getAmount().doubleValue());
                bucketData.setCurrentDayClose(bucketData.getCurrentDayClose() + scoreData.getAmount().doubleValue());
                break;
        }

        bucketCalculator.setCurrentScores();
        return bucketData;
    }

    @Override
    public double getBucketSumScore(BucketDepositBalanceBasedScoreData bucketData) {
        BucketDepositBalanceBasedScoreCalculator bucketCalculator = new BucketDepositBalanceBasedScoreCalculator(bucketData);

        if (bucketCalculator.decayScores()) {
            bucketCalculator.setCurrentScores();
        }
        return bucketCalculator.getBucketSumScore();
    }

    @Override
    public ScoreType getScoreType() {
        return ScoreType.DEPOSIT_BALANCE_BASED_SCORE;
    }
}
