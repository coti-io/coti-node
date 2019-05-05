package io.coti.basenode.data;

import lombok.Data;

import java.util.List;

@Data
public class TccInfo extends ConfirmationData {

    private static final long serialVersionUID = -7959572670527143220L;
    private Hash hash;
    private List<Hash> trustChainTransactionHashes;
    private double trustChainTrustScore;

    public TccInfo(Hash hash, List<Hash> trustChainTransactionHashes, double trustChainTrustScore) {
        this.hash = hash;
        this.trustChainTransactionHashes = trustChainTransactionHashes;
        this.trustChainTrustScore = trustChainTrustScore;
    }
}