package io.coti.common.communication.interfaces;

import io.coti.common.data.TransactionData;

public interface ITransactionPropagationPublisher {

    void propagateTransaction(TransactionData transactionData);
}
