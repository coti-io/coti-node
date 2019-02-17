package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.services.interfaces.IIndexService;
import org.springframework.stereotype.Service;

@Service
public class BaseNodeIndexService implements IIndexService {

    public void incrementAndGetTotalConfirmed(Hash hash, long totalConfirmedTransactions) {
    }
}
