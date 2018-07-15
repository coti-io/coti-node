package io.coti.common.data;

import lombok.Data;

import java.util.List;

@Data
public class TccInfo {

    private Hash hash;
    private List<Hash> trustChainTransactionHash;
    private double trustChainTrustScore;

    public TccInfo(Hash hash, List<Hash> trustChainTransactionHash, double trustChainTrustScore) {
        this.hash = hash;
        this.trustChainTransactionHash = trustChainTransactionHash;
        this.trustChainTrustScore = trustChainTrustScore;
    }



}
