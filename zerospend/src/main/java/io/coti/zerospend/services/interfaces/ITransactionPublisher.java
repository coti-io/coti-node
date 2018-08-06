package io.coti.zerospend.services.interfaces;

import io.coti.common.data.TransactionData;

public interface ITransactionPublisher {
    void publish(TransactionData transactionData, String channelName);
}
