package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.http.data.NetworkTypeName;
import io.coti.basenode.http.data.NodeTypeName;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
public class NodeRegistrationData implements IEntity, ISignValidatable {

    private static final long serialVersionUID = -3559786768335905690L;
    @NotNull
    private Hash nodeHash;
    @NotNull
    private NodeType nodeType;
    @NotNull
    private NetworkType networkType;
    @NotNull
    private Instant creationTime;
    @NotNull
    private Hash registrarHash;
    @NotNull
    @Valid
    private SignatureData registrarSignature;

    @Override
    @JsonIgnore
    public Hash getHash() {
        return nodeHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.nodeHash = hash;
    }

    @Override
    @JsonIgnore
    public Hash getSignerHash() {
        return registrarHash;
    }

    @Override
    @JsonIgnore
    public SignatureData getSignature() {
        return registrarSignature;
    }

    public void setNodeType(String node) {
        try {
            this.nodeType = NodeType.valueOf(node);
        } catch (IllegalArgumentException e) {
            this.nodeType = NodeTypeName.getNodeType(node);
        }
    }

    public void setNetworkType(String network) {
        try {
            this.networkType = NetworkType.valueOf(network);
        } catch (IllegalArgumentException e) {
            this.networkType = NetworkTypeName.getNetworkType(network);
        }
    }

    @JsonIgnore
    public String getNode() {
        return NodeTypeName.valueOf(nodeType.toString()).getNode();
    }

    @JsonIgnore
    public String getNetwork() {
        return NetworkTypeName.valueOf(networkType.toString()).getNetwork();
    }

}
