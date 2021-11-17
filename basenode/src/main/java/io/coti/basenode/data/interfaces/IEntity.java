package io.coti.basenode.data.interfaces;

import io.coti.basenode.data.Hash;

import java.io.Serializable;
import java.util.Map;

public interface IEntity extends Serializable {
    Hash getHash();

    void setHash(Hash hash);

    default Map<String, Object> getJson() {return null;} ;
}