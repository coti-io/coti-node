package io.coti.cotinode.model;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.model.Interfaces.IEntity;
import lombok.Data;

@Data
public class BaseTransaction implements IEntity {
    private Hash hash;
    private Address address;
    private long value;
    private Hash transactionHash;
    private int indexInTransactionsChain;
    private BaseTransaction nextBaseTransaction;

    public BaseTransaction(Hash hash) {
        this.hash = hash;
    }

    @Override
    public Hash getKey() {
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof BaseTransaction)) {
            return false;
        }
        return hash.equals(((BaseTransaction) other).hash);
    }
}