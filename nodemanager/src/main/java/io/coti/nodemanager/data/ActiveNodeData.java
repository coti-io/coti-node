package io.coti.nodemanager.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

@Data
public class ActiveNodeData implements IEntity {

    private Hash hash;
    private NetworkNodeData networkNodeData;

    private ActiveNodeData(){}

    public ActiveNodeData(Hash hash, NetworkNodeData networkNodeData) {
        this.hash = hash;
        this.networkNodeData = networkNodeData;
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
