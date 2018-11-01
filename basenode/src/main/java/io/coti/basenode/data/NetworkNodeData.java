package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import java.util.Objects;

@Data
public class NetworkNodeData implements IEntity, ISignable, ISignValidatable {
    private NodeType nodeType;
    private String address;
    private String httpPort;
    private String propagationPort;
    private String receivingPort;
    private String recoveryServerAddress;
    private Double trustScore;

    private Hash nodeHash;
    private SignatureData signature;


    public NetworkNodeData(NodeType nodeType, String address, String httpPort, Hash nodeHash) {
        this.nodeType = nodeType;
        this.address = address;
        this.httpPort = httpPort;
        this.nodeHash = nodeHash;
    }

    public NetworkNodeData() {
    }

    @Override
    public boolean equals(Object a) {
        if (a instanceof NetworkNodeData) {
            NetworkNodeData aNetworkNodeData = (NetworkNodeData) a;
            return nodeType.equals(aNetworkNodeData.nodeType) && nodeHash.equals(aNetworkNodeData.getHash());
        } else {
            return false;
        }
    }

    public String getHttpFullAddress() {
        return "http://" + address + ":" + httpPort;
    }

    public String getPropagationFullAddress() {
        return "tcp://" + address + ":" + propagationPort;
    }

    public String getReceivingFullAddress() {
        return "tcp://" + address + ":" + receivingPort;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeType, address, httpPort, nodeHash);
    }

    public Hash getHash(){
        return nodeHash;
    }

    public void setHash(Hash hash){
        nodeHash = hash;
    }

    @Override
    public SignatureData getSignature() {
        return signature;
    }

    @Override
    public Hash getSignerHash() {
        return nodeHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        nodeHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.signature = signature;
    }
}