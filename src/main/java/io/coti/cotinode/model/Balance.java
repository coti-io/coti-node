package io.coti.cotinode.model;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.model.Interfaces.IEntity;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class Balance implements IEntity {
    public Hash hash;
    private Date creationTIme;
    private Map<byte[], Double> addressHashToValueTransferredMapping;

    public Balance(Hash hash) {
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

        if (!(other instanceof Balance)) {
            return false;
        }
        return hash.equals(((Balance) other).hash);
    }
}
