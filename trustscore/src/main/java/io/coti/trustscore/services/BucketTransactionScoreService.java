package io.coti.trustscore.services;

import io.coti.trustscore.config.rules.ScoreRules;
import io.coti.trustscore.config.rules.UserScoreRules;
import io.coti.trustscore.data.scorebuckets.BucketTransactionScoreData;
import io.coti.trustscore.data.scoreenums.ScoreType;
import io.coti.trustscore.data.scoreenums.UserType;
import io.coti.trustscore.data.scoreevents.TransactionScoreData;
import io.coti.trustscore.data.parameters.TransactionUserParameters;
import io.coti.trustscore.services.calculationservices.BucketTransactionScoreCalculator;
import io.coti.trustscore.services.interfaces.IBucketService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Service
public class BucketTransactionScoreService implements IBucketService<TransactionScoreData, BucketTransactionScoreData> {

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
    public BucketTransactionScoreData addScoreToCalculations(TransactionScoreData transactionScoreData, BucketTransactionScoreData bucketTransactionScoreData) {

        // Decay on case that this is the first transaction today
        BucketTransactionScoreCalculator bucketCalculator = new BucketTransactionScoreCalculator(bucketTransactionScoreData);
        bucketCalculator.decayScores();

        bucketCalculator.addToBucket(transactionScoreData);
        bucketCalculator.setCurrentScores();
        return bucketTransactionScoreData;
    }

    public ScoreType getScoreType() {
        return ScoreType.TRANSACTION;
    }

    @Override
    public double getBucketSumScore(BucketTransactionScoreData bucketTransactionScoreData) {
        BucketTransactionScoreCalculator bucketCalculator = new BucketTransactionScoreCalculator(bucketTransactionScoreData);
        // Decay on case that this is the first event, or first access to data today
        if (bucketCalculator.decayScores()) {
            bucketCalculator.setCurrentScores();
        }
        return bucketCalculator.getBucketSumScore();
    }
}

