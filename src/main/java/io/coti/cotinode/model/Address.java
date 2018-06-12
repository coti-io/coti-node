package io.coti.cotinode.model;

import io.coti.cotinode.model.Interfaces.IAddress;

import java.util.Arrays;
import java.util.Date;

public class Address implements IAddress {
    private byte[] hash;
    private Date creationTime;

    public Address(byte[] hash) {
        this.hash = hash;
    }

    @Override
    public byte[] getKey() {
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
        return Arrays.equals(hash, ((Address) other).getKey());
    }
}
