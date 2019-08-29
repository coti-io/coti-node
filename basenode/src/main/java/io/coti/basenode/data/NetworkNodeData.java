package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import java.util.Objects;

@Data
public class NetworkNodeData implements IEntity, ISignable, ISignValidatable {

    private static final long serialVersionUID = 1827712996893751649L;
    private Hash nodeHash;
    private NodeType nodeType;
    private String address;
    private String httpPort;
    private String propagationPort;
    private String receivingPort;
    private NetworkType networkType;
    private transient Double trustScore;
    private String webServerUrl;
    private FeeData feeData;
    private SignatureData nodeSignature;
    private NodeRegistrationData nodeRegistrationData;

    public NetworkNodeData() {
    }

    public NetworkNodeData(NodeType nodeType, String address, String httpPort, Hash nodeHash, NetworkType networkType) {
        this.nodeType = nodeType;
        this.address = address;
        this.httpPort = httpPort;
        this.nodeHash = nodeHash;
        this.networkType = networkType;
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

    @JsonIgnore
    public String getHttpFullAddress() {
        return "http://" + address + ":" + httpPort;
    }

    @JsonIgnore
    public String getPropagationFullAddress() {
        return "tcp://" + address + ":" + propagationPort;
    }

    @JsonIgnore
    public String getReceivingFullAddress() {
        return "tcp://" + address + ":" + receivingPort;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeType, nodeHash);
    }

    @Override
    @JsonIgnore
    public Hash getHash() {
        return nodeHash;
    }

    @Override
    public void setHash(Hash hash) {
        nodeHash = hash;
    }

    @Override
    @JsonIgnore
    public SignatureData getSignature() {
        return nodeSignature;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.nodeSignature = signature;
    }

    @Override
    @JsonIgnore
    public Hash getSignerHash() {
        return nodeHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        nodeHash = signerHash;
    }

    public void clone(NetworkNodeData networkNodeData) {
        address = networkNodeData.getAddress();
        httpPort = networkNodeData.getHttpPort();
        propagationPort = networkNodeData.getPropagationPort();
        receivingPort = networkNodeData.getReceivingPort();
        networkType = networkNodeData.getNetworkType();
        webServerUrl = networkNodeData.getWebServerUrl();
        feeData = networkNodeData.getFeeData();
        nodeSignature = networkNodeData.getNodeSignature();
        nodeRegistrationData = networkNodeData.getNodeRegistrationData();
    }
}