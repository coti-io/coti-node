package io.coti.trustscore.services.calculationservices;

import io.coti.trustscore.data.scorebuckets.BucketTransactionScoreData;
import io.coti.trustscore.data.scoreenums.TransactionEventType;
import io.coti.trustscore.data.scoreenums.UserType;
import io.coti.trustscore.data.scoreevents.TransactionScoreData;
import io.coti.trustscore.data.parameters.BalanceAndContribution;
import io.coti.trustscore.data.parameters.TransactionUserParameters;
import io.coti.trustscore.data.parameters.UserParameters;
import io.coti.trustscore.services.BucketTransactionScoreService;
import io.coti.trustscore.utils.DatesCalculation;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

import static java.lang.Long.min;

public class BucketTransactionScoreCalculator extends BucketScoresCalculator<BucketTransactionScoreData> {

    private static final int MONTH_LENGTH = 30;
    private TransactionUserParameters userParameters;


    public BucketTransactionScoreCalculator(BucketTransactionScoreData bucketData) {
        super(bucketData);
        userParameters = BucketTransactionScoreService.userParameters(bucketData.getUserType());
    }

    @Override
    public boolean decayScores() {
        if (!bucketData.lastUpdateBeforeToday()) {
            return false;
        }

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        int daysDiff = (int) ChronoUnit.DAYS.between(bucketData.getLastUpdate(), today);
        decayDailyTransactionsScores(daysDiff);

        // add Balances In The Gap Between Last Update To Now
        if (userParameters.getBalanceWeight() != 0) {
            addBalancesInTheGapBetweenLastUpdateToNow();
            decayMonthlyTransactionsScores(daysDiff, today);
        }

        bucketData.setLastUpdate(today);
        return true;
    }

    private void decayDailyTransactionsScores(int daysDiff) {

        bucketData.setOldDateNumberOfTransactionsContribution(DatesCalculation.calculateDecay(
                userParameters.getNumberSemiDecay(),
                bucketData.getCurrentDateNumberOfTransactionsContribution()
                        + bucketData.getOldDateNumberOfTransactionsContribution(),
                daysDiff));
        bucketData.setCurrentDateNumberOfTransactions(0);
        bucketData.setCurrentDateNumberOfTransactionsContribution(0);

        bucketData.setOldDateTurnOverContribution(DatesCalculation.calculateDecay(
                userParameters.getTurnoverSemiDecay(),
                bucketData.getCurrentDateTurnOverContribution()
                        + bucketData.getOldDateTurnOverContribution(),
                daysDiff));
        bucketData.setCurrentDateTurnOver(0);
        bucketData.setCurrentDateTurnOverContribution(0);
    }

    private double moveToTailBalance() {
        // Map of balances after 30 days. will be moved to the tail after decay.
        int daysDiff;
        double sumMovedToTail = 0;

        // Map of balances in 30 days. decay, but keep in current balance.
        Map<LocalDate, BalanceAndContribution> currentMonthBalanceMap = bucketData.getCurrentMonthDayToBalanceCountAndContribution();

        // moving old balances from currentMonthBalanceMap to currentMonthBalanceToTailMap.
        for (Iterator<Map.Entry<LocalDate, BalanceAndContribution>> currentMonthBalanceIterator = currentMonthBalanceMap.entrySet().iterator();
             currentMonthBalanceIterator.hasNext(); ) {
            Map.Entry<LocalDate, BalanceAndContribution> entry = currentMonthBalanceIterator.next();
            daysDiff = (int) ChronoUnit.DAYS.between(entry.getKey(), LocalDate.now(ZoneOffset.UTC));
            if (daysDiff >= MONTH_LENGTH + 1) {
                sumMovedToTail += entry.getValue().getContribution();
                currentMonthBalanceIterator.remove();
            }
        }
        return sumMovedToTail;
    }

    private void decayMonthlyTransactionsScores(int daysDiff, LocalDate today) {

        //recalculate and Decay currentMonthEventsMap
        double currentMonthBalanceDecayedContribution = 0;
        double oldMonthBalanceDecayedContribution = 0;
        if (!bucketData.getCurrentMonthDayToBalanceCountAndContribution().isEmpty()) {
            int daysDiffLocal = (int) ChronoUnit.DAYS.between(bucketData.getCurrentMonthDayToBalanceCountAndContribution().firstKey(), today);

            for (Map.Entry<LocalDate, BalanceAndContribution> dayToScoreMapEntry : bucketData.getCurrentMonthDayToBalanceCountAndContribution().entrySet()) {
                double decayedCurrentDailyScore = DatesCalculation.calculateDecay(userParameters.getBalanceSemiDecay(),
                        dayToScoreMapEntry.getValue().getContribution(), (int) min(daysDiffLocal, daysDiff));
                bucketData.getCurrentMonthDayToBalanceCountAndContribution().put(dayToScoreMapEntry.getKey(),
                        new BalanceAndContribution(dayToScoreMapEntry.getValue().getCount(), decayedCurrentDailyScore));
                currentMonthBalanceDecayedContribution += decayedCurrentDailyScore;
                daysDiffLocal--;  // it is OK while currentMonthDayToBalanceCountAndContribution is ConcurrentSkipListMap
            }

            oldMonthBalanceDecayedContribution = moveToTailBalance();
        }
        // recalculate and decay the tail
        bucketData.setOldMonthBalanceContribution(oldMonthBalanceDecayedContribution + DatesCalculation.calculateDecay(
                userParameters.getBalanceSemiDecay(), bucketData.getOldMonthBalanceContribution(), daysDiff));
        bucketData.setCurrentMonthBalanceContribution(currentMonthBalanceDecayedContribution);
    }

