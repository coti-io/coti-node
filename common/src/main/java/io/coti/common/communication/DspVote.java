package io.coti.common.communication;

import io.coti.common.data.Hash;
import io.coti.common.data.SignatureData;
import io.coti.common.data.interfaces.IEntity;
import lombok.Data;

import java.io.Serializable;

@Data
public class DspVote implements IEntity {
    private Hash voterDspHash;

    private Hash transactionHash;

    private Boolean isValidTransaction;

    private SignatureData signature;

    public DspVote(Hash transactionHash, Hash voterDspId, Boolean isValidTransaction, SignatureData signature) {
        this.transactionHash = transactionHash;
        this.voterDspHash = voterDspId;
        this.isValidTransaction = isValidTransaction;
        this.signature = signature;
    }

    private DspVote(){}




    @Override
    public Hash getHash() {
        return transactionHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.transactionHash = hash;
    }
}
