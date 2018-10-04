package io.coti.trustscore.data;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.TransactionData;
import lombok.Data;
import lombok.Synchronized;

import java.math.BigDecimal;


@Data
public class BucketTransactionEventsData extends BucketEventData<TransactionEventData>{

    private double constantK = 1.098612289;

    private double periodTransactionContribution;
    private static final int periodTime = 60;


    private int currentDateNumberOfTransactions;
    private BigDecimal currentDateTurnOver;
    private BigDecimal currentDateBalance;


    @Override
    protected double getWeight() {
        return 1;
    }

    @Override
    protected double getDecay() {
        return 0;
    }

    @Override
    public int bucketPeriodTime() {
        return periodTime;
    }



    @Override
    @Synchronized
    protected void addEventToCalculations(TransactionEventData eventData) {

        TransactionData transactionData = eventData.getTransactionData();
        BaseTransactionData transferTransaction =  transactionData.getBaseTransactions().get(transactionData.getBaseTransactions().size()-1);

        currentDateNumberOfTransactions = currentDateNumberOfTransactions +1;
        currentDateTurnOver = currentDateTurnOver.add(transferTransaction.getAmount().abs());
        currentDateBalance = currentDateBalance.subtract(transferTransaction.getAmount());

        //TODO: make calculation here
        CalculatedDelta = CalculatedDelta + 0.01;
    }

    @Override
    public void ShiftCalculatedTsContribution() {
        periodTransactionContribution = periodTransactionContribution * 0.8 + 0.1;


        currentDateNumberOfTransactions = 0;
        currentDateTurnOver = new BigDecimal(0);
        currentDateBalance = new  BigDecimal(0);
    }


}




