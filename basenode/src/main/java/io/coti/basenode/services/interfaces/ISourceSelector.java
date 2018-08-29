package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.TransactionData;

import java.util.List;

public interface ISourceSelector {
    List<TransactionData> selectSourcesForAttachment(
            List<List<TransactionData>> trustScoreToTransactionMapping,
            double transactionTrustScore);
}
