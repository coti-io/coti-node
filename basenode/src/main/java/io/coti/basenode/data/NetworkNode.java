package io.coti.basenode.data;

import io.coti.basenode.crypto.NodeCryptoHelper;
import lombok.Data;

import java.util.Objects;

@Data
public class NetworkNode {
    private NodeType nodeType;
    private String address;
    private String httpPort;
    private String propagationPort;
    private String receivingPort;
    private Hash hash;
    private String recoveryServerAddress;

    public NetworkNode(NodeType nodeType, String address, String httpPort) {
        this.nodeType = nodeType;
        this.address = address;
        this.httpPort = httpPort;
        this.hash = NodeCryptoHelper.getNodeHash();
    }

    public NetworkNode() {
    }

    @Override
    public boolean equals(Object a) {
        if (a instanceof NetworkNode) {
            NetworkNode aNetworkNode = (NetworkNode) a;
            return nodeType.equals(aNetworkNode.nodeType) && hash.equals(aNetworkNode.getHash());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeType, address, httpPort);
    }

    public String getHttpFullAddress(){
        return "http://" + address + ":" + httpPort;
    }

    public String getPropagationFullAddress(){
        return "tcp://" + address + ":" + propagationPort;
    }

    public String getReceivingFullAddress(){
        return "tcp://" + address + ":" + receivingPort;
    }
}