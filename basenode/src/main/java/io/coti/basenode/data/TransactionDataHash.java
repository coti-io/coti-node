package io.coti.basenode.data;

public class TransactionDataHash extends DataHash {

    public TransactionDataHash() {
    }

    public TransactionDataHash(Hash transactionDataHash) {
        this.hash = transactionDataHash;
    }

}