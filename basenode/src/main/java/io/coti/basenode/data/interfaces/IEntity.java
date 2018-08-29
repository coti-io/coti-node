package io.coti.basenode.data.interfaces;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.coti.basenode.data.Hash;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface IEntity extends Serializable {
    Hash getHash();

    void setHash(Hash hash);
}