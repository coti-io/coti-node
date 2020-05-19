package io.coti.basenode.data;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;

@Data
public class ReducedExistingTransactionData {

    private Hash hash;
    private boolean trustChainConsensus;
    private HashMap<Hash, BigDecimal> addressAmountMap = new HashMap<>();

    public ReducedExistingTransactionData(TransactionData transactionData) {
        hash = transactionData.getHash();
        trustChainConsensus = transactionData.isTrustChainConsensus();
        transactionData.getBaseTransactions().forEach(baseTransactionData -> addressAmountMap.put(baseTransactionData.getAddressHash(), baseTransactionData.getAmount()));
    }
}
