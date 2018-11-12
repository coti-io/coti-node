package io.coti.basenode.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class TrustScoreNodeResultData implements Serializable {
    private Hash trustScoreNodeHash;
    private SignatureData trustScoreNodeSignature;
    private boolean valid;

    public void setSignature(SignatureData signatureData) {
        this.trustScoreNodeSignature = signatureData;
    }

    public SignatureData getSignature() {
        return trustScoreNodeSignature;
    }

}
