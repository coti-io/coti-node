package io.coti.trustscore.data.Events;

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

    private Instant eventDate;
    private Hash uniqueIdentifier;
    private EventType eventType;
    private Hash eventSignerHash;
    private SignatureData eventSignature;

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

    public void setSignatureData(SignatureData eventSignature) {
        this.eventSignature = eventSignature;
    }


    @Override
    public Hash getHash() {
        return this.uniqueIdentifier;
    }

    @Override
    public void setHash(Hash hash) {
        this.uniqueIdentifier = hash;
    }

    @Override
    public SignatureData getSignature() {
        return eventSignature;
    }

    @Override
    public Hash getSignerHash() {
        return eventSignerHash;
    }
}


