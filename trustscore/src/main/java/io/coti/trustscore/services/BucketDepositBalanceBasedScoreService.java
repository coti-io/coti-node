package io.coti.trustscore.services;

import io.coti.trustscore.config.rules.ScoreRules;
import io.coti.trustscore.config.rules.UserScoreRules;
import io.coti.trustscore.data.tsbuckets.BucketDepositBalanceBasedEventData;
import io.coti.trustscore.data.tsenums.FinalEventType;
import io.coti.trustscore.data.tsenums.EventType;
import io.coti.trustscore.data.tsenums.UserType;
import io.coti.trustscore.data.tsevents.BalanceBasedEventData;
import io.coti.trustscore.data.parameters.BalanceBasedUserParameters;
import io.coti.trustscore.data.parameters.UserParameters;
import io.coti.trustscore.services.calculationservices.BucketDepositBalanceBasedScoreCalculator;
import io.coti.trustscore.services.interfaces.IBucketService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BucketDepositBalanceBasedScoreService implements IBucketService<BalanceBasedEventData, BucketDepositBalanceBasedEventData> {
    private static Map<UserType, UserParameters> depositUserParametersMap;

    public static UserParameters userParameters(UserType userType) {
        return depositUserParametersMap.get(userType);
    }

    public static void init(Map<String, ScoreRules> classToScoreRulesMap) {

        depositUserParametersMap = new ConcurrentHashMap<>();

        for (UserScoreRules userScoreRules : classToScoreRulesMap.get("DepositBalanceBasedScoreData").getUsers()) {
            depositUserParametersMap.put(UserType.enumFromString(userScoreRules.getUserType()), new BalanceBasedUserParameters(userScoreRules));
        }
    }

    @Override
    public BucketDepositBalanceBasedEventData addScoreToCalculations(BalanceBasedEventData scoreData, BucketDepositBalanceBasedEventData bucketData) {
        // Decay on case that this is the first event today
        BucketDepositBalanceBasedScoreCalculator bucketCalculator = new BucketDepositBalanceBasedScoreCalculator(bucketData);
        bucketCalculator.decayScores();

        FinalEventType finalEventType = FinalEventType.enumFromString(scoreData.getClass().getSimpleName());

        if (finalEventType == FinalEventType.DEPOSIT) {
            bucketData.setCurrentBalance(bucketData.getCurrentBalance() + scoreData.getAmount().doubleValue());
        } else if (finalEventType == FinalEventType.CLOSEDEPOSIT) {
            bucketData.setCurrentBalance(bucketData.getCurrentBalance() - scoreData.getAmount().doubleValue());
            bucketData.setCurrentDayClose(bucketData.getCurrentDayClose() + scoreData.getAmount().doubleValue());
        }

        bucketCalculator.setCurrentScores();
        return bucketData;
    }

    @Override
    public double getBucketSumScore(BucketDepositBalanceBasedEventData bucketData) {
        BucketDepositBalanceBasedScoreCalculator bucketCalculator = new BucketDepositBalanceBasedScoreCalculator(bucketData);

        if (bucketCalculator.decayScores()) {
            bucketCalculator.setCurrentScores();
        }
        return bucketCalculator.getBucketSumScore();
    }

    @Override
    public EventType getScoreType() {
        return EventType.DEPOSIT_BALANCE_BASED_SCORE;
    }
}
