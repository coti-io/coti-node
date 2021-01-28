package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TransactionDspVote extends DspVote implements IPropagatable, ISignable, ISignValidatable {

    private static final long serialVersionUID = 4264452456110712450L;
    private Hash transactionHash;

    private TransactionDspVote() {
        super();
    }

    public TransactionDspVote(Hash transactionHash, boolean validTransaction) {
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
    public String toString() {
        return String.format("Transaction Hash= %s, Voter Hash= %s, IsValid= %s", transactionHash, voterDspHash, validTransaction);
    }
}
