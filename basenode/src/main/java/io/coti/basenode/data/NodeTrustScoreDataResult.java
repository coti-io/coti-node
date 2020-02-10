package io.coti.basenode.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class NodeTrustScoreDataResult implements Serializable {

    private Hash trustScoreNodeHash;
    private boolean valid;
    private SignatureData signatureData;

}
