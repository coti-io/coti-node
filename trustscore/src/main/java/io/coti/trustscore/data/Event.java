package io.coti.trustscore.data;

import io.coti.basenode.data.Hash;

import java.util.Date;

public class Event {
    Date eventDate;
    private Hash UniqueIdentifier;
    private EventType eventType;
    private double magnitude;
}
