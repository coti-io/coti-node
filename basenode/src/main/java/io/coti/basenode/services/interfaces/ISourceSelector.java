package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface ISourceSelector {

    List<TransactionData> selectSourcesForAttachment(
            ArrayList<HashSet<Hash>> trustScoreToTransactionMapping,
            Map<Hash, TransactionData> sourceMap, double transactionTrustScore);
}
