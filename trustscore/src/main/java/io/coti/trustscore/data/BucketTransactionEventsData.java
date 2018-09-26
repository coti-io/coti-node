package io.coti.trustscore.data;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.TransactionData;
import lombok.Data;
import lombok.Synchronized;

import java.math.BigDecimal;


@Data
public class BucketTransactionEventsData extends BucketEventData<TransactionEventData>{


    private static final int periodTime = 60;
    private int lastNumberOfTransactions;
    private BigDecimal lastTurnOver;
    private BigDecimal lastBalance;



    @Override
    public int bucketPeriodTime() {
        return periodTime;
    }

    public BucketTransactionEventsData(){

    }

    @Override
    @Synchronized
    protected void addEventToCalculations(TransactionEventData eventData) {
        TransactionData transactionData = eventData.getTransactionData();
        BaseTransactionData transferTransaction =  transactionData.getBaseTransactions().get(transactionData.getBaseTransactions().size()-1);

        lastNumberOfTransactions = lastNumberOfTransactions+1;
        lastTurnOver = lastTurnOver.add(transferTransaction.getAmount());
        lastBalance =lastBalance.subtract(transferTransaction.getAmount());

        //TODO: make calculation here
        CalculatedDelta = CalculatedDelta + 0.01;
    }
}




