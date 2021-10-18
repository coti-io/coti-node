package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkType;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.basenode.http.data.NetworkTypeName;
import io.coti.basenode.http.data.NodeTypeName;
import io.coti.basenode.http.interfaces.IRequest;
import lombok.Data;

@Data
public class GetNodeRegistrationRequest implements ISignable, IRequest {

    private String nodeHash;
    private SignatureData nodeSignature;
    private String nodeType;
    private String networkType;

    public GetNodeRegistrationRequest(NodeType nodeType, NetworkType networkType) {
        this.nodeType = NodeTypeName.getByNodeType(nodeType).getNode();
        this.networkType = NetworkTypeName.getByNetworkType(networkType).getNetwork();
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        nodeHash = signerHash.toString();
    }

    @Override
    public void setSignature(SignatureData signature) {
        nodeSignature = signature;
    }
}
