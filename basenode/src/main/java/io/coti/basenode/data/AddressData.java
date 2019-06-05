package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IPropagatable;
import lombok.Data;

import java.time.Instant;

@Data
public class AddressData implements IPropagatable {

    private static final long serialVersionUID = -230326735731078747L;
    private transient Hash hash;
    private Instant creationTime;

    private AddressData() {
    }

    public AddressData(Hash hash) {
        this.hash = hash;
        creationTime = Instant.now();
    }

    public AddressData(Hash hash, Instant creationTime) {
        this.hash = hash;
        this.creationTime = creationTime;
    }

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof AddressData)) {
            return false;
        }
        return hash.equals(((AddressData) other).hash);
    }
}
