package io.coti.financialserver.http.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class GetDocumentFileData implements ISignable, ISignValidatable {
    @NotNull
    private Hash documentHash;
    @NotNull
    private Hash userHash;
    @NotNull
    private @Valid SignatureData userSignature;

    public GetDocumentFileData(Hash documentHash, Hash userHash, SignatureData userSignature) {
        this.documentHash = documentHash;
        this.userHash = userHash;
        this.userSignature = userSignature;
    }
    
    @Override
    public SignatureData getSignature() {
        return userSignature;
    }

    @Override
    public Hash getSignerHash() {
        return userHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        this.userHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.userSignature = signature;
    }
}
