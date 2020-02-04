package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;

public interface ITransactionCuratorService {

    void init();

    void addUnconfirmedTransaction(Hash transactionHash);

    void removeConfirmedReceiptTransaction(Hash transactionHash);
}
