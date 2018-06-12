package io.coti.cotinode.model.Interfaces;

import java.io.Serializable;

public interface IEntity extends Serializable {
    byte[] getKey();
    byte[] getBytes();
}
