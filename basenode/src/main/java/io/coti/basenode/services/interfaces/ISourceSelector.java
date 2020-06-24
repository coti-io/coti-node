package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ISourceSelector {

    List<TransactionData> selectSourcesForAttachment(
            List<Set<Hash>> trustScoreToTransactionMapping,
            Map<Hash, TransactionData> sourceMap, double transactionTrustScore);
}
