package io.coti.common.data;

import io.coti.common.data.interfaces.IEntity;
import lombok.Data;

@Data
public class TransactionIndexData implements IEntity {

    private Hash transactionHash;
    private long index;
    private byte[] accumulatedHash;

    private TransactionIndexData() {
    }

    public TransactionIndexData(Hash transactionHash, long index, byte[] accumulatedHash) {
        this.transactionHash = transactionHash;
        this.index = index;
        this.accumulatedHash = accumulatedHash;
    }

    @Override
    public Hash getHash() {
        return new Hash(index);
    }

    @Override
    public void setHash(Hash hash) {
    }
}