package io.coti.trustscore.data.tsbuckets;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.contributiondata.BalanceAndContribution;
import io.coti.trustscore.data.tsevents.TransactionEventData;
import lombok.Data;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentSkipListMap;


@Data
public class BucketTransactionEventData extends BucketData<TransactionEventData> implements IEntity {

    private static final long serialVersionUID = 7805723170564280446L;
    private int currentDateNumberOfTransactions;
    private double currentDateNumberOfTransactionsContribution;
    private double oldDateNumberOfTransactionsContribution;

    private double currentDateTurnOver;
    private double currentDateTurnOverContribution;
    private double oldDateTurnOverContribution;

    private ConcurrentSkipListMap<LocalDate, BalanceAndContribution> currentMonthDayToBalanceCountAndContribution;
    private double currentMonthBalanceContribution;
    private double oldMonthBalanceContribution;

    public BucketTransactionEventData() {
        super();
        currentMonthDayToBalanceCountAndContribution = new ConcurrentSkipListMap<>();
    }

    public void increaseCurrentDateNumberOfTransactionsByOne() {
        currentDateNumberOfTransactions++;
    }

}

