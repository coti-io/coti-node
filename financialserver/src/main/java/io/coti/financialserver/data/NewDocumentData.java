package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;

public class NewDocumentData implements ISignable, ISignValidatable {

    private Hash userHash;
    private Hash disputeHash;
    private long itemId;
    private String name;
    private String description;
    private SignatureData signature;

    public NewDocumentData(Hash userHash, Hash disputeHash, long itemId, String name, String description, SignatureData signature) {
        this.userHash = userHash;
        this.disputeHash = disputeHash;
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.signature = signature;
    }

    @Override
    public Hash getSignerHash() {
        return userHash;
    }

    public Hash getDisputeHash() {
        return disputeHash;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public long getItemId() {
        return itemId;
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
