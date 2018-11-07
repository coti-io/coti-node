package io.coti.trustscore.data.Enums;

public enum TransactionEventScoreType {

    TRANSACTION_FREQUENCY("TransactionFrequency"),
    TURNOVER("Turnover"),
    AVERAGE_BALANCE("AverageBalance");

    private String text;

    TransactionEventScoreType(String text) {
        this.text = text;
    }

    public static TransactionEventScoreType enumFromString(String text) {
        for (TransactionEventScoreType value: TransactionEventScoreType.values()) {
            if (value.text.equalsIgnoreCase(text)) {
                return value;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return text;
    }
}
