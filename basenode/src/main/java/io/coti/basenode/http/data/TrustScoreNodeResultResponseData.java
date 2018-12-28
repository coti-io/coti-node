package io.coti.basenode.http.data;

import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TrustScoreNodeResultData;
import lombok.Data;

@Data
public class TrustScoreNodeResultResponseData {
    private String trustScoreNodeHash;
    private SignatureData trustScoreNodeSignature;
    private boolean valid;

    private TrustScoreNodeResultResponseData() {

    }

    public TrustScoreNodeResultResponseData(TrustScoreNodeResultData trustScoreNodeResultData) {
        this.trustScoreNodeHash = trustScoreNodeResultData.getTrustScoreNodeHash() == null ? null : trustScoreNodeResultData.getTrustScoreNodeHash().toString();
        this.trustScoreNodeSignature = trustScoreNodeResultData.getSignature();
        this.valid = trustScoreNodeResultData.isValid();

    }

}
