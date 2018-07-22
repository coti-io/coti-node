package io.coti.common.communication.interfaces;

import io.coti.common.data.TransactionData;

import java.util.function.Consumer;

public interface ITransactionPropagationSubscriber {
    void init(Consumer<TransactionData> unconfirmedTransactionsHandler);
}
