package io.coti.cotinode.data;

import io.coti.cotinode.data.interfaces.IEntity;
import lombok.Data;

import java.util.Map;

@Data
public class UnconfirmedTransactionData implements IEntity {
    private transient Hash hash;
    private Map<Hash, Double> addressHashToValueTransferredMapping;


    private boolean DoubleSpendPreventionConsensus;
    private boolean TrustChainConsensus;

    public UnconfirmedTransactionData(Hash hash) {
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

        if (!(other instanceof UnconfirmedTransactionData)) {
            return false;
        }
        return hash.equals(((UnconfirmedTransactionData) other).hash);
    }
}
