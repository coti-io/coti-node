package io.coti.trustscore.services;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.config.rules.ScoreRules;
import io.coti.trustscore.config.rules.UserScoreRules;
import io.coti.trustscore.data.scorebuckets.BucketDebtBalanceBasedScoreData;
import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.data.scoreenums.ScoreType;
import io.coti.trustscore.data.scoreenums.UserType;
import io.coti.trustscore.data.scoreevents.BalanceBasedScoreData;
import io.coti.trustscore.data.scoreevents.CloseDebtBalanceBasedScoreData;
import io.coti.trustscore.data.scoreevents.DebtBalanceBasedScoreData;
import io.coti.trustscore.data.parameters.BalanceBasedUserParameters;
import io.coti.trustscore.data.parameters.CounteragentBalanceContributionData;
import io.coti.trustscore.data.parameters.UserParameters;
import io.coti.trustscore.services.calculationservices.BucketDebtBalanceBasedScoreCalculator;
import io.coti.trustscore.services.interfaces.IBucketService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BucketDebtBalanceBasedScoreService implements IBucketService<BalanceBasedScoreData, BucketDebtBalanceBasedScoreData> {
    private static Map<UserType, UserParameters> DebtUserParametersMap;

    public static UserParameters userParameters(FinalScoreType finalScoreType, UserType userType) {
        return DebtUserParametersMap.get(userType);
    }

    public static void init(Map<String, ScoreRules> classToScoreRulesMap) {

        DebtUserParametersMap = new ConcurrentHashMap<>();

        for (UserScoreRules userScoreRules : classToScoreRulesMap.get("DebtBalanceBasedScoreData").getUsers()) {
            DebtUserParametersMap.put(UserType.enumFromString(userScoreRules.getUserType()), new BalanceBasedUserParameters(userScoreRules));
        }
    }

    @Override
    public BucketDebtBalanceBasedScoreData addScoreToCalculations(BalanceBasedScoreData scoreData, BucketDebtBalanceBasedScoreData bucketData) {
        BucketDebtBalanceBasedScoreCalculator bucketCalculator = new BucketDebtBalanceBasedScoreCalculator(bucketData);
        bucketCalculator.decayScores();

        FinalScoreType finalScoreType = FinalScoreType.enumFromString(scoreData.getClass().getSimpleName());
        Hash otherUserHash;
        CounteragentBalanceContributionData counteragentBalanceContributionData = null;
        CounteragentBalanceContributionData newCounteragentBalanceContributionData = null;

        switch (finalScoreType) {
            case DEBT:
                otherUserHash = ((DebtBalanceBasedScoreData) scoreData).getPlaintiffUserHash();
                counteragentBalanceContributionData = bucketData.getHashCounteragentBalanceContributionDataMap().get(otherUserHash);
                if (counteragentBalanceContributionData == null) {
                    newCounteragentBalanceContributionData = new CounteragentBalanceContributionData(scoreData.getAmount().doubleValue(),
                            false, 0, 0, 0);
                    bucketData.getHashCounteragentBalanceContributionDataMap().put(otherUserHash, newCounteragentBalanceContributionData);
                } else {
                    newCounteragentBalanceContributionData = new CounteragentBalanceContributionData(
                            counteragentBalanceContributionData.getCurrentBalance() + scoreData.getAmount().doubleValue(),
                            counteragentBalanceContributionData.isRepayment(),
                            counteragentBalanceContributionData.getFine(),
                            counteragentBalanceContributionData.getOldFine(),
                            counteragentBalanceContributionData.getTail());
                    bucketData.getHashCounteragentBalanceContributionDataMap().put(otherUserHash, newCounteragentBalanceContributionData);
                }
                break;
            case CLOSEDEBT:
                otherUserHash = ((CloseDebtBalanceBasedScoreData) scoreData).getPlaintiffUserHash();
                counteragentBalanceContributionData = bucketData.getHashCounteragentBalanceContributionDataMap().get(otherUserHash);
                if (counteragentBalanceContributionData == null) {
                    break;
                } else {
                    newCounteragentBalanceContributionData = new CounteragentBalanceContributionData(
                            counteragentBalanceContributionData.getCurrentBalance() - scoreData.getAmount().doubleValue(),
                            true,
                            counteragentBalanceContributionData.getFine(),
                            counteragentBalanceContributionData.getOldFine(),
                            counteragentBalanceContributionData.getTail());
                    bucketData.getHashCounteragentBalanceContributionDataMap().put(otherUserHash, newCounteragentBalanceContributionData);
                }
        }

        if (newCounteragentBalanceContributionData != null) {
            bucketCalculator.setCurrentScore(newCounteragentBalanceContributionData);
        }
        return bucketData;
    }

    @Override
    public double getBucketSumScore(BucketDebtBalanceBasedScoreData bucketData) {
        BucketDebtBalanceBasedScoreCalculator bucketCalculator = new BucketDebtBalanceBasedScoreCalculator(bucketData);

        bucketCalculator.decayScores();

        return bucketCalculator.getBucketSumScore();
    }

    @Override
    public ScoreType getScoreType() {
        return ScoreType.DEBT_BALANCE_BASED_SCORE;
    }
}
