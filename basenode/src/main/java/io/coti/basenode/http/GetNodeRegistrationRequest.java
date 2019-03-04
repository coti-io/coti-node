package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkType;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.basenode.http.data.NetworkTypeName;
import io.coti.basenode.http.data.NodeTypeName;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GetNodeRegistrationRequest extends Request implements ISignable {

    private String nodeHash;
    private SignatureData nodeSignature;
    private String nodeType;
    private String networkType;

    public GetNodeRegistrationRequest(@NotNull NodeType nodeType, @NotNull NetworkType networkType) {
        this.nodeType = NodeTypeName.valueOf(nodeType.toString()).getNode();
        this.networkType = NetworkTypeName.valueOf(networkType.toString()).getNetwork();
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
