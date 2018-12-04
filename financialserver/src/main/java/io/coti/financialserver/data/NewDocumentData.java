package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;

public class NewDocumentData implements ISignable, ISignValidatable {

    private Hash userHash;
    private Integer disputeId;
    private Integer documentId;
    private SignatureData signature;


    public NewDocumentData(Hash userHash, Integer disputeId, Integer documentId, SignatureData signature) {
        this.userHash = userHash;
        this.disputeId = disputeId;
        this.documentId = documentId;
        this.signature = signature;
    }

    @Override
    public Hash getSignerHash() {
        return userHash;
    }

    public Integer getDisputeId() {
        return disputeId;
    }

    public Integer getDocumentId() {
        return documentId;
    }

    @Override
    public SignatureData getSignature() {
        return signature;
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
