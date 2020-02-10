package io.coti.basenode.http.data;

import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TransactionTrustScoreData;
import io.coti.basenode.http.data.interfaces.IResponseData;
import lombok.Data;

@Data
public class TransactionTrustScoreResponseData implements IResponseData {

    private double trustScore;
    private String trustScoreNodeHash;
    private SignatureData trustScoreNodeSignature;

    public TransactionTrustScoreResponseData(TransactionTrustScoreData transactionTrustScoreData) {

        this.trustScore = transactionTrustScoreData.getTrustScore();
        this.trustScoreNodeHash = transactionTrustScoreData.getTrustScoreNodeHash().toHexString();
        this.trustScoreNodeSignature = transactionTrustScoreData.getTrustScoreNodeSignature();
    }

}
