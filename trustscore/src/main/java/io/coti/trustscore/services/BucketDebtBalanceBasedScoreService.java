package io.coti.trustscore.services;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.config.rules.ScoreRules;
import io.coti.trustscore.config.rules.UserScoreRules;
import io.coti.trustscore.data.tsbuckets.BucketDebtBalanceBasedEventData;
import io.coti.trustscore.data.tsenums.FinalEventType;
import io.coti.trustscore.data.tsenums.EventType;
import io.coti.trustscore.data.tsenums.UserType;
import io.coti.trustscore.data.tsevents.BalanceBasedEventData;
import io.coti.trustscore.data.tsevents.CloseDebtBalanceBasedEventData;
import io.coti.trustscore.data.tsevents.DebtBalanceBasedEventData;
import io.coti.trustscore.data.parameters.BalanceBasedUserParameters;
import io.coti.trustscore.data.contributiondata.CounteragentBalanceContributionData;
import io.coti.trustscore.data.parameters.UserParameters;
import io.coti.trustscore.services.calculationservices.BucketDebtBalanceBasedScoreCalculator;
import io.coti.trustscore.services.interfaces.IBucketService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BucketDebtBalanceBasedScoreService implements IBucketService<BalanceBasedEventData, BucketDebtBalanceBasedEventData> {
    private static Map<UserType, UserParameters> debtUserParametersMap;

    public static UserParameters userParameters(UserType userType) {
        return debtUserParametersMap.get(userType);
    }

    public static void init(Map<String, ScoreRules> classToScoreRulesMap) {

        debtUserParametersMap = new ConcurrentHashMap<>();

        for (UserScoreRules userScoreRules : classToScoreRulesMap.get("DebtBalanceBasedScoreData").getUsers()) {
            debtUserParametersMap.put(UserType.enumFromString(userScoreRules.getUserType()), new BalanceBasedUserParameters(userScoreRules));
        }
    }

    @Override
    public BucketDebtBalanceBasedEventData addScoreToCalculations(BalanceBasedEventData scoreData, BucketDebtBalanceBasedEventData bucketData) {
        BucketDebtBalanceBasedScoreCalculator bucketCalculator = new BucketDebtBalanceBasedScoreCalculator(bucketData);
        bucketCalculator.decayScores();

        FinalEventType finalEventType = FinalEventType.enumFromString(scoreData.getClass().getSimpleName());
        Hash otherUserHash;
        CounteragentBalanceContributionData counteragentBalanceContributionData = null;
        CounteragentBalanceContributionData newCounteragentBalanceContributionData = null;

        if (finalEventType == FinalEventType.DEBT) {
            otherUserHash = ((DebtBalanceBasedEventData) scoreData).getPlaintiffUserHash();
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
        } else if (finalEventType == FinalEventType.CLOSEDEBT) {
            otherUserHash = ((CloseDebtBalanceBasedEventData) scoreData).getPlaintiffUserHash();
            counteragentBalanceContributionData = bucketData.getHashCounteragentBalanceContributionDataMap().get(otherUserHash);
            if (counteragentBalanceContributionData != null) {
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
    public double getBucketSumScore(BucketDebtBalanceBasedEventData bucketData) {
        BucketDebtBalanceBasedScoreCalculator bucketCalculator = new BucketDebtBalanceBasedScoreCalculator(bucketData);

        bucketCalculator.decayScores();

        return bucketCalculator.getBucketSumScore();
    }

    @Override
    public EventType getScoreType() {
        return EventType.DEBT_BALANCE_BASED_SCORE;
    }
}
