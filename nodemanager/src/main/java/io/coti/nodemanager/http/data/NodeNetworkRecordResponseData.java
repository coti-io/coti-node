package io.coti.nodemanager.http.data;

import io.coti.basenode.data.Hash;
import io.coti.nodemanager.data.NetworkNodeStatus;
import io.coti.nodemanager.data.NodeNetworkDataRecord;
import lombok.Data;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

@Data
public class NodeNetworkRecordResponseData implements Serializable {

    private static final long serialVersionUID = 7814283160669885668L;
    private String hash;
    private Instant recordTime;
    private NetworkNodeStatus nodeStatus;
    private NetworkNodeResponseData networkNodeData;
    private Pair<LocalDate, String> statusChainRef;
    private boolean notOriginalEvent;

    public NodeNetworkRecordResponseData(NodeNetworkDataRecord nodeNetworkDataRecord) {
        this.hash = nodeNetworkDataRecord.getHash().toString();
        this.recordTime = nodeNetworkDataRecord.getRecordTime();
        this.nodeStatus = nodeNetworkDataRecord.getNodeStatus();
        this.networkNodeData = new NetworkNodeResponseData(nodeNetworkDataRecord.getNetworkNodeData());
        Pair<LocalDate, Hash> reference = nodeNetworkDataRecord.getStatusChainRef();
        if (reference != null) {
            this.statusChainRef = new ImmutablePair<>(reference.getLeft(), reference.getRight().toString());
        }
        this.notOriginalEvent = nodeNetworkDataRecord.isNotOriginalEvent();
    }
}
