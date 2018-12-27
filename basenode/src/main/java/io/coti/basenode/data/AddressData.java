package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IPropagatable;
import lombok.Data;

import java.util.Date;

@Data
public class AddressData implements IPropagatable {
    private AddressData() {
    }

    private transient Hash hash;
    private Date creationTime;

    public AddressData(Hash hash) {
        this.hash = hash;
        creationTime = new Date();
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
