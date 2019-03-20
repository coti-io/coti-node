package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;

public class TransactionDataHash implements IEntity {

    private Hash hash;

    public TransactionDataHash() {
    }

    public TransactionDataHash(Hash transactionDataHash) {
        this.hash = transactionDataHash;
    }

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return hash.toString();
    }
}