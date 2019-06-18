package io.coti.trustscore.services;

import io.coti.trustscore.config.rules.RulesData;
import io.coti.trustscore.data.Buckets.BucketTransactionEventsData;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Events.BalanceCountAndContribution;
import io.coti.trustscore.data.Events.TransactionEventData;
import io.coti.trustscore.services.calculationservices.BucketTransactionsCalculator;
import io.coti.trustscore.services.interfaces.IBucketEventService;
import io.coti.trustscore.utils.DatesCalculation;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

@Data
@Service
public class BucketTransactionService implements IBucketEventService<TransactionEventData, BucketTransactionEventsData> {

    private static RulesData rulesData;

    public static RulesData getRulesData() {
        return rulesData;
    }

    public static void init(RulesData rulesData) {
        BucketTransactionService.rulesData = rulesData;
        BucketTransactionsCalculator.init(rulesData);
    }

    @Override
    public BucketTransactionEventsData addEventToCalculations(TransactionEventData transactionEventData, BucketTransactionEventsData bucketTransactionEventsData) {
        //  TransactionData transactionData = transactionEventData.getTransactionData();
//        BaseTransactionData transferTransaction = transactionData.getBaseTransactions().get(transactionData.getBaseTransactions().size() - 1);

        // Decay on case that this is the first transaction today
        BucketTransactionsCalculator bucketCalculator = new BucketTransactionsCalculator(bucketTransactionEventsData);
        bucketCalculator.decayScores(bucketTransactionEventsData);

        addToBucket(transactionEventData, bucketTransactionEventsData);
        bucketCalculator.setCurrentScores();
        return bucketTransactionEventsData;
    }

    private void addToBucket(TransactionEventData transactionEventData,
                             BucketTransactionEventsData bucketTransactionEventsData) {


        bucketTransactionEventsData.addEventToBucket(transactionEventData);
        {
            // Add dailyEvents to calculations
            double transactionAmount = transactionEventData.getAmount().doubleValue();
            bucketTransactionEventsData.increaseCurrentDateNumberOfTransactionsByOne();
            bucketTransactionEventsData.setCurrentDateTurnOver(bucketTransactionEventsData.getCurrentDateTurnOver() + Math.abs(transactionAmount));

            // Add monthlyEvents to calculations
            Map<Date, BalanceCountAndContribution> currentMonthBalanceMap
                    = bucketTransactionEventsData.getCurrentMonthDayToBalanceCountAndContribution();
            Date beginningOfToday = DatesCalculation.setDateOnBeginningOfDay(new Date());
            if (currentMonthBalanceMap.containsKey(beginningOfToday)) {
                currentMonthBalanceMap.put(beginningOfToday,
                        new BalanceCountAndContribution(currentMonthBalanceMap.get(beginningOfToday).getCount() + transactionAmount, 0));
            } else {
                double previousBalance = 0;
                if (!currentMonthBalanceMap.isEmpty()) {
                    Date lastDayWithChangeInBalance = Collections.max(currentMonthBalanceMap.keySet());
                    previousBalance = currentMonthBalanceMap.get(lastDayWithChangeInBalance).getCount();
                }
                currentMonthBalanceMap.put(beginningOfToday, new BalanceCountAndContribution(transactionAmount + previousBalance, 0));
            }
        }
    }

    @Override
    public EventType getBucketEventType() {
        return EventType.TRANSACTION;
    }

    @Override
    public double getBucketSumScore(BucketTransactionEventsData bucketTransactionEventsData) {
        BucketTransactionsCalculator bucketCalculator = new BucketTransactionsCalculator(bucketTransactionEventsData);
        // Decay on case that this is the first event, or first access to data today
        if (bucketCalculator.decayScores(bucketTransactionEventsData)) {
            bucketCalculator.setCurrentScores();
        }
        return bucketCalculator.getBucketSumScore(bucketTransactionEventsData);
    }

}
