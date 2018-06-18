package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.TransactionData;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ISourceSelector {
    public List<TransactionData> selectSourcesForAttachment(
            Map<Integer,? extends List<TransactionData>> trustScoreToTransactionMapping,
            int transactionTrustScore,
            Date transactionCreationTime,
            int minSourcePercentage,
            int maxNeighbourhoodRadius);
}
