package io.coti.cotinode.model;

import io.coti.cotinode.model.Interfaces.IEntity;
import lombok.Data;

import java.util.Arrays;
import java.util.Map;

@Data
public class PreBalance implements IEntity {
    private byte[] hash;
    private byte[] userHash;
    private Map<byte[], Double> addressHashToValueTransferredMapping;

    public PreBalance(byte[] hash){
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

        if(!(other instanceof PreBalance)){
            return false;
        }
        return Arrays.equals(hash, ((PreBalance) other).getKey());
    }
}
