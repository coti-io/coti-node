package io.coti.trustscore.data.Buckets;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Events.BalanceCountAndContribution;
import io.coti.trustscore.data.Events.TransactionEventData;
import lombok.Data;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class BucketTransactionEventsData extends BucketEventData<TransactionEventData> implements IEntity {

    private static final long serialVersionUID = -8196175492567413372L;
    private int currentDateNumberOfTransactions;
    private double currentDateNumberOfTransactionsContribution;
    private double oldDateNumberOfTransactionsContribution;

    private double currentDateTurnOver;
    private double currentDateTurnOverContribution;
    private double oldDateTurnOverContribution;

    private ConcurrentHashMap<Date, BalanceCountAndContribution> currentMonthDayToBalanceCountAndContribution;
    private double currentMonthBalanceContribution;
    private double oldMonthBalanceContribution;

    public BucketTransactionEventsData() {
        super.setEventType(EventType.TRANSACTION);
        currentMonthDayToBalanceCountAndContribution = new ConcurrentHashMap<>();
    }

    public void increaseCurrentDateNumberOfTransactionsByOne() {
        currentDateNumberOfTransactions++;
    }


}


//todo delete

