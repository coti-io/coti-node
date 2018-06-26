package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.TransactionData;

import java.util.List;
import java.util.Map;

public interface ISourceSelector {
    List<TransactionData> selectSourcesForAttachment(
            Map<Integer, List<TransactionData>> trustScoreToTransactionMapping,
            double transactionTrustScore);
}
