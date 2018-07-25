package io.coti.common.http.websocket;

import io.coti.common.data.TransactionData;
import io.coti.common.http.data.TransactionResponseData;
import io.coti.common.http.data.TransactionStatus;


public class NotifyTransactionChange {



    public TransactionStatus status;
    public TransactionResponseData transactionData;

    public NotifyTransactionChange(TransactionData transactionData, TransactionStatus transactionStatus)
    {
        this.status = transactionStatus;
        this.transactionData = new TransactionResponseData(transactionData);
    }
}
