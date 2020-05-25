package io.coti.basenode.http.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.basenode.http.interfaces.IRequest;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class NodeTrustScoreRequest implements ISignable, ISignValidatable, IRequest {

    @NotNull
    private SignatureData signature;
    @NotNull
    private Hash nodeManagerHash;
    @NotNull
    private List<Hash> nodesHash;

    @Override
    public SignatureData getSignature() {
        return signature;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.signature = signature;
    }

    @Override
    public Hash getSignerHash() {
        return nodeManagerHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        this.nodeManagerHash = signerHash;
    }

}
