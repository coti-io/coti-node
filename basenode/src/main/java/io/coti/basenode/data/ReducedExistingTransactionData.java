package io.coti.basenode.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ReducedExistingTransactionData {

    private Hash hash;
    private boolean trustChainConsensus;
    private List<ReducedExistingBaseTransactionData> addressAmounts = new ArrayList<>();

    public ReducedExistingTransactionData(TransactionData transactionData) {
        hash = transactionData.getHash();
        trustChainConsensus = transactionData.isTrustChainConsensus();
        transactionData.getBaseTransactions().forEach(baseTransactionData -> addressAmounts.add((new ReducedExistingBaseTransactionData(baseTransactionData))));
    }
}
