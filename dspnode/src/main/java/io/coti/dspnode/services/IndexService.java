package io.coti.dspnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.services.BaseNodeIndexService;
import io.coti.dspnode.model.NotTotalConfirmedTransactionHashes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IndexService extends BaseNodeIndexService {

    @Autowired
    private NotTotalConfirmedTransactionHashes notTotalConfirmedTransactionHashes;

    // TODO rename.
    public void incrementAndGetTotalConfirmed(Hash transactionHash, long totalConfirmedTransactions) {
        notTotalConfirmedTransactionHashes.deleteByHash(transactionHash);
    }
}
