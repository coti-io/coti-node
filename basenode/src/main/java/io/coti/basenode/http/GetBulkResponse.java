package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;


@Data
public class GetBulkResponse extends Response implements ISignValidatable, ISignable {

    //@NotNull
    private Hash signerHash;
    //@NotNull
    private SignatureData signature;

    public GetBulkResponse() {

    }

    public GetBulkResponse(String message, String status) {
        super(message, status);

    }

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
