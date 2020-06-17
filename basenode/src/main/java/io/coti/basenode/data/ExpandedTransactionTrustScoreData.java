package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExpandedTransactionTrustScoreData extends TransactionTrustScoreData implements ISignable, ISignValidatable {

    private Hash userHash;
    private Hash transactionHash;

    public ExpandedTransactionTrustScoreData(Hash userHash, Hash transactionHash, double trustScore) {
        super(trustScore);
        this.userHash = userHash;
        this.transactionHash = transactionHash;
    }

    public ExpandedTransactionTrustScoreData(Hash userHash, Hash transactionHash, TransactionTrustScoreData transactionTrustScoreData) {
        super(transactionTrustScoreData);
        this.userHash = userHash;
        this.transactionHash = transactionHash;
    }

    @Override
    public SignatureData getSignature() {
        return trustScoreNodeSignature;
    }

    @Override
    public void setSignature(SignatureData signature) {
        trustScoreNodeSignature = signature;
    }

    @Override
    public Hash getSignerHash() {
        return trustScoreNodeHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        trustScoreNodeHash = signerHash;
    }
}
