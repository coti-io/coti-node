package io.coti.cotinode.data;

import io.coti.cotinode.data.interfaces.IEntity;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class ConfirmedTransactionData implements IEntity {
    private transient Hash hash;
    private Date creationTIme;
    private Map<byte[], Double> addressHashToValueTransferredMapping;

    public ConfirmedTransactionData(Hash hash) {
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

        if (!(other instanceof ConfirmedTransactionData)) {
            return false;
        }
        return hash.equals(((ConfirmedTransactionData) other).hash);
    }
}
