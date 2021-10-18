package io.coti.basenode.http.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeTrustScoreData;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.basenode.http.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class NodeTrustScoreResponse extends Response implements ISignable, ISignValidatable {

    @NotNull
    private List<NodeTrustScoreData> nodeTrustScoreDataList;
    @NotNull
    private Hash trustScoreNodeHash;
    @NotNull
    private SignatureData signatureData;

    @Override
    public SignatureData getSignature() {
        return signatureData;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.signatureData = signature;
    }

    @Override
    public Hash getSignerHash() {
        return trustScoreNodeHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        this.trustScoreNodeHash = signerHash;
    }

}
