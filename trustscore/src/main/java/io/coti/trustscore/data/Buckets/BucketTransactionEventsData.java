package io.coti.trustscore.data.Buckets;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.TransactionEventData;
import javafx.util.Pair;
import lombok.Data;
import lombok.Synchronized;

import java.util.Date;
import java.util.HashMap;

import static io.coti.trustscore.utils.DatesCalculation.setDateOnBeginningOfDay;

@Data
public class BucketTransactionEventsData extends BucketEventData<TransactionEventData> implements IEntity {
    private Hash bucketHash;
    private static final int periodTime = 60;
    private double constantK = 1.098612289;
    public UserType userType;

    private int currentDateNumberOfTransactions;
    private double currentDateNumberOfTransactionsContribution;
    private double oldDateNumberOfTransactionsContribution;

    private double currentDateTurnOver;
    private double currentDateTurnOverContribution;
    private double oldDateTurnOverContribution;

    private HashMap<Long, Pair<Double, Double>> currentMonthBalance;
    private double currentMonthBalanceContribution;
    private double oldMonthBalanceContribution;

    public BucketTransactionEventsData() {
        currentMonthBalance = new HashMap<>();
    }

    public void increaseCurrentDateNumberOfTransactionsByOne() {
        currentDateNumberOfTransactions++;
    }


    @Override
    public Hash getHash() {
        return this.bucketHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.bucketHash = hash;
    }

    public boolean lastUpdateBeforeToday() {
        return this.getLastUpdate().before(setDateOnBeginningOfDay(new Date()));
    }

}




