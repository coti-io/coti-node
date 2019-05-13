package io.coti.nodemanager.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

@Data
public class ActiveNodeData implements IEntity {

    private static final long serialVersionUID = 6551846513058427089L;
    private Hash nodeHash;
    private NetworkNodeData networkNodeData;

    private ActiveNodeData() {
    }

    public ActiveNodeData(Hash nodeHash, NetworkNodeData networkNodeData) {
        this.nodeHash = nodeHash;
        this.networkNodeData = networkNodeData;
    }

    @Override
    public Hash getHash() {
        return nodeHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.nodeHash = hash;
    }
}
