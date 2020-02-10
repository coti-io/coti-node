package io.coti.trustscore.data.Enums;

public enum CompensableEventScoreType {
    DEPOSITED_BALANCE("DepositedBalance"),
    CLOSED_DEPOSITS("ClosedDeposits"),
    LATE_FULFILLMENT("LateFulfillment"),
    NON_FULFILMENT("NonFulfillment");

    private String text;

    CompensableEventScoreType(String text) {
        this.text = text;
    }

    public static CompensableEventScoreType enumFromString(String text) {
        for (CompensableEventScoreType value : CompensableEventScoreType.values()) {
            if (value.text.equalsIgnoreCase(text)) {
                return value;
            }
        }
        throw new IllegalArgumentException(String.format("got event name %s, which not exists", text));
    }

    @Override
    public String toString() {
        return text;
    }
}
