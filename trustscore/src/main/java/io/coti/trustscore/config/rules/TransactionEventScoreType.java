package io.coti.trustscore.config.rules;

public class TransactionEventScoreType {
    public static String TRANSACTION_FREQUENCY = "TransactionFrequency";
    public static  String TURNOVER = "Turnover";
    public static String AVERAGE_BALANCE = "AverageBalance";

    private String transactionEventScoreType;

    TransactionEventScoreType(String transactionEventScoreType) {
        this.transactionEventScoreType = transactionEventScoreType;
    }

    public String getTransactionEventScoreType() {
        return transactionEventScoreType;
    }
}
