package io.coti.cotinode.data;

import io.coti.cotinode.data.interfaces.IEntity;
import lombok.Data;

import java.util.Map;

@Data
public class PreBalanceDifferenceData implements IEntity {
    private transient Hash hash;
    private Hash userHash;
    private Map<Hash, Double> addressHashToValueTransferredMapping;
    private boolean DoubleSpendPreventionConsensus;
    private boolean TrustChainConsensus;

    public PreBalanceDifferenceData(Hash hash) {
        this.hash = hash;
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

        if (!(other instanceof PreBalanceDifferenceData)) {
            return false;
        }
        return hash.equals(((PreBalanceDifferenceData) other).hash);
    }
}
