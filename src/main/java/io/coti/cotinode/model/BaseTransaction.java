package io.coti.cotinode.model;

import io.coti.cotinode.model.Interfaces.IAddress;
import io.coti.cotinode.model.Interfaces.IBaseTransaction;

import java.util.Arrays;

public class BaseTransaction implements IBaseTransaction {
    private byte[] hash;
    private Address address;
    private long value;
    private byte[] transactionHash;
    private int indexInTransactionsChain;
    private BaseTransaction nextBaseTransaction;

    public BaseTransaction(byte[] hash){
        this.hash = hash;
    }

    @Override
    public byte[] getKey() {
        return hash;
    }

    @Override
    public boolean equals(Object other){
        if (other == this){
            return true;
        }

        if(!(other instanceof BaseTransaction)){
            return false;
        }
        return Arrays.equals(hash, ((BaseTransaction) other).getKey());
    }
}