package io.coti.financialserver.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class DisputeItemVoteData implements Serializable, ISignable, ISignValidatable {

    private Hash userHash;
    @NotNull
    private Long itemId;
    @NotNull
    private Hash disputeHash;
    @NotNull
    private DisputeItemStatus status;
    private SignatureData userSignature;

    @Override
    public SignatureData getSignature() {
    return userSignature;
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
    this.userSignature = signature;
    }
}
