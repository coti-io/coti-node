package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

@Data
public class FullTransactionTrustScoreData extends TransactionTrustScoreData implements ISignable, ISignValidatable {

    private Hash userHash;
    private Hash transactionHash;

    public FullTransactionTrustScoreData(Hash userHash, Hash transactionHash, double trustScore) {
        super(trustScore);
        this.userHash = userHash;
        this.transactionHash = transactionHash;
    }

    public FullTransactionTrustScoreData(Hash userHash, Hash transactionHash, TransactionTrustScoreData transactionTrustScoreData) {
        super(transactionTrustScoreData);
        this.userHash = userHash;
        this.transactionHash = transactionHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        trustScoreNodeHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        trustScoreNodeSignature = signature;
    }

    @Override
    public SignatureData getSignature() {
        return trustScoreNodeSignature;
    }

    @Override
    public Hash getSignerHash() {
        return trustScoreNodeHash;
    }
}
