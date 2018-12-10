package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ConsumerDisputesData implements IEntity {
    private Hash consumerHash;
    private List<Hash> disputeHashes;

    public void appendDisputeHash(Hash disputeHash) {
        if(disputeHashes == null) {
            disputeHashes = new ArrayList<>();
        }
        disputeHashes.add(disputeHash);
    }

    @Override
    public Hash getHash() {
        return consumerHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.consumerHash = hash;
    }
}
