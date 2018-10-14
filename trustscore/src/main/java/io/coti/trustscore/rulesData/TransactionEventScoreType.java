package io.coti.trustscore.rulesData;

public enum TransactionEventScoreType {
    TRANSACTION_FREQUENCY("TransactionFrequency"),
    TURNOVER("Turnover"),
    AVERAGE_BALANCE("AverageBalance");

    private String transactionEventScoreType;

    TransactionEventScoreType(String transactionEventScoreType) {
        this.transactionEventScoreType = transactionEventScoreType;
    }

    public String getTransactionEventScoreType() {
        return transactionEventScoreType;
    }
}
