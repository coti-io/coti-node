package io.coti.trustscore.data.Enums;


public enum EventType {
    TRANSACTION(0),
    HIGH_FREQUENCY_EVENTS(1),
    BEHAVIOR_EVENT(2)
    /*INSUFFICIENT_FUNDS(2),
    Dispute_LOST(4),
    Dispute_WON(5),
    CLAIM(6),
    LATE_FULFILLMENT(7),
    NON_FULFILLMENT(8),
    DOUBLE_SPENDING(9),
    Incorrect_Transaction(10)*/;


    private int value;

    EventType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

