package io.coti.nodemanager.data;


import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.util.LinkedList;

@Data
public class NodeHistoryData implements IEntity {

    private Hash nodeHash;
    private NodeType nodeType;
    private NetworkNodeStatus nodeStatus;
    private LinkedList<NodeNetworkDataTimestamp> nodeHistory = new LinkedList<>();

    public NodeHistoryData(NetworkNodeStatus nodeStatus, Hash nodeHash, NodeType nodeType) {
        this.nodeType = nodeType;
        this.nodeStatus = nodeStatus;
        this.nodeHash = nodeHash;
    }

    @Override
    public Hash getHash() {
        return nodeHash;
    }

    @Override
    public void setHash(Hash hash) {
        nodeHash = hash;
    }

}
