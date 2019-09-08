package io.coti.basenode.data;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class TransactionTrustScoreData implements Serializable {

    private static final long serialVersionUID = 7892150405277241806L;
    @NotNull
    protected Double trustScore;
    @NotNull
    protected Hash trustScoreNodeHash;
    @NotNull
    protected @Valid SignatureData trustScoreNodeSignature;

    private TransactionTrustScoreData() {
    }

    public TransactionTrustScoreData(double trustScore) {
        this.trustScore = trustScore;
    }

    public TransactionTrustScoreData(TransactionTrustScoreData transactionTrustScoreData) {
        this.trustScore = transactionTrustScoreData.getTrustScore();
        this.trustScoreNodeHash = transactionTrustScoreData.getTrustScoreNodeHash();
        this.trustScoreNodeSignature = transactionTrustScoreData.getTrustScoreNodeSignature();
    }
}
