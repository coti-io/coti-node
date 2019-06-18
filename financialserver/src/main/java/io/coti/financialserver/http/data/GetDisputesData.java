package io.coti.financialserver.http.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.financialserver.data.ActionSide;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class GetDisputesData implements ISignable, ISignValidatable {

    private List<Hash> disputeHashes;
    @NotNull
    private ActionSide disputeSide;
    @NotNull
    private Hash userHash;
    @NotNull
    private @Valid SignatureData userSignature;

    private GetDisputesData() {

    }

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
