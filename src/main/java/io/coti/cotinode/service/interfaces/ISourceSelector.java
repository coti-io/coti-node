package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.TransactionData;

import java.util.List;
import java.util.Vector;

public interface ISourceSelector {
    List<TransactionData> selectSourcesForAttachment(
            Vector<TransactionData>[] trustScoreToTransactionMapping,
            double transactionTrustScore);
}
