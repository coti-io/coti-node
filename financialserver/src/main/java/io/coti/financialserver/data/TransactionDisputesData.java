package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TransactionDisputesData implements IEntity {
    private Hash transactionHash;
    private List<Hash> disputeHashes;

    public void appendDisputeHash(Hash disputeHash) {
        if (disputeHashes == null) {
            disputeHashes = new ArrayList<>();
        }
        disputeHashes.add(disputeHash);
    }

    @Override
    public Hash getHash() {
        return transactionHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.transactionHash = hash;
    }
}
