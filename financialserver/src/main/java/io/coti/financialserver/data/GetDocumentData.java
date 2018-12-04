package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;

public class GetDocumentData implements ISignable, ISignValidatable {

    private SignatureData signature;
    private Hash userHash;

    public GetDocumentData(Hash userHash, SignatureData signature) {
        this.signature = signature;
        this.userHash = userHash;
    }

    @Override
    public SignatureData getSignature() {
        return signature;
    }

    @Override
    public Hash getSignerHash() {
        return userHash;
    }

    @Override
    public void setSignerHash(Hash hash) {
        userHash = hash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.signature = signature;
    }
}
