package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;

import java.util.List;

public interface IClusterService {

    TransactionData addTransactionDataToSources(TransactionData zeroSpendTransaction);

    TransactionData selectSources(TransactionData transactionData);

    void setInitialUnconfirmedTransactions(List<Hash> transactionHashes);

}
