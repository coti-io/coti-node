package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class DisputeHistoryData implements IEntity {

    private Hash disputeHash;
    private Map<Hash, Map<Hash, ActionSide>> disputeEventHashToEventDisplayUserMap;

    public DisputeHistoryData(Hash disputeHash) {
        this.disputeHash = disputeHash;
        disputeEventHashToEventDisplayUserMap = new ConcurrentHashMap<>();
    }

    @Override
    public Hash getHash() {
        return disputeHash;
    }

    @Override
    public void setHash(Hash hash) {
        disputeHash = hash;
    }
}
