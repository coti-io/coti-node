package io.coti.cotinode.data;

import io.coti.cotinode.data.interfaces.IEntity;
import lombok.Data;

@Data
public class BaseTransactionData implements IEntity {
    private transient Hash hash;
    private String signature;
    private AddressData addressData;
    private Hash addressHash;
    private double amount;
    private Hash transactionHash;
    private int indexInTransactionsChain;
    private BaseTransactionData nextBaseTransactionData;

    private BaseTransactionData(){}

    public BaseTransactionData(String hashString, double amount){
        this.hash = new Hash(hashString);
        this.amount = amount;
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