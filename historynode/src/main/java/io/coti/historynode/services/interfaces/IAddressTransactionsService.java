package io.coti.historynode.services.interfaces;

import io.coti.basenode.data.TransactionData;

public interface IAddressTransactionsService {
    void saveToAddressTransactionsHistories(TransactionData transaction);
}
