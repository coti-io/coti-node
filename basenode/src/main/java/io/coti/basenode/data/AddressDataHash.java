package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;

public class AddressDataHash implements IEntity {

    private Hash hash;

    public AddressDataHash() {
    }

    public AddressDataHash(Hash addressDataHash) {
        this.hash = addressDataHash;
    }

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }


}