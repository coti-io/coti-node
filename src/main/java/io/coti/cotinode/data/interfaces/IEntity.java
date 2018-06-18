package io.coti.cotinode.data.interfaces;

import io.coti.cotinode.data.Hash;

import java.io.Serializable;

public interface IEntity extends Serializable {
    Hash getKey();
    void setKey(Hash hash);
}
