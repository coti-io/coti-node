package io.coti.trustscore.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public abstract class EventData implements IEntity {

    protected Date eventDate;
    protected Hash uniqueIdentifier;
    protected EventType eventType;
    protected double magnitude;

    public EventData(){
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


