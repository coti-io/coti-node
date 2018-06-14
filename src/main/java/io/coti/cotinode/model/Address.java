package io.coti.cotinode.model;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.model.Interfaces.IEntity;
import lombok.Data;

import java.util.Date;

@Data
public class Address implements IEntity {
    private Hash hash;
    private Date creationTime;

    public Address(Hash hash) {
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

        if (!(other instanceof Address)) {
            return false;
        }
        return hash.equals(((Address) other).hash);
    }
}
