package io.coti.common.communication.interfaces;

import io.coti.common.data.TransactionData;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ITransactionReceiver {
    void init(Function<TransactionData, String> unconfirmedTransactionsHandler);
}
