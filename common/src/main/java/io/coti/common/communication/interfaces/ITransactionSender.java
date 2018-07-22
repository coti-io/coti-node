package io.coti.common.communication.interfaces;

import io.coti.common.data.TransactionData;

public interface ITransactionSender {

    void sendTransaction(TransactionData transactionData);
}
