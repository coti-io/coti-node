package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;

public interface IIndexService {
    // TODO rename
    void incrementAndGetTotalConfirmed(Hash transactionHash, long totalConfirmedTransactions);
}