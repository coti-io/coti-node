package io.coti.common.data;

import io.coti.common.data.Hash;
import io.coti.common.data.SignatureData;
import io.coti.common.data.interfaces.IEntity;
import io.coti.common.data.interfaces.ISignValidatable;
import io.coti.common.data.interfaces.ISignable;
import lombok.Data;

@Data
public class DspVote implements IEntity, ISignable, ISignValidatable {
    public Hash voterDspHash;

    public Hash transactionHash;

    public boolean validTransaction;

    public SignatureData signature;

    @Override
    public Hash getHash() {
        return transactionHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.transactionHash = hash;
    }

    @Override
    public SignatureData getSignature() {
        return signature;
    }

    @Override
    public Hash getSignerHash() {
        return voterDspHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        voterDspHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.signature = signature;
    }
}
