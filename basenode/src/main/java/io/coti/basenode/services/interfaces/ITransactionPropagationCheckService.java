package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;

public interface ITransactionPropagationCheckService {

    void init();

    void addUnconfirmedTransaction(Hash transactionHash);

    void removeTransactionHashFromUnconfirmed(Hash transactionHash);

    void removeTransactionHashFromUnconfirmedOnBackPropagation(Hash transactionHash);
}
