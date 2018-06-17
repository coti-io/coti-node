package io.coti.cotinode.data;

import lombok.Data;

@Data
public class BaseTransactionObject {
    private Hash addressHash;
    private double amount;

    public BaseTransactionObject(Hash addressHash, double amount){
        this.addressHash = addressHash;
        this.amount = amount;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof BaseTransactionObject)) {
            return false;
        }
        return addressHash.equals(((BaseTransactionObject) other).addressHash);
    }
}
