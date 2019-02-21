package io.coti.dspnode.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

@Data
public class NotTotalConfirmedTransactionHash implements IEntity {

    private Hash transactionHash;

    public NotTotalConfirmedTransactionHash(Hash transactionHash) {
        this.transactionHash = transactionHash;
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