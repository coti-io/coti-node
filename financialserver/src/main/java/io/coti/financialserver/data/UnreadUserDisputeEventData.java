package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class UnreadUserDisputeEventData implements IEntity {

    private Hash userHash;
    private Map<Hash, ActionSide> disputeEventHashToEventDisplaySideMap;

    public UnreadUserDisputeEventData(Hash userHash) {
        this.userHash = userHash;
        disputeEventHashToEventDisplaySideMap = new ConcurrentHashMap<>();
    }

    @Override
    public Hash getHash() {
        return userHash;
    }

    @Override
    public void setHash(Hash hash) {
        userHash = hash;
    }
}
