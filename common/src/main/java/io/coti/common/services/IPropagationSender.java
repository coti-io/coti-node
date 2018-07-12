package io.coti.common.services;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;

import java.util.List;

public interface IPropagationSender {
    void propagateTransactionToDspNode(TransactionData transactionData);

    TransactionData propagateTransactionFromDspByHash(Hash transactionHash);

    TransactionData propagateTransactionFromDspByIndex(int index);

    List<TransactionData> propagateMultiTransactionFromDsp(int index);

    void propagateTransactionToSpecificDspNode(TransactionData transactionData, String node);

    String getMostUpdatedDspNode();

}
