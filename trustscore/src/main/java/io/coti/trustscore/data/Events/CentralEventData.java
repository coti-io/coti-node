package io.coti.trustscore.data.Events;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.trustscore.data.Enums.EventType;
import lombok.Data;

import java.util.Date;


@Data
public class CentralEventData extends EventData implements ISignValidatable {

    public Hash userHash;
    public EventType eventType;
    public SignatureData signature;
    private Hash kycServerPublicKey;

    public CentralEventData(Hash userHash, Date eventDate, EventType eventType, SignatureData signature) {
        super.eventDate = eventDate;
        this.userHash = userHash;
        this.eventType = eventType;
        this.signature = signature;
    }

    @Override
    public Hash getHash() {
        return super.getHash();
    }


    @Override
    public Hash getSignerHash() {
        return kycServerPublicKey;
    }
}
