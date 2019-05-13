package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class DisputeUpdateItemData implements Serializable, ISignable, ISignValidatable {

    private static final long serialVersionUID = 3456200480822448729L;
    @NotNull
    private Hash userHash;
    @NotNull
    private List<Long> itemIds;
    @NotNull
    private Hash disputeHash;
    @NotNull
    private @Valid DisputeItemStatus status;
    @NotNull
    private SignatureData userSignature;

    @Override
    public SignatureData getSignature() {
        return userSignature;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.userSignature = signature;
    }

    @Override
    public Hash getSignerHash() {
        return userHash;
    }

    @Override
    public void setSignerHash(Hash hash) {
        userHash = hash;
    }
}
