package io.coti.trustscore.data.Buckets;

import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.TransactionEventData;
import lombok.Data;
import lombok.Synchronized;

import java.util.*;

import static io.coti.trustscore.utils.DatesCalculation.setDateOnBeginningOfDay;

@Data
public class BucketTransactionEventsData extends BucketEventData<TransactionEventData> {
    private static final int periodTime = 60;
    private double constantK = 1.098612289;
    private UserType userType;

    private int currentDateNumberOfTransactions;
    private double currentDateNumberOfTransactionsContribution;

    private double currentDateTurnOver;
    private double currentDateTurnOverContribution;

    private Map<Date, Double> currentMonthBalance;
    private double currentMonthBalanceContribution;

    private Date LastUpdate;

    public BucketTransactionEventsData() {
        currentMonthBalance = new HashMap<>();

    }

    public void increaseCurrentDateNumberOfTransactionsByOne() {
        currentDateNumberOfTransactions++;
    }

    @Override
    public int bucketPeriodTime() {
        return periodTime;
    }

    @Override
    @Synchronized
    protected void addEventToCalculations(TransactionEventData eventData) {
    }


    public boolean lastUpdateBeforeToday(){
        return this.getLastUpdate().before(setDateOnBeginningOfDay(new Date()));
    }

}




