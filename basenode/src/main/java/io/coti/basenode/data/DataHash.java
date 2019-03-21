package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;

public class DataHash implements IEntity {

    protected Hash hash;

    public DataHash(){
        // Empty constructor for serialization
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
