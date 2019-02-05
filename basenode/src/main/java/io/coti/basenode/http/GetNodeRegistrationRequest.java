package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
public class GetNodeRegistrationRequest extends Request implements ISignable {

    private Hash nodeHash;
    private SignatureData nodeSignature;
    private NodeType nodeType;

    public GetNodeRegistrationRequest(@NotNull NodeType nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        nodeHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
       nodeSignature = signature;
    }
}
