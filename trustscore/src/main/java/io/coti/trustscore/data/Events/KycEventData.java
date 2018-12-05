package io.coti.trustscore.data.Events;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.http.InsertEventRequest;
import lombok.Data;


@Data
public class KycEventData extends EventData implements ISignValidatable {
    private Hash userHash;
    private EventType eventType;
    private SignatureData signature;
    private Hash kycServerPublicKey;

    public KycEventData(InsertEventRequest request) {
        super(request);
    }

    @Override
    public Hash getSignerHash() {
        return kycServerPublicKey;
    }
}
