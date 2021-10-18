package io.coti.nodemanager.websocket.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.coti.basenode.data.FeeData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.nodemanager.data.NetworkNodeStatus;
import lombok.Data;

@Data
public class NodeMessage {

    private String nodeHash;
    private NodeType nodeType;
    private NetworkNodeStatus nodeStatus;
    private String httpAddress;
    private String url;
    private String version;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private FeeData feeData;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double trustScore;

    public NodeMessage(NetworkNodeData networkNodeData, NetworkNodeStatus nodeStatus) {
        nodeHash = networkNodeData.getNodeHash().toString();
        nodeType = networkNodeData.getNodeType();
        this.nodeStatus = nodeStatus;
        httpAddress = networkNodeData.getHttpFullAddress();
        url = networkNodeData.getWebServerUrl();
        version = networkNodeData.getVersion();
        feeData = networkNodeData.getFeeData();
        trustScore = networkNodeData.getTrustScore();
    }

}
