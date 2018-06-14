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
}
