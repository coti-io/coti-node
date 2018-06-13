package io.coti.cotinode.services.interfaces;

import io.coti.cotinode.model.Transaction;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ISourceSelector {
    public List<Transaction> selectSourcesForAttachment(
            Map<Integer,? extends List<Transaction>> trustScoreToTransactionMapping,
            int transactionTrustScore,
            Date transactionCreationTime,
            int minSourcePercentage,
            int maxNeighbourhoodRadius);
}
