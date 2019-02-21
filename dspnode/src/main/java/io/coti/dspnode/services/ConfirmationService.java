package io.coti.dspnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.model.NotTotalConfirmedTransactionHashes;
import io.coti.basenode.services.BaseNodeConfirmationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfirmationService extends BaseNodeConfirmationService {

    @Autowired
    NotTotalConfirmedTransactionHashes notTotalConfirmedTransactionHashes;

    @Override
    protected void removeConfirmedTxFromUnconfirmedTransactions(Hash txHash){
        notTotalConfirmedTransactionHashes.deleteByHash(txHash);
    }
}
