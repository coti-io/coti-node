package io.coti.cotinode.model;

import io.coti.cotinode.model.Interfaces.IBalance;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

public class Balance implements IBalance {
    public byte[] hash;
    private Date creationTIme;
    private Map<byte[], Double> addressHashTovValueTransferedMapping;

    public Balance(byte[] hash){
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

        if(!(other instanceof Balance)){
            return false;
        }
        return Arrays.equals(hash, ((Balance) other).getKey());
    }
}
