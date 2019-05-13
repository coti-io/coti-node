package io.coti.financialserver.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.financialserver.data.interfaces.IDisputeEvent;
import lombok.Data;

import java.time.Instant;

@Data
public class DisputeEventData implements IEntity {

    private static final long serialVersionUID = 5473835124802284108L;
    private Hash hash;
    private Instant creationTime;
    private IDisputeEvent eventObject;

    public DisputeEventData(IDisputeEvent eventObject) {
        this.eventObject = eventObject;
        init();
    }

    public void init() {
        this.creationTime = Instant.now();
        this.hash = CryptoHelper.cryptoHash(creationTime.toString().getBytes());
    }

}
