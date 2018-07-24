package io.coti.common.communication.interfaces.publisher;

import io.coti.common.data.TransactionData;

public interface ITransactionPropagationPublisher {

    void propagateTransactionToDSPs(TransactionData transactionData);

    void propagateTransactionToFullNodes(TransactionData transactionData);
}
