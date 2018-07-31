package io.coti.common.communication;

import io.coti.common.data.Hash;
import io.coti.common.data.SignatureData;
import io.coti.common.data.interfaces.IEntity;
import lombok.Data;

@Data
public class DspVote implements IEntity {
    public Hash voterDspHash;

    public Hash transactionHash;

    public boolean isValidTransaction;

    public SignatureData signature;

    @Override
    public Hash getHash() {
        return transactionHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.transactionHash = hash;
    }
}
