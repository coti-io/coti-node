package io.coti.common.data.interfaces;


import io.coti.common.data.Hash;

import java.io.Serializable;

public interface IEntity extends Serializable {
    Hash getKey();
    void setKey(Hash hash);
}