    public void setCurrentScores() {
        LocalDate lastUpdateDate = bucketData.getLastUpdate();

        bucketData.setCurrentDateNumberOfTransactionsContribution(Math.tanh(
                bucketData.getCurrentDateNumberOfTransactions() / userParameters.getNumberLevel08()
                        * UserParameters.atanh08));
        bucketData.setCurrentDateTurnOverContribution(Math.tanh(
                bucketData.getCurrentDateTurnOver() / userParameters.getTurnoverLevel08()
                        * UserParameters.atanh08));

        if (userParameters.getBalanceWeight() != 0) {
            // Calculate every day from the last days balance score.
            Map<LocalDate, Double> scoresToDatesScoreMap = collectNewDaysAverageBalanceScore();

            for (Map.Entry<LocalDate, Double> dayToScoreCalculateEntry : scoresToDatesScoreMap.entrySet()) {
                double dailyScore = Math.tanh(dayToScoreCalculateEntry.getValue() / userParameters.getBalanceLevel08() * UserParameters.atanh08);
                if (!lastUpdateDate.equals(dayToScoreCalculateEntry.getKey())) {
                    int daysDiff = (int) ChronoUnit.DAYS.between(dayToScoreCalculateEntry.getKey(), lastUpdateDate);
                    dailyScore = DatesCalculation.calculateDecay(userParameters.getBalanceSemiDecay(), dailyScore, daysDiff);
                }
                bucketData.getCurrentMonthDayToBalanceCountAndContribution()
                        .put(dayToScoreCalculateEntry.getKey(), new BalanceAndContribution(dayToScoreCalculateEntry.getValue(), dailyScore));
            }
            updateCurrentMonthBalanceContribution();
        }
    }

    private void addBalancesInTheGapBetweenLastUpdateToNow() {
        ConcurrentSkipListMap<LocalDate, BalanceAndContribution> currentMonthBalanceMap = bucketData.getCurrentMonthDayToBalanceCountAndContribution();

        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        if (!currentMonthBalanceMap.isEmpty()) {

            LocalDate lastDayWithChangeInBalance = currentMonthBalanceMap.lastKey();
            double previousBalanceCount = currentMonthBalanceMap.get(lastDayWithChangeInBalance).getCount();
            double previousBalanceScore = currentMonthBalanceMap.get(lastDayWithChangeInBalance).getContribution();
            int daysDiff = 1;

            for (LocalDate day = lastDayWithChangeInBalance.plusDays(1);
                 !day.isAfter(today);
                 day = day.plusDays(1), daysDiff++) {
                currentMonthBalanceMap.put(day, new BalanceAndContribution(previousBalanceCount, previousBalanceScore));
            }
        }
    }

