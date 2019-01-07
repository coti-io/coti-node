package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DisputeHistoryData implements IEntity{

    private Hash disputeHash;
    private List<Hash> disputeEventHashes;

    public DisputeHistoryData(Hash disputeHash) {
        this.disputeHash = disputeHash;
        disputeEventHashes = new ArrayList<>();
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
