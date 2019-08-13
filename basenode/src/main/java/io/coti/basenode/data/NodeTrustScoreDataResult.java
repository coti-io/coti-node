package io.coti.basenode.data;

import lombok.Data;

@Data
public class NodeTrustScoreDataResult {

    private Hash trustScoreNodeHash;
    private boolean valid;
    private SignatureData signatureData;

}
