package io.coti.cotinode.model;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.model.Interfaces.IEntity;
import lombok.Data;

import java.util.Map;

@Data
public class PreBalance implements IEntity {
    private Hash hash;
    private Hash userHash;
    private Map<Hash, Double> addressHashToValueTransferredMapping;

    public PreBalance(Hash hash) {
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

        if (!(other instanceof PreBalance)) {
            return false;
        }
        return hash.equals(((PreBalance) other).hash);
    }
}
