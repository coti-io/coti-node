package io.coti.trustscore.services;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.TransactionData;
import io.coti.trustscore.bl.BucketCalculator.BucketTransactionsCalculator;
import io.coti.trustscore.data.Buckets.BucketTransactionEventsData;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Events.TransactionEventData;
import io.coti.trustscore.rulesData.RulesData;
import javafx.util.Pair;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

import static io.coti.trustscore.utils.DatesCalculation.setDateOnBeginningOfDay;


@Data
@Service
public class BucketTransactionService implements BucketEventService<TransactionEventData, BucketTransactionEventsData> {


    private RulesData rulesData;

    @Override
    public BucketTransactionEventsData addEventToCalculations(TransactionEventData transactionEventData, BucketTransactionEventsData bucketTransactionEventsData) {
        TransactionData transactionData = transactionEventData.getTransactionData();
        BaseTransactionData transferTransaction = transactionData.getBaseTransactions().get(transactionData.getBaseTransactions().size() - 1);

        // Decay on case that this is the first transaction today
        BucketTransactionsCalculator bucketCalculator = new BucketTransactionsCalculator(bucketTransactionEventsData);
        bucketCalculator.decayScores();

        addToBucket(transactionEventData, bucketTransactionEventsData, transferTransaction);
        bucketCalculator.setCurrentScores();
        return bucketTransactionEventsData;
    }

    private void addToBucket(TransactionEventData transactionEventData, BucketTransactionEventsData bucketTransactionEventsData, BaseTransactionData transferTransaction) {
        bucketTransactionEventsData.addEventToBucket(transactionEventData);

        if (transferTransaction.getAmount().doubleValue() < 0) {
            bucketTransactionEventsData.increaseCurrentDateNumberOfTransactionsByOne();
            bucketTransactionEventsData.setCurrentDateTurnOver(bucketTransactionEventsData.getCurrentDateTurnOver() + Math.abs(transferTransaction.getAmount().doubleValue()));
        }

        Map<Long, Pair<Double, Double>> currentMonthBalanceMap = bucketTransactionEventsData.getCurrentMonthBalance();
        long beginningOfToday = setDateOnBeginningOfDay(new Date()).getTime();
        if (currentMonthBalanceMap.containsKey(beginningOfToday)) {
            currentMonthBalanceMap.put(beginningOfToday,
                    new Pair<>(currentMonthBalanceMap.get(beginningOfToday).getKey() + transferTransaction.getAmount().doubleValue(), 0.0));
        } else {
            double previousBalance = 0;
            if (currentMonthBalanceMap.size() > 0) {
                long lastDayWithChangeInBalance = currentMonthBalanceMap.keySet().stream()
                        .reduce((i, j) -> i > j ? i : j).get();

                previousBalance = currentMonthBalanceMap.get(lastDayWithChangeInBalance).getKey();
            }
            currentMonthBalanceMap.put(beginningOfToday, new Pair<>(transferTransaction.getAmount().doubleValue() + previousBalance, 0.0));
        }

    }

    @Override
    public EventType getBucketEventType() {
        return EventType.TRANSACTION;
    }

    @Override
    public double getBucketSumScore(BucketTransactionEventsData bucketTransactionEventsData) {
        BucketTransactionsCalculator bucketCalculator = new BucketTransactionsCalculator(bucketTransactionEventsData);
        bucketCalculator.decayScores();
        return bucketCalculator.getBucketSumScore(bucketTransactionEventsData);
    }

    public void init(RulesData rulesData) {
        this.rulesData = rulesData;
        BucketTransactionsCalculator.init(rulesData);
    }

}
