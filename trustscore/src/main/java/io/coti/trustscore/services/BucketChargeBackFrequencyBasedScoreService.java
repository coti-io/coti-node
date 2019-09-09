package io.coti.trustscore.services;

import io.coti.basenode.data.TransactionData;
import io.coti.trustscore.config.rules.ScoreRules;
import io.coti.trustscore.config.rules.UserScoreRules;
import io.coti.trustscore.data.scorebuckets.BucketChargeBackFrequencyBasedScoreData;
import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.data.scoreenums.ScoreType;
import io.coti.trustscore.data.scoreenums.UserType;
import io.coti.trustscore.data.scoreevents.ChargeBackFrequencyBasedScoreData;
import io.coti.trustscore.data.parameters.ChargeBackUserParameters;
import io.coti.trustscore.data.parameters.UserParameters;
import io.coti.trustscore.services.calculationservices.BucketChargeBackFrequencyBasedScoreCalculator;
import io.coti.trustscore.services.interfaces.IBucketService;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Service
public class BucketChargeBackFrequencyBasedScoreService implements IBucketService<ChargeBackFrequencyBasedScoreData, BucketChargeBackFrequencyBasedScoreData> {

    private static Map<UserType, UserParameters> ChargeBackFrequencyBasedUserParametersMap;

    public static UserParameters userParameters(FinalScoreType finalScoreType, UserType userType) {
        return ChargeBackFrequencyBasedUserParametersMap.get(userType);
    }

    public static void init(Map<String, ScoreRules> classToScoreRulesMap) {

        ChargeBackFrequencyBasedUserParametersMap = new ConcurrentHashMap<>();

        for (UserScoreRules userScoreRules : classToScoreRulesMap.get("ChargeBackFrequencyBasedScoreData").getUsers()) {
            ChargeBackFrequencyBasedUserParametersMap.put(UserType.enumFromString(userScoreRules.getUserType()), new ChargeBackUserParameters(userScoreRules));
        }
    }

    @Override
    public BucketChargeBackFrequencyBasedScoreData addScoreToCalculations(ChargeBackFrequencyBasedScoreData scoreData, BucketChargeBackFrequencyBasedScoreData bucketData) {
        if (userParameters(FinalScoreType.CHARGEBACK, bucketData.getUserType()) == null) return bucketData;

        BucketChargeBackFrequencyBasedScoreCalculator bucketCalculator = new BucketChargeBackFrequencyBasedScoreCalculator(bucketData);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        bucketCalculator.decayScores();
        bucketData.addScoreToBucketMap(scoreData);
        synchronized (this) {
            Double currentMonthAmountOfChargeBacks = bucketData.getCurrentMonthAmountOfChargeBacks().get(today);
            if (currentMonthAmountOfChargeBacks == null) {
                currentMonthAmountOfChargeBacks = scoreData.getAmount().doubleValue();
            } else {
                currentMonthAmountOfChargeBacks += scoreData.getAmount().doubleValue();
            }
            bucketData.getCurrentMonthAmountOfChargeBacks().put(today, currentMonthAmountOfChargeBacks);

            Integer currentMonthNumberOfChargeBacks = bucketData.getCurrentMonthNumberOfChargeBacks().get(today);
            if (currentMonthNumberOfChargeBacks == null) {
                currentMonthNumberOfChargeBacks = 1;
            } else {
                currentMonthNumberOfChargeBacks += 1;
            }
            bucketData.getCurrentMonthNumberOfChargeBacks().put(today, currentMonthNumberOfChargeBacks);
        }
        bucketCalculator.setCurrentScores();
        return bucketData;
    }

    public void addPaymentTransactionToCalculations(TransactionData transactionData, BucketChargeBackFrequencyBasedScoreData bucketData) {

        BucketChargeBackFrequencyBasedScoreCalculator bucketCalculator = new BucketChargeBackFrequencyBasedScoreCalculator(bucketData);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        bucketCalculator.decayScores();
        synchronized (this) {
            Double currentMonthAmountOfPaymentTransactions = bucketData.getCurrentMonthAmountOfPaymentTransactions().get(today);
            if (currentMonthAmountOfPaymentTransactions == null) {
                currentMonthAmountOfPaymentTransactions = transactionData.getAmount().doubleValue();
            } else {
                currentMonthAmountOfPaymentTransactions += transactionData.getAmount().doubleValue();
            }
            bucketData.getCurrentMonthAmountOfPaymentTransactions().put(today, currentMonthAmountOfPaymentTransactions);

            Integer currentMonthNumberOfPaymentTransactions = bucketData.getCurrentMonthNumberOfPaymentTransactions().get(today);
            if (currentMonthNumberOfPaymentTransactions == null) {
                currentMonthNumberOfPaymentTransactions = 1;
            } else {
                currentMonthNumberOfPaymentTransactions += 1;
            }
            bucketData.getCurrentMonthNumberOfPaymentTransactions().put(today, currentMonthNumberOfPaymentTransactions);
        }
        bucketCalculator.setCurrentScores();
    }

    @Override
    public double getBucketSumScore(BucketChargeBackFrequencyBasedScoreData bucketData) {
        if (userParameters(FinalScoreType.CHARGEBACK, bucketData.getUserType()) == null) return 0;

        BucketChargeBackFrequencyBasedScoreCalculator bucketCalculator = new BucketChargeBackFrequencyBasedScoreCalculator(bucketData);

        if (bucketCalculator.decayScores()) {
            bucketCalculator.setCurrentScores();
        }
        return bucketCalculator.getBucketSumScore(bucketData);
    }

    @Override
    public ScoreType getScoreType() {
        return ScoreType.CHARGEBACK_SCORE;
    }

}
