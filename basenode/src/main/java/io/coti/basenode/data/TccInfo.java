package io.coti.basenode.data;

import lombok.Data;

import java.time.Instant;

@Data
public class TccInfo extends ConfirmationData {

    private static final long serialVersionUID = -7959572670527143220L;
    private Hash hash;
    private double trustChainTrustScore;
    private Instant trustChainConsensusTime;

    public TccInfo(Hash hash, double trustChainTrustScore, Instant trustChainConsensusTime) {
        this.hash = hash;
        this.trustChainTrustScore = trustChainTrustScore;
        this.trustChainConsensusTime = trustChainConsensusTime;
    }
}