package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class GetEntitiesBulkRequest extends Request implements ISignable, ISignValidatable {
//    @NotEmpty(message = "Hashes must not be empty")
//    private List<Hash> hashes;
    private Hash signerHash;
    private SignatureData signature;


    public GetEntitiesBulkRequest() {
    }

    // TODO: Temporary change for checking serialization issues
//public GetEntitiesBulkRequest(List<Hash> hashes) {
//        this.hashes = hashes;
//    }

    @Override
    public SignatureData getSignature() {
        return this.signature;
    }

    @Override
    public Hash getSignerHash() {
        return this.signerHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        this.signerHash = signerHash;

    }

    @Override
    public void setSignature(SignatureData signature) {
        this.signature = signature;
    }
}

