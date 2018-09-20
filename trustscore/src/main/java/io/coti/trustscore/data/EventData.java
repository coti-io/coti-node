package io.coti.trustscore.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.util.Date;

@Data
public class EventData implements IEntity {
    private Date eventDate;
    private Hash uniqueIdentifier;
    private EventType eventType;
    private double magnitude;

    @Override
    public Hash getHash() {
        return this.uniqueIdentifier;
    }

    @Override
    public void setHash(Hash hash) {
        this.uniqueIdentifier = hash;
    }
}
