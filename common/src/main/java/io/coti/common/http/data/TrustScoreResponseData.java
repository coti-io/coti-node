package io.coti.common.http.data;

import io.coti.common.data.Hash;
import io.coti.common.data.SignatureData;
import lombok.Data;

@Data
public class TrustScoreResponseData {
    private String userHash;
    private String transactionHash;
    private double trustScore;
    private String trustScoreNodeHash;
    private SignatureData signature;

    public TrustScoreResponseData(Hash userHash, Hash transactionHash, double trustScore, Hash trustScoreNodeHash, SignatureData signature) {
        this.userHash = userHash.toHexString();
        this.transactionHash = transactionHash.toHexString();
        this.trustScore = trustScore;
        this.trustScoreNodeHash = trustScoreNodeHash.toHexString();
        this.signature = signature;
    }
}
