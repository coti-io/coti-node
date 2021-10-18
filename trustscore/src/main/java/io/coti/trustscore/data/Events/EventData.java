package io.coti.trustscore.data.Events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.http.InsertEventRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.Instant;

@Slf4j
@Data
public abstract class EventData implements IEntity, Serializable, ISignValidatable {

    private static final long serialVersionUID = -4980370548341874180L;
    private Instant eventDate;
    private Hash uniqueIdentifier;
    private EventType eventType;
    private Hash eventSignerHash;
    private SignatureData eventSignature;

    protected EventData() {
    }

    protected EventData(InsertEventRequest request) {
        if (request.getEventType() != EventType.TRANSACTION) {
            this.uniqueIdentifier = request.getUniqueIdentifier();
            this.eventDate = Instant.now();
            this.eventType = request.getEventType();
        }
        log.info(String.format("uniqueIdentifier: %s for type: %d", this.uniqueIdentifier.toHexString(), eventType.getValue()));
    }

    @Override
    @JsonIgnore
    public Hash getHash() {
        return this.uniqueIdentifier;
    }

    @Override
    public void setHash(Hash hash) {
        this.uniqueIdentifier = hash;
    }

    @Override
    @JsonIgnore
    public SignatureData getSignature() {
        return eventSignature;
    }

    @Override
    @JsonIgnore
    public Hash getSignerHash() {
        return eventSignerHash;
    }
}


