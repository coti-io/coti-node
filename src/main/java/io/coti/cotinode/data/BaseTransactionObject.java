package io.coti.cotinode.data;

import lombok.Data;

@Data
public class BaseTransactionObject {
    private byte[] addressHash;
    private double amount;

    public BaseTransactionObject(byte[] addressHash, double amount){
        this.addressHash = addressHash;
        this.amount = amount;
    }
}
