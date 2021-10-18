package io.coti.basenode.data;

import lombok.Data;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ReducedExistingTransactionData {

    private Hash hash;
    private boolean trustChainConsensus;
    private List<Pair<Hash, BigDecimal>> addressAmounts = new ArrayList<>();

    public ReducedExistingTransactionData(TransactionData transactionData) {
        hash = transactionData.getHash();
        trustChainConsensus = transactionData.isTrustChainConsensus();
        transactionData.getBaseTransactions().forEach(baseTransactionData -> addressAmounts.add(new ImmutablePair<>(baseTransactionData.getAddressHash(), baseTransactionData.getAmount())));
    }
}
