package io.coti.trustscore.services;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.trustscore.bl.BucketCalculator.BucketTransactionsCalculator;
import io.coti.trustscore.data.Buckets.BucketTransactionEventsData;
import io.coti.trustscore.data.Events.TransactionEventData;
import io.coti.trustscore.rulesData.RulesData;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

import static io.coti.trustscore.utils.DatesCalculation.setDateOnBeginningOfDay;

@Service
@Data
public class BucketTransactionService {

    private BucketTransactionsCalculator bucketTransactionsCalculator;
    private RulesData rulesData;

    public BucketTransactionEventsData addEventToCalculations(TransactionEventData transactionEventData, BucketTransactionEventsData bucketTransactionEventsData) {

        TransactionData transactionData = transactionEventData.getTransactionData();
        BaseTransactionData transferTransaction = transactionData.getBaseTransactions().get(transactionData.getBaseTransactions().size() - 1);

        bucketTransactionsCalculator.setBucketTransactionEventsData(bucketTransactionEventsData);
        bucketTransactionsCalculator.decayTransactionScores(transactionData.getHash());
        bucketTransactionEventsData.addEventToBucket(transactionEventData);
        bucketTransactionEventsData.increaseCurrentDateNumberOfTransactionsByOne();
        bucketTransactionEventsData.setCurrentDateTurnOver(bucketTransactionEventsData.getCurrentDateTurnOver() + Math.abs(transferTransaction.getAmount().doubleValue()));

        Map<Date, Double> currentMonthBalanceMap = bucketTransactionEventsData.getCurrentMonthBalance();
        Date beginningOfToday = setDateOnBeginningOfDay(new Date());
        if (currentMonthBalanceMap.containsKey(beginningOfToday)) {
            currentMonthBalanceMap.put(beginningOfToday, currentMonthBalanceMap.get(beginningOfToday) + Math.abs(transferTransaction.getAmount().doubleValue()));
        } else {
            currentMonthBalanceMap.put(beginningOfToday, Math.abs(transferTransaction.getAmount().doubleValue()));
        }
        bucketTransactionsCalculator.setCurrentTransactionsScores();

        return bucketTransactionEventsData;
    }

    public double getBucketSumScore(BucketTransactionEventsData bucketTransactionEventsData, Hash UserHash) {
        bucketTransactionsCalculator.decayTransactionScores(UserHash);
        return bucketTransactionsCalculator.getBucketSumScore(bucketTransactionEventsData);
    }

    public void init(RulesData rulesData) {
        this.rulesData = rulesData;
        bucketTransactionsCalculator.init(rulesData);
    }
}
