package io.coti.common.services.interfaces;

import io.coti.common.data.TransactionData;

import java.util.List;
import java.util.Vector;

public interface ISourceSelector {
    List<TransactionData> selectSourcesForAttachment(
            Vector<TransactionData>[] trustScoreToTransactionMapping,
            double transactionTrustScore);
}
