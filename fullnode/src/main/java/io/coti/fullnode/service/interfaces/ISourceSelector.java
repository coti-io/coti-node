package io.coti.fullnode.service.interfaces;

import io.coti.common.data.TransactionData;

import java.util.List;
import java.util.Vector;

public interface ISourceSelector {
    List<TransactionData> selectSourcesForAttachment(
            Vector<TransactionData>[] trustScoreToTransactionMapping,
            double transactionTrustScore);
}
