package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

@Data
public class DisputeItemVoteData implements Serializable, ISignable, ISignValidatable {
    @NotNull
    private Hash arbitratorHash;
    @NotNull
    private Hash disputeHash;
    @NotNull
    private Long itemId;
    @NotNull
    private DisputeItemVoteStatus status;
    private Instant voteTime;
    @NotNull
    private @Valid SignatureData arbitratorSignature;

    @Override
    public SignatureData getSignature() {
        return arbitratorSignature;
    }

    @Override
    public Hash getSignerHash() {
        return arbitratorHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        arbitratorHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.arbitratorSignature = signature;
    }
}
