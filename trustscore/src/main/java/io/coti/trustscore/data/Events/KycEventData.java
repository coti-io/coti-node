package io.coti.trustscore.data.Events;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.trustscore.data.Enums.EventType;
import lombok.Data;

import java.util.Date;


@Data
public class KycEventData extends EventData implements ISignValidatable {
    private Hash userHash;
    private EventType eventType;
    private SignatureData signature;
    private Hash kycServerPublicKey;

    public KycEventData(Hash userHash, Date eventDate, EventType eventType, SignatureData signature) {
        super.setEventDate(eventDate);
        this.userHash = userHash;
        this.eventType = eventType;
        this.signature = signature;
    }

    @Override
    public Hash getSignerHash() {
        return kycServerPublicKey;
    }
}
