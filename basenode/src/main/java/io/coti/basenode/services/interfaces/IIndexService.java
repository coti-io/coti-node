package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;

public interface IIndexService {
    void incrementAndGetTotalConfirmed(Hash hash, long totalConfirmedTransactions);
}