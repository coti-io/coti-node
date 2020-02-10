package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.TransactionData;

import java.util.List;
import java.util.Set;

public interface ISourceSelector {
    
    List<TransactionData> selectSourcesForAttachment(
            List<Set<TransactionData>> trustScoreToTransactionMapping,
            double transactionTrustScore);
}
