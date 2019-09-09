package io.coti.trustscore.data.scorebuckets;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.scoreevents.TransactionScoreData;
import io.coti.trustscore.data.parameters.BalanceAndContribution;
import lombok.Data;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentSkipListMap;


@Data
public class BucketTransactionScoreData extends BucketData<TransactionScoreData> implements IEntity {

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

    public BucketTransactionScoreData() {
        super();
        currentMonthDayToBalanceCountAndContribution = new ConcurrentSkipListMap<>();
    }

    public void increaseCurrentDateNumberOfTransactionsByOne() {
        currentDateNumberOfTransactions++;
    }

}

