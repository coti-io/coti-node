package io.coti.nodemanager.http.data;

import io.coti.basenode.data.FeeData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NetworkType;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.http.data.interfaces.IResponseData;
import lombok.Data;

@Data
public class NetworkNodeResponseData implements IResponseData {

    private String nodeHash;
    private NodeType nodeType;
    private String address;
    private String httpPort;
    private String propagationPort;
    private String receivingPort;
    private NetworkType networkType;
    private String webServerUrl;
    private FeeData feeData;

    public NetworkNodeResponseData(NetworkNodeData networkNodeData) {
        this.nodeHash = networkNodeData.getNodeHash().toString();
        this.nodeType = networkNodeData.getNodeType();
        this.address = networkNodeData.getAddress();
        this.httpPort = networkNodeData.getHttpPort();
        this.propagationPort = networkNodeData.getPropagationPort();
        this.receivingPort = networkNodeData.getReceivingPort();
        this.networkType = networkNodeData.getNetworkType();
        this.webServerUrl = networkNodeData.getWebServerUrl();
        this.feeData = networkNodeData.getFeeData();
    }
}
