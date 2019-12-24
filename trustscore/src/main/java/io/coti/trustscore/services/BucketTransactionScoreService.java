package io.coti.trustscore.services;

import io.coti.trustscore.config.rules.ScoreRules;
import io.coti.trustscore.config.rules.UserScoreRules;
import io.coti.trustscore.data.parameters.TransactionUserParameters;
import io.coti.trustscore.data.tsbuckets.BucketTransactionEventData;
import io.coti.trustscore.data.tsenums.EventType;
import io.coti.trustscore.data.tsenums.UserType;
import io.coti.trustscore.data.tsevents.TransactionEventData;
import io.coti.trustscore.services.calculationservices.BucketTransactionScoreCalculator;
import io.coti.trustscore.services.interfaces.IBucketService;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Service
public class BucketTransactionScoreService implements IBucketService<TransactionEventData, BucketTransactionEventData> {

    private static final int MONTH_LENGTH = 30;
    private static Map<UserType, TransactionUserParameters> transactionUserParametersMap;

    public static void init(Map<String, ScoreRules> classToScoreRulesMap) {

        transactionUserParametersMap = new ConcurrentHashMap<>();

        for (UserScoreRules userScoreRules : classToScoreRulesMap.get("TransactionScoreData").getUsers()) {
            transactionUserParametersMap.put(UserType.enumFromString(userScoreRules.getUserType()), new TransactionUserParameters(userScoreRules));
        }
    }

    public static TransactionUserParameters userParameters(UserType userType) {
        return transactionUserParametersMap.get(userType);
    }

    @Override
    public BucketTransactionEventData addScoreToCalculations(TransactionEventData transactionScoreData, BucketTransactionEventData bucketTransactionEventData) {

        // Decay on case that this is the first transaction today
        BucketTransactionScoreCalculator bucketCalculator = new BucketTransactionScoreCalculator(bucketTransactionEventData);
        bucketCalculator.decayScores();

        bucketCalculator.addToBucket(transactionScoreData);
        bucketCalculator.setCurrentScores();
        return bucketTransactionEventData;
    }

    public EventType getScoreType() {
        return EventType.TRANSACTION;
    }

    @Override
    public double getBucketSumScore(BucketTransactionEventData bucketTransactionEventData) {
        BucketTransactionScoreCalculator bucketCalculator = new BucketTransactionScoreCalculator(bucketTransactionEventData);
        // Decay on case that this is the first event, or first access to data today
        if (bucketCalculator.decayScores()) {
            bucketCalculator.setCurrentScores();
        }
        return bucketCalculator.getBucketSumScore();
    }
}

