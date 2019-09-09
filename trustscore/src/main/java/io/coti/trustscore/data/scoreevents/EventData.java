package io.coti.trustscore.data.scoreevents;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.scoreenums.EventType;
import io.coti.trustscore.http.InsertEventRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.Instant;

@Slf4j
@Data
public abstract class EventData implements IEntity, Serializable {

    private static final long serialVersionUID = -4980370548341874180L;
    private Instant eventDate;
    private Hash uniqueIdentifier;
    private EventType eventType;

    public EventData() {
    }

    public EventData(InsertEventRequest request) {
        if (request.eventType != EventType.TRANSACTION) {
            this.uniqueIdentifier = request.uniqueIdentifier;
            this.eventDate = Instant.now();
            this.eventType = request.eventType;
        }
        log.info(String.format("uniqueIdentifier: %s for type: %d", this.uniqueIdentifier.toHexString(), eventType.getValue()));
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


// todo delete