package io.coti.nodemanager.data;


import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.nodemanager.database.NetworkNodeStatus;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class NodeHistoryData implements IEntity {
    @Override
    public String toString() {
        return "NodeHistoryData{" +
                "nodeType=" + nodeType +
                ", nodeStatus=" + nodeStatus +
                ", nodeHash=" + nodeHash +
                ", nodeHistory=" + nodeHistory.toString() +
                '}';
    }

    private NodeType nodeType;
    private NetworkNodeStatus nodeStatus;
    private Hash nodeHash;
    private LinkedList<NodeNetworkDataTimestamp> nodeHistory = new LinkedList<>();


    public NodeHistoryData(NetworkNodeStatus nodeStatus, Hash nodeHash, NodeType nodeType) {
        this.nodeType = nodeType;
        this.nodeStatus = nodeStatus;
        this.nodeHash = nodeHash;
    }

    private NodeHistoryData() {
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
