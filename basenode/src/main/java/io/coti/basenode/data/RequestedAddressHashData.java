package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
public class RequestedAddressHashData implements IEntity {

    private static final long serialVersionUID = 8458125010309122807L;
    @NotNull
    private Hash addressHash;
    @NotNull
    private Instant lastUpdateTime;

    public RequestedAddressHashData(Hash addressHash) {
        this.addressHash = addressHash;
        this.lastUpdateTime = Instant.now();
    }

    @Override
    public Hash getHash() {
        return addressHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.addressHash = hash;
    }
}