package io.coti.cotinode.data;

import io.coti.cotinode.data.interfaces.IEntity;
import lombok.Data;

@Data
public class BaseTransactionData implements IEntity {
    private transient Hash hash;
    private AddressData addressData;
    private long value;
    private Hash transactionHash;
    private int indexInTransactionsChain;
    private BaseTransactionData nextBaseTransactionData;

    public BaseTransactionData(Hash hash, double amount) {
        this.hash = hash;
    }

    @Override
    public Hash getKey() {
        return hash;
    }

    @Override
    public void setKey(Hash hash) {
        this.hash = hash;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof BaseTransactionData)) {
            return false;
        }
        return hash.equals(((BaseTransactionData) other).hash);
    }
}