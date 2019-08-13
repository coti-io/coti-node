package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

@Data
public class DspVote implements IPropagatable, ISignable, ISignValidatable {

    private static final long serialVersionUID = -2950462664712850605L;
    public Hash voterDspHash;
    public Hash transactionHash;
    public boolean validTransaction;
    public SignatureData signature;

    private DspVote() {
    }

    public DspVote(Hash transactionHash, boolean validTransaction) {
        this.transactionHash = transactionHash;
        this.validTransaction = validTransaction;
    }

    @Override
    public Hash getHash() {
        return transactionHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.transactionHash = hash;
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

    public String toString() {
        return String.format("Transaction Hash= {}, Voter Hash= {}, IsValid= {}", transactionHash, voterDspHash, isValidTransaction());
    }
}
