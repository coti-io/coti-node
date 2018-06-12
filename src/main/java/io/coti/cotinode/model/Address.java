package io.coti.cotinode.model;

import io.coti.cotinode.model.Interfaces.IAddress;

public class Address implements IAddress {
    @Override
    public byte[] getKey() {
        return new byte[0];
    }
}
