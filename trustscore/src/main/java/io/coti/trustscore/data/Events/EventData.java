package io.coti.trustscore.data.Events;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.Enums.EventType;
import lombok.Data;

import java.util.Date;

@Data
public abstract class EventData implements IEntity {

    protected Date eventDate;
    protected Hash uniqueIdentifier;
    protected EventType eventType;


    public EventData() {
        this.eventDate = new Date();
    }

    @Override
    public Hash getHash() {
        return this.uniqueIdentifier;
    }

    @Override
    public void setHash(Hash hash) {
        this.uniqueIdentifier = hash;
    }
}


