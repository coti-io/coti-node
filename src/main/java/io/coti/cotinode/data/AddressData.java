package io.coti.cotinode.data;

import io.coti.cotinode.data.interfaces.IEntity;
import lombok.Data;

import java.util.Date;

@Data
public class AddressData implements IEntity {
    private transient Hash hash;
    private Date creationTime;

    public AddressData(Hash hash) {
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

        if (!(other instanceof AddressData)) {
            return false;
        }
        return hash.equals(((AddressData) other).hash);
    }
}