    private Map<LocalDate, Double> collectNewDaysAverageBalanceScore() {
        return bucketData.getCurrentMonthDayToBalanceCountAndContribution().entrySet().stream()
                .filter(x -> x.getValue().getContribution() == 0)
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getCount()));
    }

    private void updateCurrentMonthBalanceContribution() {
        double sumCurrentMonthBalanceContribution = 0;
        Map<LocalDate, BalanceAndContribution> currentMonthBalance = bucketData.getCurrentMonthDayToBalanceCountAndContribution();
        for (Map.Entry<LocalDate, BalanceAndContribution> currentMonthBalanceEntry : currentMonthBalance.entrySet()) {
            sumCurrentMonthBalanceContribution += currentMonthBalanceEntry.getValue().getContribution();
        }
        bucketData.setCurrentMonthBalanceContribution(sumCurrentMonthBalanceContribution);
    }

    public double getBucketSumScore() {
        return ((bucketData.getCurrentDateTurnOverContribution()
                + bucketData.getOldDateTurnOverContribution()) * userParameters.getTurnoverWeight())
                + ((bucketData.getCurrentMonthBalanceContribution()
                + bucketData.getOldMonthBalanceContribution()) * userParameters.getBalanceWeight())
                + ((bucketData.getCurrentDateNumberOfTransactionsContribution()
                + bucketData.getOldDateNumberOfTransactionsContribution()) * userParameters.getNumberWeight());
    }

    public void addToBucket(TransactionScoreData transactionScoreData) {

        // Add dailyEvents to calculations
        double transactionAmount = transactionScoreData.getAmount().doubleValue();
        LocalDate eventDate = transactionScoreData.getEventDate();
        LocalDate nextDate = eventDate.plusDays(1);
        ConcurrentSkipListMap<LocalDate, BalanceAndContribution> currentMonthBalanceMap
                = bucketData.getCurrentMonthDayToBalanceCountAndContribution();

        if (transactionScoreData.getTransactionEventType() == TransactionEventType.SENDER_EVENT ||
                transactionScoreData.getTransactionEventType() == TransactionEventType.SENDER_NEW_ADDRESS_EVENT) {
            bucketData.increaseCurrentDateNumberOfTransactionsByOne();
            bucketData.setCurrentDateTurnOver(bucketData.getCurrentDateTurnOver() + Math.abs(transactionAmount));
            transactionAmount = -transactionAmount;
        }
        if (transactionScoreData.getTransactionEventType() == TransactionEventType.SENDER_NEW_ADDRESS_EVENT) {
            ConcurrentSkipListMap<LocalDate, Double> unlinkedAddressBalance = transactionScoreData.getUnlinkedAddressData().getDateToBalanceMap();
            if (!unlinkedAddressBalance.isEmpty()) {
                Map.Entry<LocalDate, Double> currentUnlinkedBalance = unlinkedAddressBalance.firstEntry();
                Map.Entry<LocalDate, BalanceAndContribution> currentMonthBalanceEntry;
                LocalDate todayDate = LocalDate.now(ZoneOffset.UTC);
                LocalDate startDate;
                LocalDate monthAgo = todayDate.minusDays(MONTH_LENGTH);

                if (!currentMonthBalanceMap.isEmpty()) {
                    currentMonthBalanceEntry = currentMonthBalanceMap.firstEntry();

                    LocalDate tempDate = currentUnlinkedBalance.getKey().isBefore(currentMonthBalanceEntry.getKey()) ? currentUnlinkedBalance.getKey() : currentMonthBalanceEntry.getKey();
                    startDate = tempDate.isAfter(monthAgo) ? tempDate : monthAgo;
                } else {
                    currentMonthBalanceEntry = null;
                    startDate = currentUnlinkedBalance.getKey().isAfter(monthAgo) ? currentUnlinkedBalance.getKey() : monthAgo;

                }

                for (LocalDate dayForBalance = startDate;
                     !dayForBalance.isAfter(todayDate);
                     dayForBalance = dayForBalance.plusDays(1)) {

                    if (currentUnlinkedBalance != null) {
                        while (unlinkedAddressBalance.higherKey(currentUnlinkedBalance.getKey()) != null &&
                                !unlinkedAddressBalance.higherKey(currentUnlinkedBalance.getKey()).isAfter(dayForBalance)) {
                            currentUnlinkedBalance = unlinkedAddressBalance.higherEntry(currentUnlinkedBalance.getKey());
                        }
                    }
                    if (currentMonthBalanceEntry != null) {
                        while (currentMonthBalanceMap.higherKey(currentMonthBalanceEntry.getKey()) != null &&
                                !currentMonthBalanceMap.higherKey(currentMonthBalanceEntry.getKey()).isAfter(dayForBalance)) {
                            currentMonthBalanceEntry = currentMonthBalanceMap.higherEntry(currentMonthBalanceEntry.getKey());
                        }
                    }

                    if ((currentMonthBalanceEntry != null && !currentMonthBalanceEntry.getKey().isAfter(dayForBalance)) ||
                            (currentUnlinkedBalance != null && !currentUnlinkedBalance.getKey().isAfter(dayForBalance))) {
                        double newBalance = (currentMonthBalanceEntry != null) ? currentMonthBalanceEntry.getValue().getCount() : 0;
                        newBalance += (currentUnlinkedBalance != null) ? currentUnlinkedBalance.getValue() : 0;
                        currentMonthBalanceMap.put(dayForBalance, new BalanceAndContribution(newBalance, 0));
                    }
                }
            }
        }

        // Add monthlyEvents to calculations
        if (transactionScoreData.getUserType() != UserType.FULL_NODE && transactionScoreData.getUserType() != UserType.DSP_NODE && transactionScoreData.getUserType() != UserType.TRUST_SCORE_NODE) {
            if (currentMonthBalanceMap.containsKey(eventDate)) {
                currentMonthBalanceMap.put(eventDate,
                        new BalanceAndContribution(currentMonthBalanceMap.get(eventDate).getCount() + transactionAmount, 0));
            } else {
                double previousBalance = 0;
                if (!currentMonthBalanceMap.isEmpty()) {
                    LocalDate lastDayWithChangeInBalance = Collections.max(currentMonthBalanceMap.keySet());
                    previousBalance = currentMonthBalanceMap.get(lastDayWithChangeInBalance).getCount();
                }
                currentMonthBalanceMap.put(eventDate, new BalanceAndContribution(transactionAmount + previousBalance, 0));
            }
            if (currentMonthBalanceMap.containsKey(nextDate)) {
                currentMonthBalanceMap.put(nextDate,
                        new BalanceAndContribution(currentMonthBalanceMap.get(nextDate).getCount() + transactionAmount, 0));
            }
        }
    }
}
