package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;

public class InitialFundDataHash implements IEntity {

    private Hash hash;

    public InitialFundDataHash(Hash hash) {
        this.hash = hash;
    }

    @Override
    public Hash getHash() {
        return this.hash;
    }

    @Override
    public void setHash(Hash hash) {

    }
}
