package io.coti.trustscore.data.Events;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.http.InsertEventRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.nio.ByteBuffer;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

@Slf4j
@Data
public abstract class EventData implements IEntity, Serializable, ISignValidatable {

    private Date eventDate;
    private Hash uniqueIdentifier;
    private EventType eventType;
    private Hash eventSignerHash;
    private SignatureData eventSignature;

    public EventData() {
    }


    public EventData(InsertEventRequest request) throws Exception {
        if (request.eventType != EventType.TRANSACTION) {
            if (request.uniqueIdentifier == null)
                throw new Exception("hash is empty");

            if (!this.verifyHashOfEvent(request))
                throw new Exception("wrong hash in event");

            this.uniqueIdentifier = request.uniqueIdentifier;
            this.eventDate = request.eventDate;
            this.eventType = request.eventType;
        }

        log.info(String.format("uniqueIdentifier: %s for type: %d", this.uniqueIdentifier.toHexString(), eventType.getValue()));
    }


    private Hash getHashOfEvent(InsertEventRequest request){
        ByteBuffer buffer = ByteBuffer.allocate(eventSignerHash.getBytes().length + Long.BYTES + Integer.BYTES );
        buffer.put(request.signerHash.getBytes()).putInt(request.eventType.getValue()).putLong(request.eventDate.getTime());
        Hash hash  = new Hash(CryptoHelper.cryptoHash(buffer.array()).getBytes());
        return hash;
    }

    private boolean verifyHashOfEvent(InsertEventRequest request){
        Hash hash = getHashOfEvent(request);
        return Arrays.equals(hash.getBytes(),request.uniqueIdentifier.getBytes());
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


