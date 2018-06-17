package io.coti.cotinode.data;

import io.coti.cotinode.data.Hash;

import java.io.Serializable;

public interface IEntity extends Serializable {
    Hash getKey();
}
