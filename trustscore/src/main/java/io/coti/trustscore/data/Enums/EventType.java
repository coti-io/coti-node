package io.coti.trustscore.data.Enums;


public enum EventType {
    TRANSACTION(0),
    DISPUTE(1),
    INSUFFICIENT_FUNDS(2),
    CHARGEBACK(3),
    Dispute_LOST(4),
    Dispute_WON(5),
    CLAIM(6),
    LATE_FULFILLMENT(7),
    NON_FULFILLMENT(8),
    DOUBLE_SPENDING(9);


    private int value;

    EventType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}