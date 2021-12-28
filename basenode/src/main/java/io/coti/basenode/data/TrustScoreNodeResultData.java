package io.coti.basenode.data;

import lombok.Data;

import javax.validation.Valid;
import java.io.Serializable;

@Data
public class TrustScoreNodeResultData implements Serializable {

    private static final long serialVersionUID = -5904500945533790796L;
    private @Valid Hash trustScoreNodeHash;
    private @Valid SignatureData trustScoreNodeSignature;
    private boolean valid;

    private TrustScoreNodeResultData() {

    }

    public TrustScoreNodeResultData(Hash trustScoreNodeHash, boolean valid) {
        this.trustScoreNodeHash = trustScoreNodeHash;
        this.valid = valid;
    }

    public SignatureData getSignature() {
        return trustScoreNodeSignature;
    }

    public void setSignature(SignatureData signatureData) {
        this.trustScoreNodeSignature = signatureData;
    }

}
