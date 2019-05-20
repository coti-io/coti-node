package io.coti.basenode.data;

import lombok.Data;

@Data
public class TccInfo extends ConfirmationData {

    private static final long serialVersionUID = -7959572670527143220L;
    private Hash hash;
    private double trustChainTrustScore;

    public TccInfo(Hash hash, double trustChainTrustScore) {
        this.hash = hash;
        this.trustChainTrustScore = trustChainTrustScore;
    }
}