package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class ReceiverBaseTransactionOwnerData implements IEntity, ISignable, ISignValidatable {
    @NotNull
    private Hash merchantHash;

    @NotNull
    private Hash receiverBaseTransactionHash;

    @NotNull
    private @Valid SignatureData merchantSignature;

    private ReceiverBaseTransactionOwnerData() {

    }

    @Override
    public Hash getHash() {
        return receiverBaseTransactionHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.receiverBaseTransactionHash = hash;
    }

    @Override
    public SignatureData getSignature() {
        return merchantSignature;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.merchantSignature = signature;
    }

    @Override
    public Hash getSignerHash() {
        return merchantHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        this.merchantHash = signerHash;
    }
}
