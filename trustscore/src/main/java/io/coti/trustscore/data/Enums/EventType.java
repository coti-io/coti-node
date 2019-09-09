package io.coti.trustscore.data.Enums;

public enum EventType {
    TRANSACTION(0),
    HIGH_FREQUENCY_EVENTS(1),
    BEHAVIOR_EVENT(2),
    INITIAL_EVENT(3),
    NOT_FULFILMENT_EVENT(4);

    private int value;

    EventType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

// todo delete


