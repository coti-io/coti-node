package io.coti.basenode.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class TrustScoreNodeResultData implements Serializable {
    private Hash trustScoreNodeHash;
    private SignatureData trustScoreNodeSignature;

}
