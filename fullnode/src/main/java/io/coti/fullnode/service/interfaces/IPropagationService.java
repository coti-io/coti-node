package io.coti.fullnode.service.interfaces;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;

import java.util.List;

public interface IPropagationService {
    void propagateTransactionToDspNode(TransactionData transactionData);

    TransactionData propagateTransactionFromDspByHash(Hash transactionHash);

    TransactionData propagateTransactionFromDspByIndex(int index);

    List<TransactionData> propagateMultiTransactionFromDsp(int index);

    void updateLastIndex(TransactionData transactionData);

    void updateLastTransactionFromFile();
}
