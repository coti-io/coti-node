package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;

public class TransactionDataHash implements IEntity {

    private Hash transactionDataHash;

    public TransactionDataHash() {
    }

    public TransactionDataHash(Hash transactionDataHash) {
        this.transactionDataHash = transactionDataHash;
    }

    @Override
    public Hash getHash() {
        return transactionDataHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.transactionDataHash = hash;
    }

    @Override
    public String toString() {
        // "TransactionDataHash: {}"
        //return transactionDataHash.toString();
//        return "TransactionDataHash = " + transactionDataHash.toString();
        return transactionDataHash.toString();
    }
}