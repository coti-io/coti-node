package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@Data
public class NetworkNodeData implements IEntity, ISignable, ISignValidatable {
    private Hash nodeHash;
    private NodeType nodeType;
    private String address;
    private String httpPort;
    private String propagationPort;
    private String receivingPort;
    private String recoveryServerAddress;
    private NetworkType networkType;
    private transient Double trustScore;
    private FeeData feeData;
    private SignatureData nodeSignature;
    private NodeRegistrationData nodeRegistrationData;


    public NetworkNodeData(NodeType nodeType, String address, String httpPort, Hash nodeHash, NetworkType networkType) {
        this.nodeType = nodeType;
        this.address = address;
        this.httpPort = httpPort;
        this.nodeHash = nodeHash;
        this.networkType = networkType;
    }

    public NetworkNodeData(NodeType nodeType, String address, String httpPort, Hash nodeHash, NetworkType networkType, FeeData feeData) {
        this(nodeType, address, httpPort, nodeHash, networkType);
        this.feeData = feeData;
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
        return Objects.hash(nodeType, nodeHash);
    }

    public Hash getHash() {
        return nodeHash;
    }

    public void setHash(Hash hash) {
        nodeHash = hash;
    }

    @Override
    public SignatureData getSignature() {
        return nodeSignature;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.nodeSignature = signature;
    }

    @Override
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
        recoveryServerAddress = networkNodeData.getRecoveryServerAddress();
        networkType = networkNodeData.getNetworkType();
        nodeSignature = networkNodeData.getNodeSignature();
        nodeRegistrationData = networkNodeData.getNodeRegistrationData();
    }
}