package io.coti.basenode.data.interfaces;

import io.coti.basenode.data.Hash;

import java.io.Serializable;

public interface IEntity extends Serializable {
    Hash getHash();

    void setHash(Hash hash);
}