package io.coti.basenode.data;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TrustChainConfirmationResult {

    private List<TransactionData> topologicalOrderedGraph;
    private Map<Hash, Double> transactionTrustChainTrustScoreMap;

    public TrustChainConfirmationResult(Map<Hash, Double> transactionTrustChainTrustScoreMap, List<TransactionData> topologicalOrderedGraph) {
        this.topologicalOrderedGraph = topologicalOrderedGraph;
        this.transactionTrustChainTrustScoreMap = transactionTrustChainTrustScoreMap;
    }
}
