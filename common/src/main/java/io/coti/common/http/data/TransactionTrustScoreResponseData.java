package io.coti.common.http.data;

import io.coti.common.data.Hash;
import io.coti.common.data.SignatureData;
import io.coti.common.data.TransactionTrustScoreData;
import lombok.Data;

@Data
public class TransactionTrustScoreResponseData{
    private String userHash;
    private String transactionHash;
    private double trustScore;
    private String trustScoreNodeHash;
    private SignatureData signature;

    public TransactionTrustScoreResponseData(TransactionTrustScoreData transactionTrustScoreData) {
        this.userHash = transactionTrustScoreData.getUserHash().toHexString();
        this.transactionHash = transactionTrustScoreData.getTransactionHash().toHexString();
        this.trustScore = transactionTrustScoreData.getTrustScore();
        this.trustScoreNodeHash = transactionTrustScoreData.getTrustScoreNodeHash().toHexString();
        this.signature = transactionTrustScoreData.getSignature();
    }

}
