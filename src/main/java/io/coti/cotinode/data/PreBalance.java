package io.coti.cotinode.data;

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
