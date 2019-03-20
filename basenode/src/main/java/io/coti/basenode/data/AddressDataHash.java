package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;

public class AddressDataHash implements IEntity {

    private Hash addressDataHash;

    public AddressDataHash() {
    }

    public AddressDataHash(Hash addressDataHash) {
        this.addressDataHash = addressDataHash;
    }

    @Override
    public Hash getHash() {
        return addressDataHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.addressDataHash = hash;
    }


}