package io.coti.nodemanager.data;


import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;
import org.apache.commons.collections4.map.LinkedMap;

@Data
public class NodeHistoryData implements IEntity {

    private static final long serialVersionUID = 1342674273074140300L;
    private Hash nodeHistoryHash;  //compound
    private LinkedMap<Hash, NodeNetworkDataRecord> nodeHistory = new LinkedMap<>();

    public NodeHistoryData() {
    }

    public NodeHistoryData(Hash nodeHistoryHash) {
        this.nodeHistoryHash = nodeHistoryHash;
    }

    @Override
    public Hash getHash() {
        return nodeHistoryHash;
    }

    @Override
    public void setHash(Hash hash) {
        nodeHistoryHash = hash;
    }
}
